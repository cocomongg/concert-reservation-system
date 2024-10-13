# 프로젝트 패키지 구조 및 기술 스택

## 패키지 구조
- Clean + Layered Architecture 채택
- Persistence Layer나 Presentation Layer가 변경되어도 Business Layer에 영향이 가지 않음

```shell
src/main/java/io/hhplus/concert
├── application
│   └── <domain>
│       ├── facade
│       └── dto
│
├── domain
│   └── <domain>
│       ├── repository
│       ├── service
│       ├── exception
│       └── entity //model
│
├── infrastructure
│   └── db
│       └── <domain>
│           ├── <entity>JpaRepository.java
│           └── impl
│       
└── interfaces
    └── api
        ├── common
        │   └── response
        │       ├── ApiErrorResponse.java
        │       └── ApiResponse.java
        │
        ├── config
        │   └── OpenApiConfig.java
        │
        └── <domain>
            ├── <domain>Controller.java
            ├── <domain>ControllerDocs.java
            ├── <domain>Request.java
            └── <domain>Response.java

```

## 기술 스택
### 1. 프로그래밍 언어
- Java 17

### 2. 프레임워크
- Spring Boot 3.3.4
- Spring Data JPA
- Spring Validation
- Springdoc OpenAPI 2.5.0

### 3. 빌드 도구
- Gradle 8.x

### 4. 데이터베이스
- MySQL 8.x