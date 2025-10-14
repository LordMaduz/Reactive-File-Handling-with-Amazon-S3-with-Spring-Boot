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
