package com.example.demo.services;

import com.example.demo.model.Face;
import com.example.demo.model.Photo;

public interface FaceService {
    public void detectFace(Photo photo);
    public void createEmbedding(Photo photo);
    public float compareSimilarity(Face face1, Face face2);
    public float compareSimilarity(long id1, long id2);
}
