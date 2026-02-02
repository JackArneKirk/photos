package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.Keyword;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {

    public Optional<Keyword> findFirstByName(String name);
    
    @Query("SELECT k FROM Photo p JOIN p.tags pt JOIN pt.keyword k WHERE p.id = :photoID")
    public List<Keyword> findKeywordsByPhoto(@Param("photoID" )long id);
}
