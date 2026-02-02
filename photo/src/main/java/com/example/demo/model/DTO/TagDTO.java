package com.example.demo.model.DTO;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TagDTO {
    private Long keywordID;
    private Long photoID;
    private float xNorm;
    private float yNorm;
    private List<String> tagNames;

}
