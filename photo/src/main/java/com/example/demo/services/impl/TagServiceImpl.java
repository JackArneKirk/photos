package com.example.demo.services.impl;

import static com.example.demo.constant.Constant.LOG_PREFIX;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.model.Keyword;
import com.example.demo.model.Photo;
import com.example.demo.model.PhotoTag;
import com.example.demo.model.DTO.TagDTO;
import com.example.demo.repository.KeywordRepository;
import com.example.demo.repository.PhotoRepository;
import com.example.demo.repository.PhotoTagRepository;
import com.example.demo.services.KeywordService;
import com.example.demo.services.TagService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TagServiceImpl implements TagService {

    private static final String CLASS_NAME = TagServiceImpl.class.getSimpleName();
    private PhotoTagRepository tagRepo;
    private PhotoRepository photoRepo;
    private KeywordRepository keywordRepo;

    @Autowired
    private KeywordService keywordService;

    public TagServiceImpl(PhotoTagRepository tagRepo, PhotoRepository photoRepo, KeywordRepository keywordRepo) {
        this.tagRepo = tagRepo;
        this.photoRepo = photoRepo;
        this.keywordRepo = keywordRepo;
    }

    @Override
    public void createTags(List<TagDTO> tagNames) {
        tagNames.forEach(tag -> {
            if (tag.getKeywordID() != null) {
                createTag(tag);
            } else {
                createTagAndKeyword(tag);
            }
        });
    }

    private void createTag(TagDTO tag) {
        Photo photo = photoRepo.findById(tag.getPhotoID()).orElseThrow();
        Keyword keyword = keywordRepo.findById(tag.getKeywordID()).orElseThrow();
        PhotoTag newTag = new PhotoTag();
        newTag.setKeyword(keyword);
        newTag.setPhoto(photo);
        if (tag.getXNorm() != 0.0f) {
            newTag.setXNorm(tag.getXNorm());
            newTag.setYNorm(tag.getYNorm());
        }
        tagRepo.save(newTag);
    }

    private void createTagAndKeyword(TagDTO tag) {
        Photo photo = photoRepo.findById(tag.getPhotoID()).orElseThrow();
        for (String keywordTitle : tag.getTagNames()) {
            log.info("{} -- {} keyword name: {}", LOG_PREFIX, CLASS_NAME, keywordTitle);
            Optional<Keyword> keyword = keywordRepo.findFirstByName(keywordTitle);
            keyword.ifPresentOrElse(key -> {
                PhotoTag newTag = new PhotoTag();
                newTag.setKeyword(key);
                newTag.setPhoto(photo);
                tagRepo.save(newTag);
            },
                    () -> {
                        Keyword createdKeyword = keywordService.createKeyword(keywordTitle);
                        PhotoTag newTag = new PhotoTag();
                        newTag.setKeyword(createdKeyword);
                        newTag.setPhoto(photo);
                        tagRepo.save(newTag);
                    });
        }
    }
}
