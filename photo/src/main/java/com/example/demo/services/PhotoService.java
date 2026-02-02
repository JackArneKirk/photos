package com.example.demo.services;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.example.demo.constant.Constant.LOG_PREFIX;
import static com.example.demo.constant.Constant.RESULT_SET_LIMIT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.model.Photo;
import com.example.demo.model.DTO.GeoDataDTO;
import com.example.demo.model.DTO.GeoResponseDTO;
import com.example.demo.repository.PhotoRepository;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PhotoService {

    private static final String CLASS_NAME = PhotoService.class.getSimpleName();

    @Value("${file.upload-dir}")
    private String rootFolderPath;

    private PhotoRepository repo;

    private int limit = 150;

    @Autowired
    private ThumbnailService thumbnailService;

    @Autowired
    private MetadataService metadataService;

    @Autowired
    private FaceService faceService;

    public PhotoService(PhotoRepository repo) {
        this.repo = repo;
    }

    @PostConstruct
    public void init() {
        log.info("{} -- {} initialised. Root folder dir: {}", LOG_PREFIX, CLASS_NAME, rootFolderPath);
    }

    public boolean storePhoto(MultipartFile file, Photo photo) {

        if (!file.getContentType().startsWith("image")) {
            log.warn("{} -- {} Attached file is not an image, but type: {}", LOG_PREFIX, CLASS_NAME,
                    file.getContentType());
            return false;
        }
        setFilePath(photo, file);
        setSize(photo, file);
        try {
            file.transferTo(new File(rootFolderPath + "/" + photo.getFileName()));
            repo.save(photo); // TODO - do something with response
            thumbnailService.createThumbnail(photo);
            metadataService.processAndStoreMetadata(photo, repo);
            faceService.detectFace(photo);

        } catch (OptimisticLockingFailureException | IllegalArgumentException e) {
            log.error("{} -- {} occurred when trying to save asset metadata to database", CLASS_NAME,
                    e.getClass().getSimpleName());
            return false;
        } catch (IOException e) {
            log.error("{} -- {} occurred when trying to save asset to file system at path: {}\nerror:{}", CLASS_NAME,
                    e.getClass().getSimpleName(), rootFolderPath + "/" + photo.getFileName(), e.getMessage());
            return false;
        }
        return true;
    }

    public List<Photo> findPhotos(List<String> keywords) {
        log.info("{} -- {} finding photos...... {}", LOG_PREFIX, CLASS_NAME, String.join(", ", keywords));
        String dateString = "12-01-2026";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate date = LocalDate.parse(dateString, formatter);
        return findPhotos(date.atStartOfDay(), ">", 150, 0, keywords.toArray(new String[keywords.size()]));
    }

    public List<Photo> findPhotos(LocalDateTime date, String operation, int limit, int offset, String[] keywords) {
        int checkedNumberOfResults = limit > 0 && limit < RESULT_SET_LIMIT ? limit : RESULT_SET_LIMIT;
        int checkedOffset = offset >= 0 ? offset : 0;
        if (keywords == null) {
            return searchPhotosAscending(checkedOffset, checkedNumberOfResults);
        } else {
            log.info("{} -- {} SEARCHING for keywords: {}", LOG_PREFIX, CLASS_NAME, String.join(", ", keywords));
            return repo.searchByKeyword(Set.of(keywords), keywords.length);
        }
    }

    public List<Photo> searchPhotos() {
        return repo.findAll();
    }

    public List<Photo> searchPhotosAscending(int offset, int limit) {
        Pageable mostRecent = PageRequest.of(offset / limit, limit, Sort.by("createdDate").descending());
        return repo.findAll(mostRecent).getContent();
    }

    public List<Photo> searchPhotosByDate(LocalDateTime date, String operation, int limit, int offset) {
        log.info("{} -- {} Searching for assets created {} {}", LOG_PREFIX, CLASS_NAME, operation, date.toString());
        if (operation.equals(">")) {
            return repo.searchByDateBefore(date, limit, offset);
        } else if (operation.equals("<")) {
            return repo.searchByDateAfter(date, limit, offset);
        }
        throw new IllegalArgumentException("Operation Invalid");
    }

    public GeoResponseDTO getGeoData(int limit) {
        limit = limit > 10000 ? 10000 : limit;
        Pageable geoLimit = PageRequest.of(0, limit);
        Page<Photo> photos = repo.findAll(geoLimit);
        List<GeoDataDTO> geoPoints = photos.stream()
                .filter(photo -> photo.getLatitude() != null && photo.getLongitude() != null)
                .map(photo -> new GeoDataDTO(photo))
                .collect(Collectors.toList());
        return new GeoResponseDTO(geoPoints);
    }

    private void setSize(Photo photo, MultipartFile file) {
        if (photo.getSize() <= 0) {
            photo.setSize(file.getSize());
        }
    }

    private void setFilePath(Photo photo, MultipartFile file) {
        if (photo.getFileName() == null || photo.getFileName().isBlank()) {
            photo.setFileName(UUID.randomUUID().toString() + "." + file.getContentType().split("/")[1]);
        }
    }
}
