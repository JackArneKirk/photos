package com.example.demo.model.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GeometryDTO {

    private String type = "Point";
    private double[] coordinates;

}
