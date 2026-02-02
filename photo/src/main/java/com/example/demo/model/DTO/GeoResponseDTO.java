package com.example.demo.model.DTO;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GeoResponseDTO {

    private String type = "FeatureCollection";
    private List<GeoDataDTO> features;
    private CrsDTO crs;

    public GeoResponseDTO(List<GeoDataDTO> features){
        this.features = features;
        this.crs = new CrsDTO();
    }
}
