package com.example.demo.model;

import org.hibernate.annotations.Check;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"photo_id", "keyword_id"}), 
        name = "PhotoTag")
@Check(constraints = "keyword_id IS NOT NULL OR person_id IS NOT NULL")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PhotoTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "keyword_id")
    @ManyToOne(optional = true)
    private Keyword keyword;

    @JoinColumn(name = "person_id")
    @ManyToOne(optional = true)
    private Person person;

    @JoinColumn(name = "photo_id")
    @ManyToOne(optional = false)
    private Photo photo;

    @Column(name = "x")
    private float xNorm;
    @Column(name = "y")
    private float yNorm;
}
