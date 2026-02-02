package com.example.demo.controllers;

import static com.example.demo.constant.Constant.LOG_PREFIX;
import static com.example.demo.utils.StringUtils.determinePath;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import com.example.demo.model.Photo;
import com.example.demo.model.DTO.GeoResponseDTO;
import com.example.demo.model.DTO.PhotoDto;
import com.example.demo.model.DTO.TagDTO;
import com.example.demo.services.FaceService;
import com.example.demo.services.KeywordService;
import com.example.demo.services.PhotoService;
import com.example.demo.services.TagService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class AssetControllerImpl implements AssetController {

    @Autowired
    private PhotoService photoService;

    @Autowired
    private KeywordService keywordService;

    @Autowired
    private TagService tagService;

    @Autowired
    private FaceService faceService;

    @Value("${file.upload-dir}")
    private String rootFolderPath;

    @Value("${thumbnail.suffix}")
    private String thumbnailSuffix;

    private final List<String> allowedSearchOperations = new ArrayList<String>();
    private static final String CLASS_NAME = AssetControllerImpl.class.getSimpleName();

    @PostConstruct
    private void init() {
        allowedSearchOperations.add(">");
        allowedSearchOperations.add("<");
        allowedSearchOperations.add("=");
        allowedSearchOperations.add(">=");
        allowedSearchOperations.add("<=");
    }

    /**
     * Search functionality
     * 
     * @return
     */
    @GetMapping("/photos")
    public ResponseEntity<?> searchPhotos(@RequestParam(required = false) String operation,
            @RequestParam(required = false) String dateString, @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset, @RequestParam(required = false) List<String> keywords) {
        if (operation != null && !allowedSearchOperations.contains(operation)) {
            return ResponseEntity.badRequest().body("error");
        }
        List<Photo> photos = new ArrayList<Photo>();

        if (keywords != null && keywords.size() > 0) {
            log.info("Keywords: {}", keywords.size());
            photos = photoService.findPhotos(keywords);
        } else {
            photos = photoService.searchPhotosAscending(0, 150);
        }
        return ResponseEntity.ok()
                .body(photos.stream().map(
                        photo -> new PhotoDto(photo.getId(), photo.getFileName(), photo.getThumbnailPath(),
                                photo.getFolderPath()))
                        .collect(Collectors.toList()));

        // try {
        // DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        // LocalDate date = LocalDate.parse(dateString, formatter);
        // List<Photo> photos = photoService.searchPhotosByDate(date.atStartOfDay(),
        // operation, numberOfResults,
        // checkedOffset);
        // return ResponseEntity.ok()
        // .body(photos.stream().map(photo -> new PhotoDto(photo.getFileName(),
        // photo.getThumbnailPath(),
        // photo.getFolderPath())).collect(Collectors.toList()));
        // } catch (DateTimeParseException e) {
        // log.warn("{} -- {} Bad Request when searching by date", LOG_PREFIX,
        // CLASS_NAME);
        // return ResponseEntity.badRequest().body("Date parameter must be in format
        // dd-MM-yyyy");
        // }
    }

    @GetMapping(value = "/photos/file/**", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<?> getPhoto(HttpServletRequest request, @RequestParam(required = false) boolean thumb)
            throws IOException {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String filePath = new AntPathMatcher().extractPathWithinPattern(bestMatchPattern, path);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("auth: {}", auth.getPrincipal());
        log.info("{} -- {} retrieving asset at path: {}", LOG_PREFIX, CLASS_NAME, rootFolderPath + "/" + filePath);
        Path fullPath = Paths.get(rootFolderPath + "/" + determinePath(filePath, thumbnailSuffix, thumb));
        if (fullPath.toUri() != null) {
            Resource resource = new UrlResource(fullPath.toUri());
            return resource.exists() ? ResponseEntity.ok().body(resource)
                    : ResponseEntity.status(404).body("File not found");
        } else {
            return ResponseEntity.badRequest().body("Invalid path");
        }
    }

    @PostMapping("/photos")
    public ResponseEntity<?> uploadAssets(@RequestParam("file") MultipartFile[] files,
            @RequestParam("metadata") String metadata) {
        log.info("{} -- {} Recieved request to upload multiple files", LOG_PREFIX, CLASS_NAME);
        ObjectMapper mapper = new ObjectMapper();
        try {
            Photo[] photos = mapper.readValue(metadata, Photo[].class);
            Arrays.stream(photos).filter(Objects::nonNull).forEach(photo -> photo.setCreatedDate(LocalDateTime.now()));
            Arrays.stream(photos).filter(Objects::nonNull).forEach(photo -> photo.setFolderPath(rootFolderPath));

            List<Boolean> responses = new ArrayList<>();
            for (int i = 0; i < photos.length; i++) {
                responses.add(photoService.storePhoto(files[i], photos[i]));
            }
            return new ResponseEntity<>(
                    HttpStatusCode.valueOf(responses.stream().allMatch(x -> x.equals(true)) ? 200 : 400));
        } catch (JsonProcessingException e) {
            log.warn("poorly formatted JSON: could not be adapted to photo[].class");
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/keywords")
    public ResponseEntity<?> createKeywords(@RequestBody String keywordsString) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String[] keywords = mapper.readValue(keywordsString, String[].class);
            List<String> createdIDs = keywordService.createKeywords(Set.of(keywords))
                    .stream()
                    .map(x -> x.toString())
                    .collect(Collectors.toList());
            return ResponseEntity.ok().body("[" + String.join(", ", createdIDs) + "]");
        } catch (Exception e) {
            log.error("{} -- {} exception saving keywords: {}. Error: {}", LOG_PREFIX, CLASS_NAME, keywordsString,
                    e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/tags")
    public ResponseEntity<?> createTags(@RequestBody List<TagDTO> tagDTOs) {
        try {
            tagService.createTags(tagDTOs);
            return new ResponseEntity<>(HttpStatusCode.valueOf(200));
        } catch (Exception e) {
            log.error("{} -- {} exception saving tags: {}. Error: {}", LOG_PREFIX, CLASS_NAME, tagDTOs, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/geos")
    public ResponseEntity<GeoResponseDTO> getPhotoGeos() {
        return ResponseEntity.ok().body(photoService.getGeoData(1000));
    }

    @GetMapping("/similarity")
    public ResponseEntity<?> getFaceSimilarity(@RequestParam long id1, @RequestParam long id2) {
        float similarity = faceService.compareSimilarity(id1, id2);
        return ResponseEntity.ok().body(similarity);
    }

    @GetMapping("/photo/keywords")
    public ResponseEntity<?> getKeywords(@RequestParam long photoID){
        List<String> keywords = keywordService.getKeywords(photoID);
        if(keywords == null){
            return ResponseEntity.badRequest().body("invalid ID");
        }
        return ResponseEntity.ok().body(String.join(", ", keywords));  
    }
}
