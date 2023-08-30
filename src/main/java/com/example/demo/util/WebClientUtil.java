package com.example.demo.util;

import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.net.URISyntaxException;

@Component
public class WebClientUtil {
    private final WebClient webClient = WebClient.create("http://localhost:8090/file-storage-service");

    public WebClient.ResponseSpec getResponse(MultipartBodyBuilder multipartBodyBuilder) throws URISyntaxException {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        return webClient.post()
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .retrieve();
    }
}
