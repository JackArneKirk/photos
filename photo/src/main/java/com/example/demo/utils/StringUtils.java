package com.example.demo.utils;

public class StringUtils {

    public static String determinePath(String filePath, String thumbnailSuffix, boolean getThumbnail) {
        if(filePath.split("\\.").length < 2){
            throw new IllegalArgumentException("Asset must have a file extension");
        }
        String fileName = filePath.split("\\.")[0];
        String fileExt = filePath.split("\\.")[1];
        return getThumbnail ? fileName + thumbnailSuffix + "." + fileExt : filePath;
    }
}
