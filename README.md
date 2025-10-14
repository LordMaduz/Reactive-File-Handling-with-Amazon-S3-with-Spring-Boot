# Reactive AWS S3 File Upload with Spring Boot

> Reactive file upload and download service for AWS S3 using Spring WebFlux with multipart upload support and non-blocking I/O.

## Overview

A production-ready reactive microservice for handling file operations with AWS S3. Implements streaming multipart uploads for large files, reactive download, and seamless integration with external services via WebClient. Built with Spring WebFlux for high concurrency and non-blocking operations.

**Key Features:**
- Reactive multipart upload to AWS S3 with automatic chunking
- Non-blocking file streaming (no memory loading)
- Support for large files (GB+ sizes)
- Dynamic buffer management with backpressure handling
- WebClient integration for service-to-service communication
- OpenAPI/Swagger documentation
- Configurable file size limits and multipart chunk sizes

---

##  Architecture
<img width="1077" height="2069" alt="Mermaid Chart - Create complex, visual diagrams with text -2025-10-14-070136" src="https://github.com/user-attachments/assets/e3e23351-2a33-46cf-9b43-4978e6bdbe9a" />

## Tech Stack

| Category | Technologies |
|----------|-------------|
| **Core** | Java 21, Spring Boot 3.2.2|
| **Reactive** | Spring WebFlux, Project Reactor |
| **AWS** | AWS SDK for Java 2.23.14 (S3 Async Client) |
| **API Docs** | SpringDoc OpenAPI 3 |

## Getting Started
Prerequisites

```bash
- Java 21+
- Maven 3.8+
- AWS Account with S3 bucket
- AWS credentials configured
```

AWS Configuration

```bash
export AWS_ACCESS_KEY_ID=your_access_key
export AWS_SECRET_ACCESS_KEY=your_secret_key
export AWS_REGION=us-east-1
```

## Installation
### 1. Clone and Build
```bash
git clone <repository-url>
cd reactive-s3-upload
mvn clean install
```
### 2. Configure Application
application.yml:

```yaml
aws:
  access-key:                    # Optional if using AWS credentials file
  secret-key:                    # Optional if using AWS credentials file
  region: us-east-1
  s3-bucket-name: your-bucket-name
  multipart-min-part-size: 5242880   # 5MB minimum part size
  endpoint:                      # Optional: For localstack/minio

```

### 3. Run Main Service
```bash
mvn spring-boot:run
```
### 4. Run Optional Services (if needed)
S3 Trigger Service (Port 9070):

```bash
cd S3 Trigger Service
mvn spring-boot:run
```

File Storage Service (Port 8090):
```bash
cd WebClientTestService
mvn spring-boot:run
```
## Key Features

### S3 File Storage Service

Handles reactive S3 operations with multipart upload:
```java
@Service
@RequiredArgsConstructor
public class S3FileStorageServiceImpl implements S3FileStorageService {

    private final S3AsyncClient s3AsyncClient;
    private final AwsProperties s3ConfigProperties;

    @Override
    public Mono uploadObject(FilePart filePart, String path) {
        String key = path + "/" + filePart.filename();
        
        // Create multipart upload
        CompletableFuture multipartUpload = 
            s3AsyncClient.createMultipartUpload(
                CreateMultipartUploadRequest.builder()
                    .contentType(mediaType.toString())
                    .key(key)
                    .bucket(s3ConfigProperties.getS3BucketName())
                    .build());

        UploadStatus uploadStatus = new UploadStatus(contentType, key);

        return Mono.fromFuture(multipartUpload)
            .flatMapMany(response -> {
                uploadStatus.setUploadId(response.uploadId());
                return filePart.content();
            })
            // Buffer until chunk size reached
            .bufferUntil(dataBuffer -> {
                uploadStatus.addBuffered(dataBuffer.readableByteCount());
                if (uploadStatus.getBuffered() >= minPartSize) {
                    uploadStatus.setBuffered(0);
                    return true;
                }
                return false;
            })
            .map(FileUtils::dataBufferToByteBuffer)
            .flatMap(buffer -> uploadPartObject(uploadStatus, buffer))
            .onBackpressureBuffer()
            .reduce(uploadStatus, (status, part) -> {
                status.getCompletedParts().put(part.partNumber(), part);
                return status;
            })
            .flatMap(this::completeMultipartUpload)
            .map(response -> new FileResponse(filename, uploadId, location, type, eTag));
    }
}
```

