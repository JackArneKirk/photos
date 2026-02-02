package com.example.demo.services.impl;

import static com.example.demo.constant.Constant.LOG_PREFIX;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;
import com.example.demo.model.Photo;
import com.example.demo.repository.PhotoRepository;
import com.example.demo.services.MetadataService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MetadataServiceImpl implements MetadataService {

    private static final String CLASS_NAME = MetadataServiceImpl.class.getSimpleName();

    @Value("${file.upload-dir}")
    private String rootFolderPath;

    @Async
    @Override
    public void processAndStoreMetadata(Photo photo, PhotoRepository repo) {

        String imagePath = photo.getFileName();
        File imageFile = new File(rootFolderPath + "/" + imagePath);

        try {
            Metadata metadata = ImageMetadataReader.readMetadata(imageFile);
            Iterator<Directory> directories = metadata.getDirectories().iterator();
            while (directories.hasNext()) {
                Directory directory = directories.next();
                log.info("looking at directory: " + directory.getName());
                if (GpsDirectory.class.isInstance(directory)) {
                    processGPSMetadata((GpsDirectory) directory, photo);
                }
            }
            log.info("saving after metadata updates...");
            repo.save(photo);
        } catch (ImageProcessingException | IOException e) {
            log.error("{} -- {} Could not read image metadata: {}", LOG_PREFIX, CLASS_NAME, e.getMessage());
        }
    }

    private void processGPSMetadata(GpsDirectory gpsData, Photo photo) {
        GeoLocation location = gpsData.getGeoLocation();
        log.info("Setting location data: " + location.getLongitude() + ", " + location.getLatitude());
        photo.setLongitude(location.getLongitude());
        photo.setLatitude(location.getLatitude());
    }
}
