package com.example.demo.services;

import com.example.demo.model.Photo;
import com.example.demo.repository.PhotoRepository;

public interface MetadataService {
    
    /**
     * Read image from file and process metadata information by setting it on the photo
     * @param photo
     */
    public void processAndStoreMetadata(Photo photo, PhotoRepository repo);
}
