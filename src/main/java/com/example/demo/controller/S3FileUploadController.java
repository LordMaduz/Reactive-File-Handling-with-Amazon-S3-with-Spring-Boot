package com.example.demo.controller;


import com.example.demo.common.FileUtils;
import com.example.demo.domain.SuccessResponse;
import com.example.demo.service.S3FileStorageService;
import com.example.demo.util.WebClientUtil;
import lombok.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
@RestController
@RequestMapping("/files")
@Validated
public class S3FileUploadController {

    private final S3FileStorageService fileStorageService;
    private final WebClientUtil webClientUtil;

    @PostMapping(value = "/upload/{path}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<SuccessResponse> upload(@RequestPart("file") Mono<FilePart> filePart, @PathVariable String path) {
        return filePart
                .map(file -> {
                    FileUtils.filePartValidator(file);
                    return file;
                })
                .flatMap(file -> fileStorageService.uploadObject(file, path))
                .map(fileResponse -> new SuccessResponse(fileResponse, "Upload successfully"));
    }

    @PostMapping(value = "/upload/file-storage/{path}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<Response> uploadToStorage(@RequestPart("file") Mono<FilePart> filePartMono, @PathVariable String path) {

        AtomicReference<Response> reference = new AtomicReference<>(new Response());
        List<Mono<?>> publisherList = new ArrayList<>();
        return filePartMono.flatMap(filePart -> {
            try {

                File file = File.createTempFile("Temp","File");
                Mono<byte[]> array = getByteArray(filePart);
                return array.flatMap(bytes -> {

                    try {
                        org.apache.commons.io.FileUtils.writeByteArrayToFile(file, bytes);
                        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
                        multipartBodyBuilder.part("key", "Test_Key");
                        multipartBodyBuilder.part("files", new FileSystemResource(file));

                        publisherList.add(webClientUtil.getResponse(multipartBodyBuilder).bodyToMono(ResponsePayload.class).doOnError(error -> {
                        }).map(response -> {
                            reference.set(new Response(response));
                            return response;
                        }));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return Mono.zip(publisherList, c -> reference);
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).then(Mono.fromCallable(reference::get));
    }

    @PostMapping(value = "/upload/many/{path}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<SuccessResponse> uploadMany(@RequestPart("file") Flux<FilePart> fileParts, @PathVariable(required = false) String path) {
        return fileParts.map(filePart -> {
                    FileUtils.filePartValidator(filePart);
                    return filePart;
                }).flatMap(file -> fileStorageService.uploadObject(file, path))
                .collectList().map((fileResponses) -> new SuccessResponse(fileResponses, "Upload successfully"));
    }

    @GetMapping(path = "/{fileKey}")
    public Mono<SuccessResponse> download(@PathVariable("fileKey") String fileKey) {

        return fileStorageService.getByteObject(fileKey)
                .map(objectKey -> new SuccessResponse(objectKey, "Object byte response"));
    }

    @DeleteMapping(path = "/{objectKey}")
    public Mono<SuccessResponse> deleteFile(@PathVariable("objectKey") String objectKey) {
        return fileStorageService.deleteObject(objectKey)
                .map(resp -> new SuccessResponse(null, MessageFormat.format("Object with key: {0} deleted successfully", objectKey)));
    }

    @GetMapping
    public Flux<SuccessResponse> getObject() {
        return fileStorageService.getObjects()
                .map(objectKey -> new SuccessResponse(objectKey, "Result found"));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class Response {
        private Object response;
    }

    Mono<byte[]> getByteArray(FilePart filePart) {
        return DataBufferUtils.join(filePart.content())
                .map(dataBuffer -> dataBuffer.asByteBuffer().array());
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public  static class ResponsePayload {
        private byte[] array;
    }
}
