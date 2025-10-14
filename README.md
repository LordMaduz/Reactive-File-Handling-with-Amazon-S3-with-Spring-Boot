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
