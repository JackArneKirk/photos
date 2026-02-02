package com.example.demo.model.DTO;

import org.springframework.beans.factory.annotation.Value;

import com.example.demo.model.Photo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * For exposing data compatible with mapcn's cluster
 */
@Getter
@Setter
@NoArgsConstructor
public class GeoDataDTO {

    private String type = "Feature";
    private PropertyDTO properties;
    private GeometryDTO geometry;

    @Value("${file.upload-dir}")
    private String rootFolderPath;

    public GeoDataDTO(Photo photo) {
        properties = new PropertyDTO();
        properties.setPath(photo.getFileName());
        properties.setThumbnailPath(photo.getThumbnailPath());
        geometry = new GeometryDTO();
        geometry.setCoordinates(new double[] { photo.getLongitude(), photo.getLatitude() });
    }
}
