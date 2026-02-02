package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.model.Person;
import com.example.demo.model.DTO.PersonEmbeddingDTO;

public interface PersonRepository extends JpaRepository<Person, Long> {
    
    @Query("SELECT p.id FROM Person p")
    public List<Long> getIDs();

    @Query("SELECT new com.example.demo.model.DTO.PersonEmbeddingDTO(p.id, p.aggregateEmbedding) FROM Person p")
    public List<PersonEmbeddingDTO> getIDsAndEmbeddings();
}
