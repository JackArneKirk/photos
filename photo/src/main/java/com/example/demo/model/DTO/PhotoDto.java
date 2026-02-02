package com.example.demo.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PhotoDto {

    private long id;
    private String filePath;
    private String thumbnailPath;
    private String folderPath;
    
}
