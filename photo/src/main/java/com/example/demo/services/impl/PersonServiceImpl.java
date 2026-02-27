package com.example.demo.services.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.model.Person;
import com.example.demo.model.Photo;
import com.example.demo.model.DTO.PersonPhotoDTO;
import com.example.demo.repository.PersonRepository;
import com.example.demo.repository.PhotoRepository;
import com.example.demo.repository.PhotoTagRepository;
import com.example.demo.services.PersonService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PersonServiceImpl implements PersonService {

    private PersonRepository personRepo;

    private PhotoTagRepository tagRepo;

    public PersonServiceImpl(PersonRepository personRepo, PhotoTagRepository tagRepo) {
        this.personRepo = personRepo;
        this.tagRepo = tagRepo;
    }

    @Override
    public List<PersonPhotoDTO> findUnnamedPeople() {
        List<PersonPhotoDTO> unnamedPeople = personRepo.findPeopleWithNoName()
                .stream()
                .map(p -> p.getId())
                .map(id -> {
                    log.info("fetching tags for:  {}", id);
                    Optional<Photo> photoOfPerson = tagRepo.getPhotosByPerson(id);
                    log.info("found photo? {}", photoOfPerson.isPresent());
                    if (photoOfPerson.isPresent()) {
                        return new PersonPhotoDTO(id, photoOfPerson.get().getThumbnailPath(),
                                photoOfPerson.get().getFileName());
                    }
                    return null;
                })
                .filter(dto -> dto != null)
                .toList();
        return unnamedPeople;
    }
}
