package com.example.demo.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FaceDTO implements Comparable<FaceDTO>{
    private long faceID;
    private double likelihood;

    @Override
    public int compareTo(FaceDTO face) {
        if(face.likelihood == this.likelihood){
            return 0;
        }
        return this.likelihood < face.likelihood ? -1 : 1;
    }
}
