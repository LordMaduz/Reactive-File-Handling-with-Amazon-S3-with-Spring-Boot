package com.example.demo.configuration;


import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@RequiredArgsConstructor
@Configuration
public class AwsS3Config {

    private final AwsProperties s3ConfigProperties;

    @Bean
    public S3AsyncClient s3AsyncClient() {
        return S3AsyncClient.create();
    }

    @Bean
    AwsCredentialsProvider awsCredentialsProvider() {
        if (StringUtils.isBlank(s3ConfigProperties.getAccessKey())) {
            return DefaultCredentialsProvider.create();
        } else {
            return () -> AwsBasicCredentials.create(
                    s3ConfigProperties.getAccessKey(),
                    s3ConfigProperties.getSecretKey());
        }
    }

}
