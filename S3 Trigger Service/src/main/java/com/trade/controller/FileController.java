package com.trade.controller;


import com.trade.util.WebClientUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URISyntaxException;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class FileController {

    private final WebClientUtil webClientUtil;


    @PostMapping(value = "/upload/many", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<Response > uploadMany(@RequestPart("file") Mono<FilePart> fileParts, @RequestPart String path) throws URISyntaxException {
        return webClientUtil.getResponse(fileParts, path).bodyToMono(Response.class);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class Response{
        private  Object response;
    }
}
