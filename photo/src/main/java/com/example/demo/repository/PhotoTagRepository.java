package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.Photo;
import com.example.demo.model.PhotoTag;
import com.example.demo.model.DTO.TagnameAndIdDTO;

public interface PhotoTagRepository extends JpaRepository<PhotoTag, Long> {

    @Query("""
            SELECT new com.example.demo.model.DTO.TagnameAndIdDTO(pt.id, COALESCE(k.name, p.name), pt.xNorm, pt.yNorm)
            FROM PhotoTag pt
            LEFT JOIN pt.keyword k
            LEFT JOIN pt.person p
            JOIN pt.photo photo
            WHERE photo.id = :photoID
                """)
    public List<TagnameAndIdDTO> getTagNames(@Param("photoID") long photoID);

    @Query("""
            SELECT p FROM PhotoTag pt JOIN pt.photo p WHERE pt.person.id = :personID
            """)
    public Optional<Photo> getPhotosByPerson(@Param("personID") long personID);

}
