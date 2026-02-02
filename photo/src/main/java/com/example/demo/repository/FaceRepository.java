package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.Face;

public interface FaceRepository extends JpaRepository<Face, Long> {

    @Query("SELECT f.embedding FROM Face f WHERE f.person.id = :personID")
    public List<float[]> findEmbeddingsByPerson(@Param("personID") long personID);

}
