package com.example.file.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/file-storage-service")
public class FileStorageController {

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponsePayload getResponse(@ModelAttribute RequestPayload requestPayload) throws IOException {
        return new ResponsePayload(requestPayload.files.getBytes());
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public  static class RequestPayload {
        private String key;
        private MultipartFile files;
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public  static class ResponsePayload {
        private byte[] array;
    }

}
