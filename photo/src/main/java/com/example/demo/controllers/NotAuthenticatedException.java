package com.example.demo.controllers;

public class NotAuthenticatedException extends Exception {
    
    public NotAuthenticatedException(String message){
        super(message);
    }
}