### Buffer Management

Converts DataBuffer to ByteBuffer for S3 upload:
```java
public ByteBuffer dataBufferToByteBuffer(List buffers) {
    int partSize = 0;
    for(DataBuffer b : buffers) {
        partSize += b.readableByteCount();
    }

    ByteBuffer partData = ByteBuffer.allocate(partSize);
    buffers.forEach(buffer -> partData.put(buffer.toByteBuffer()));
    
    partData.rewind();
    return partData;
}
```

### WebFlux Configuration

Configures multipart file handling limits:
```java
@Configuration
@EnableWebFlux
public class WebConfiguration implements WebFluxConfigurer {
    
    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        var partReader = new DefaultPartHttpMessageReader();
        partReader.setMaxParts(3);
        partReader.setMaxDiskUsagePerPart(30L * 10000L * 1024L); // 307.2 MB
        
        MultipartHttpMessageReader multipartReader = 
            new MultipartHttpMessageReader(partReader);
        
        configurer.defaultCodecs().multipartReader(multipartReader);
        configurer.defaultCodecs().maxInMemorySize(512 * 1024); // 512 KB
    }
}
```

### AWS S3 Configuration
```java
@Configuration
@RequiredArgsConstructor
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
        }
        return () -> AwsBasicCredentials.create(
            s3ConfigProperties.getAccessKey(),
            s3ConfigProperties.getSecretKey());
    }
}
```

---


## Multipart Upload Flow
### How It Works

#### Initiate Multipart Upload: Create upload session with S3
- Stream File Content: Receive file chunks reactively
- Buffer Chunks: Accumulate data until minimum part size (5MB)
- Upload Parts: Send buffered chunks to S3 asynchronously
- Track Parts: Store completed part ETags
- Complete Upload: Finalize multipart upload with all parts

#### Upload Status Tracking
```java
public class UploadStatus {
    private String uploadId;           // S3 upload session ID
    private int partCounter;           // Current part number
    private int buffered;              // Bytes buffered
    private Map<Integer, CompletedPart> completedParts; // Completed uploads
    
    public int getAddedPartCounter() {
        return ++this.partCounter;
    }
    
    public void addBuffered(int buffered) {
        this.buffered += buffered;
    }
}
```

## Learn More

For detailed step-by-step guides and in-depth explanations, check out these related Medium articles:

## Related Articles

| Topic | Article Link |
|-------|-------------|
| **AWS S3 Reactive Multipart Uploads & Downloads** | [Download and Upload from/to AWS S3 with Reactive Spring and WebFlux](https://blog.stackademic.com/download-and-upload-from-to-aws-s3-with-reactive-spring-and-web-flux-ea37d1aff800) |(https://levelup.gitconnected.com/asynchronous-parallel-file-transfers-using-amazon-s3-transfer-manager-0da7fa5ff83f)) |
| **Asynchronous Streaming to S3** | [Stream data to Amazon S3 asynchronously](https://levelup.gitconnected.com/asynchronous-streaming-of-data-to-amazon-s3-c4f8e066fa9b) |
| **AWS SDK Best Practises** | [Download Large Files from Amazon S3 without loading them into memory](https://levelup.gitconnected.com/downloading-large-files-from-amazon-s3-without-loading-them-into-the-memory-41dfbf273dc4) |

**More Articles**: Visit my [Medium Profile](https://medium.com/@maduz.ruchira) for more Spring Boot and AWS tutorials.

---
