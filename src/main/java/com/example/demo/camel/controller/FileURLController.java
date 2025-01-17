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

    private final String PRE_SIGNED_URL = "http://localhost:8080/s3-pre-signed-url/test";

    @GetMapping
    public String getPreSignedURL(@RequestHeader(name = "Authorization") final String requestHeader) {
        log.info(requestHeader);
        return PRE_SIGNED_URL;
    }

    @GetMapping("/test")
    public String test() {
        return """
            EFFECTIVE_DATE|FROM_CURRENCY|TO_CURRENCY|RATE_TYPE|RATE_MULTIPLIER|RATE_DIV|LAST_UPDATED_TIME|ORIGIN_COUNTRY|DATE_BUSNIESS_UNIT|BUSINESS_DATE\n
            2025-01-17|USD|USD|PSGL|1.0|2|2025-01-17|SG|UNIT|2025-01-17\n
            2025-01-17|USD|USD|PSGL|1.5|1|2025-01-17|SG|UNIT|2025-01-17\n
            2025-01-17|USD|USD|PSGL|1.6|0|2025-01-17|SG|UNIT|2025-01-17
            """;
    }
}
