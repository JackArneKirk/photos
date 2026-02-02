package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Face {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Lob
    private float[] embedding;
    @Column(name = "user_id")
    private long userID;
    @JoinColumn(name = "photo_id")
    @ManyToOne(optional = false)
    private Photo photo;
    @JoinColumn(name = "person_id")
    @ManyToOne(optional = true)
    private Person person;
}
