package com.trade.util;

import org.reactivestreams.Publisher;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.net.URISyntaxException;

@Component
public class WebClientUtil {
    private final WebClient webClient = WebClient.create("http://localhost:8080/files/upload/file-storage");

    public WebClient.ResponseSpec getResponse(Publisher<FilePart> filePartPublisher, final String path) throws URISyntaxException {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        return webClient.post()
                .uri(url-> url
                        .path("/{path}")
                        .build(path))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromPublisher(filePartPublisher, FilePart.class))
                .retrieve();
    }
}
