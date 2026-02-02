package com.example.demo.model;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    
    private String title;
    private long size;
    private LocalDateTime createdDate;
    private String fileName;
    private String thumbnailPath;
    private String folderPath;
    private String description;
    @OneToMany(mappedBy = "photo", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PhotoTag> tags;
    private Double longitude;
    private Double latitude;
    @OneToMany(mappedBy = "photo", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Face> faces;
}