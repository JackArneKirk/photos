package com.example.demo.model.DTO;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PersonMatchLikelihoodDTO implements Comparable<PersonMatchLikelihoodDTO>{
    private long id;
    private float faceMatch;

    @Override
    public int compareTo(PersonMatchLikelihoodDTO candidate) {
        if(candidate.faceMatch == this.faceMatch){
            return 0;
        }
        return this.faceMatch < candidate.faceMatch ? -1 : 1;
    }
}
