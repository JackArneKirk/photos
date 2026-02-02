package com.example.demo.model;

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
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"photo_id", "keyword_id", "x", "y"}), name = "PhotoTag")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PhotoTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "keyword_id")
    @ManyToOne(optional = false)
    private Keyword keyword;

    @JoinColumn(name = "photo_id")
    @ManyToOne(optional = false)
    private Photo photo;

    @Column(name = "x")
    private float xNorm;
    @Column(name = "y")
    private float yNorm;

}
