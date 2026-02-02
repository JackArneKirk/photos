package com.example.demo.model.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PersonEmbeddingDTO {
    private long id;
    private float[] centralEmbedding;

    public PersonEmbeddingDTO(long id, float[] centralEmbedding){
        this.id = id;
        this.centralEmbedding = centralEmbedding;
    }
}
