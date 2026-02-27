package com.example.demo.services;

import java.util.List;

import com.example.demo.model.DTO.PersonPhotoDTO;

public interface PersonService {
    public List<PersonPhotoDTO> findUnnamedPeople();
}
