package com.example.demo.services;

import java.util.List;
import java.util.Set;

import com.example.demo.model.Keyword;

public interface KeywordService {
    
        public Keyword createKeyword(String tagName);
        public List<Long> createKeywords(Set<String> tagNames);
        public List<String> getKeywords(long photoID);

}
