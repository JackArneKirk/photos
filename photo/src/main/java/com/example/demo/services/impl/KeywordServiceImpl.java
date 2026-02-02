package com.example.demo.services.impl;

import static com.example.demo.constant.Constant.LOG_PREFIX;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.demo.model.Keyword;
import com.example.demo.repository.KeywordRepository;
import com.example.demo.services.KeywordService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class KeywordServiceImpl implements KeywordService{

    private String CLASS_NAME = KeywordServiceImpl.class.getSimpleName();

    private KeywordRepository repo;

    public KeywordServiceImpl(KeywordRepository repo){
        this.repo = repo;
    }

    @Override
    public Keyword createKeyword(String name){
        Keyword keyToAdd = new Keyword();
        keyToAdd.setName(name);
        return repo.save(keyToAdd);
    }


    @Override
    public List<Long> createKeywords(Set<String> keywords) {
        List<Long> keywordIDs = new ArrayList<Long>();
        keywords.forEach(keywordName -> {
            Keyword keyword = new Keyword();
            keyword.setName(keywordName);
            keyword.setIsPerson(false);
            keyword = repo.save(keyword);
            keywordIDs.add(keyword.getId());
            log.info("{} -- {} Creating keyword: {}", LOG_PREFIX, CLASS_NAME, keywordName);

        });
        return keywordIDs;
    }

    @Override
    public List<String> getKeywords(long photoID) {
        List<Keyword> keys = repo.findKeywordsByPhoto(photoID);
        return keys.stream().map(key -> key.getName()).toList();
    } 
}
