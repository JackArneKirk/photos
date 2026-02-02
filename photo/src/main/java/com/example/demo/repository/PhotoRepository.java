package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.Photo;

public interface PhotoRepository extends JpaRepository<Photo, Long>{
    
    @Query("SELECT p FROM Photo p JOIN p.tags t JOIN t.keyword k WHERE k.name IN :keywords GROUP BY p HAVING COUNT(DISTINCT k.name) = :count")
    List<Photo> searchByKeyword(@Param("keywords") Set<String> keyword, @Param("count") long count);

    @Query("SELECT p FROM Photo p WHERE  p.createdDate > :date ORDER BY p.createdDate ASC LIMIT :limit OFFSET :offset")
    List<Photo> searchByDateBefore(@Param("date") LocalDateTime date, int limit, int offset);

    @Query("SELECT p FROM Photo p WHERE  p.createdDate > :date ORDER BY p.createdDate ASC LIMIT :limit OFFSET :offset")
    List<Photo> searchByDateAfter(@Param("date") LocalDateTime date, int limit, int offset);
}
