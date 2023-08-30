package com.example.demo.domain;

import java.time.Instant;

public record S3Object(String key, Instant lastModified, String eTag, Long size) {}
