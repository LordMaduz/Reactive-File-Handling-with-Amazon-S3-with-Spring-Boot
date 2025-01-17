package com.example.demo.camel.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/s3-pre-signed-url")
@Slf4j
public class FileURLController {

    private final String PRE_SIGNED_URL = "";

    @GetMapping
    public String getPreSignedURL(@RequestHeader(name = "Authorization") final String requestHeader) {
        log.info(requestHeader);
        return PRE_SIGNED_URL;
    }
}
