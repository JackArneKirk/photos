package com.example.demo.model.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CrsDTO {
    private String type = "name";
    private String properties;
}
