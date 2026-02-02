package com.example.demo.services.impl;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import static com.example.demo.constant.Constant.LOG_PREFIX;

import java.awt.Image;
import java.awt.image.BufferedImage;

import com.example.demo.model.Photo;
import com.example.demo.repository.PhotoRepository;
import com.example.demo.services.ThumbnailService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ThumbnailServiceImpl implements ThumbnailService {

    private final String CLASS_NAME = ThumbnailServiceImpl.class.getSimpleName();

    private PhotoRepository repo;

    @Value("${thumbnail.suffix}")
    private String thumbnailSuffix;

    @Value("${thumbnail.width}")
    private int thumbnailWidth;

    @Value("${thumbnail.height}")
    private int thumbnailHeight;

    @Value("${file.upload-dir}")
    private String rootFolderPath;

    public ThumbnailServiceImpl(PhotoRepository repo) {
        this.repo = repo;
    }

    @Async
    @Override
    public void createThumbnail(Photo photo) {
        String imagePath = photo.getFileName();
        try {
            if(!imagePath.contains(".")){
                log.warn("{} -- {} File does not contain an ext, could not create thumbnail.", LOG_PREFIX, CLASS_NAME);
                return;
            }

            String fileName = imagePath.split("\\.")[0];
            String fileExt = imagePath.split("\\.")[1];
            String filePath = rootFolderPath + "/" + imagePath;
            String thumbnailPath = rootFolderPath + "/" + fileName + thumbnailSuffix + "." + fileExt;

            File f = new File(filePath);
            File thumbnailFile = new File(thumbnailPath);
            Image image = ImageIO.read(f);
            BufferedImage buffer = new BufferedImage(thumbnailWidth, thumbnailHeight, BufferedImage.TYPE_INT_RGB);
            float width = buffer.getWidth();
            float height = buffer.getHeight();
            int scaledSize = Math.round(width > height ? width / height : height / width) * thumbnailWidth;
            buffer.createGraphics().drawImage(image.getScaledInstance(width > height ?  thumbnailWidth : scaledSize,  height > width ? thumbnailHeight : scaledSize, Image.SCALE_SMOOTH),0,0,null);
            log.info("Photo ID: {}. Creating thumbnail: {}", photo.getId(), thumbnailPath);
            photo.setThumbnailPath(fileName + thumbnailSuffix + "." + fileExt);
            ImageIO.write(buffer, fileExt, thumbnailFile);
            repo.save(photo);
        } catch (IOException e) {
            log.error("{} -- {} Error reading/writing thumbnail image: {}", LOG_PREFIX, CLASS_NAME, e.getMessage());
        }
    }

}
