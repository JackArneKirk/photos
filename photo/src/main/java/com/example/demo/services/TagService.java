package com.example.demo.services;

import java.util.List;

import com.example.demo.model.DTO.TagDTO;

public interface TagService {
    
    /**
     * Create tags based on the given list. The way that tags are created should depend on the populated fields in the TagDTOs provided.
     * For tagDTOs with a keyword provided, create new tags with the keywords retrieved from IDs
     * For tagDTOs with keyword names provided, create keywords first then use these to create the tags
     * @param tagNames
     */
    public void createTags(List<TagDTO> tagNames);
}
