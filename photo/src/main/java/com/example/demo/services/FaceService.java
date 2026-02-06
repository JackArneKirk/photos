package com.example.demo.services;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.example.demo.model.Face;
import com.example.demo.model.Person;
import com.example.demo.model.Photo;
import com.example.demo.model.DTO.FaceTagDTO;

public interface FaceService {
    public CompletableFuture<List<FaceTagDTO>> detectFaces(Photo photo);
    public void createEmbedding(Photo photo);
    public float compareSimilarity(Face face1, Face face2);
    public float compareSimilarity(long id1, long id2);
    public Optional<Person> findPerson(long id);
    public void setName(long id, String name);
}
