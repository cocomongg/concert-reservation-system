# Sequence Diagram
<br/>

## 1. 사용자 대기열 토큰 발급
### 1-1. sequence diagram
```mermaid
sequenceDiagram
    participant Client
    participant API
    participant User
    participant Queue
    participant Database

    Client ->> API: 대기열 토큰 발급 요청 (userId)
    API ->> User: 사용자 정보 요청 (userId)
    User ->> Database: 사용자 정보 조회 요청 (userId)
    Database -->> User: 사용자 정보 반환
    User-->>API: 사용자 정보 반환
    alt 사용자가 존재할 경우
        API->>Queue: 대기열 등록 및 토큰 생성 요청
        Queue->>Database: 토큰 생성 및 대기열에 사용자 추가
        Database -->> Queue: 토큰 및 대기열 정보 반환
        Queue-->>API: 토큰 및 대기열 정보 반환
        API-->>Client: 토큰 및 대기열 정보 응답
    else 사용자가 존재하지 않을 경우
        API-->>Client: 오류 메시지 응답
    end
```
### 1-2. Description
todo
<br/>

## 2. 사용자 대기열 관련 polling
### 2-1. sequence diagram
```mermaid
sequenceDiagram
    participant Client
    participant API
    participant Queue
    participant Database
    loop polling
        Client->>API: 대기열 정보 요청 (seatId, tokenId)
        API->>Queue: 대기열 정보 검증 (tokenId)
        Queue ->> Database: 대기열 정보 조회 (tokenId)
        Database -->> Queue: 대기열 정보 반환
        Queue -->> API: 대기열 정보 및 유효성 검증 결과 반환
        alt 대기열 정보가 유효할 경우
            API ->> Queue: 대기열에서 client 토큰 앞의 대기자 수 요청
            Queue ->> Database: 대기열에서 client 토큰 앞의 대기자 수 조회
            Database -->> Queue: 앞의 대기자 수 반환
            Queue -->> API: 앞의 대기자 수 반환
            API -->> Client: 앞의 대기자 수 및 현재 대기열 정보 등 응답
        else 대기열 정보가 유효하지 않을 경우
            API -->> Client: 오류 메시지 응답
        end
    end
```
### 2-2. description
todo
<br/>

## 3. 예약 가능 날짜 조회
### 3-1. sequence diagram
```mermaid
sequenceDiagram
    participant Client
    participant API
    participant Queue
    participant Concert
    participant Database

    Client->>API: 예약 가능 날짜 목록 조회 요청 (concertId, tokenId)
    API->>Queue: 대기열 정보 검증 (tokenId)
    Queue ->> Database: 대기열 정보 조회 (tokenId)
    Database -->> Queue: 대기열 정보 반환
    Queue -->> API: 대기열 정보 유효성 검증 결과 반환
    alt 대기열 정보가 유효할 경우
        API ->> Concert: 예약 가능 날짜 목록 요청 (concertId)
        Concert ->> Database: 예약 가능 날짜 목록 조회 (concertId)
        Database -->> Concert: 예약 가능 날짜 목록 반환
        Concert -->> API: 예약 가능 날짜 목록 반환
        API -->> Client: 예약 가능 날짜 목록 응답
    else 대기열 정보가 유효하지 않을 경우
        API -->> Client: 오류 메시지 응답
    end
```
### 3-2. description
todo
<br/>

## 4. 예약 가능 좌석 조회
### 4-1. sequence diagram
```mermaid
sequenceDiagram
    participant Client
    participant API
    participant Queue
    participant Concert
    participant Database

    Client->>API: 예약 가능 좌석 목록 조회 요청 (concertId, scheduleId, tokenId)
    API->>Queue: 대기열 정보 검증 (tokenId)
    Queue ->> Database: 대기열 정보 조회 (tokenId)
    Database -->> Queue: 대기열 정보 반환
    Queue -->> API: 대기열 정보 유효성 검증 결과 반환
    alt 대기열 정보가 유효할 경우
        API ->> Concert: 예약 가능 좌석 목록 요청 (concertId, scheduleId)
        Concert ->> Database: 예약 가능 좌석 목록 조회 (concertId, scheduleId)
        Database -->> Concert: 예약 가능 좌석 목록 반환 (concertId, scheduleId)
        Concert -->> API: 예약 가능 좌석 목록 반환
        API -->> Client: 예약 가능 좌석 목록 응답
    else 대기열 정보가 유효하지 않을 경우
        API -->> Client: 오류 메시지 응답
    end
```
### 4-2. description
todo
<br/>

## 5. 좌석 예약 요청
### 5-1. sequence diagram
```mermaid
sequenceDiagram
    participant Client
    participant API
    participant Queue
    participant Concert
    participant Database

    Client->>API: 좌석 예약 요청 (seatId, tokenId)
    API->>Queue: 대기열 정보 검증 (tokenId)
    Queue ->> Database: 대기열 정보 조회 (tokenId)
    Database -->> Queue: 대기열 정보 반환
    Queue -->> API: 대기열 정보 유효성 검증 결과 반환
    alt 대기열 정보가 유효할 경우
        API ->> Concert: 좌석 정보 요청 (seatId)
        Concert ->> Database: 좌석 조회 (seatId)
        Database -->> Concert: 좌석 반환
        Concert -->> API: 좌석 정보 반환
        alt 예약 가능한 좌석일 경우
            API ->> Concert: 좌석 예약 요청
            Concert ->> Database: 예약 정보 저장 및 좌석 정보 업데이트
            Database -->> Concert: 예약 정보 및 좌석 정보 반환
            Concert -->> API: 예약 정보 및 좌석 정보 반환
            API -->> Client: 좌석 예약 성공 응답
        else 예약할 수 없는 좌석일 경우
            Concert -->> API: 좌석 예약 실패
            API -->> Client: 오류 메시지 응답
        end
    else 대기열 정보가 유효하지 않을 경우
        API -->> Client: 오류 메시지 응답
    end
```
### 5-2. description
todo
<br/>

## 6. 잔액 조회
### 6-1. sequence diagram
```mermaid
sequenceDiagram
    participant Client
    participant API
    participant Queue
    participant User
    participant Database

    Client->>API: 잔액 조회 요청 (userId, tokenId)
    API->>Queue: 대기열 정보 유효성 검증 (tokenId)
    Queue ->> Database: 대기열 정보 조회 (tokenId)
    Database -->> Queue: 대기열 정보 반환
    Queue -->> API: 대기열 정보 유효성 검증 결과 반환
    alt 대기열 정보가 유효할 경우
        API ->> User: 잔액 조회 요청 (userId)
        User ->> Database: 잔액 조회 (userId)
        Database -->> User: 잔액 반환
        User -->> API: 잔액 반환
        API -->> Client: 잔액 응답
    else 대기열 정보가 유효하지 않을 경우
        API -->> Client: 오류 메시지 응답
    end
```
### 6-2. description
todo
<br/>

## 7. 잔액 충전
### 7-1. sequence diagram
```mermaid
sequenceDiagram
    participant Client
    participant API
    participant Queue
    participant User
    participant Database

    Client->>API: 잔액 충전 요청 (userId, amount, tokenId)
    API->>Queue: 대기열 정보 유효성 검증 (tokenId)
    Queue ->> Database: 대기열 정보 조회 (tokenId)
    Database -->> Queue: 대기열 정보 반환
    Queue -->> API: 대기열 정보 유효성 검증 결과 반환
    alt 대기열 정보가 유효할 경우
        API ->> API: 충전 금액 유효성 검사
        alt 충전 금액이 정상일 경우
            API ->> User: 충전 요청 (amount, userId)
            User ->> Database: 잔액 조회 (userId)
            Database -->> User: 잔액 반환
            User ->> User: 잔액에 충전금액(amount) 더하기
            User ->> Database: 잔액 업데이트
            Database -->> User: 잔액 반환
            User -->> API: 잔액 반환
            API -->> Client: 충전된 잔액 응답
        else 충전 금액이 0이하일 경우
            API -->> Client: 오류 메시지 응답
        end

    else 대기열 정보가 유효하지 않을 경우
        API -->> Client: 오류 메시지 응답
    end
```
### 7-2. description
todo
<br/>

## 8. 결제
### 8-1. sequence diagram
```mermaid
sequenceDiagram
    participant Client
    participant API
    participant Queue
    participant Concert
    participant User
    participant Payment
    participant Database

    Client->>API: 결제 요청 (reservationId, tokenId)
    API->>Queue: 대기열 정보 유효성 검증 (tokenId)
    Queue ->> Database: 대기열 정보 조회 (tokenId)
    Database -->> Queue: 대기열 정보 반환
    Queue -->> API: 대기열 정보 유효성 검증 결과 반환
    alt 대기열 정보가 유효할 경우
        API ->> Concert: 예약 정보 요청
        Concert ->> Database: 예약 정보 및 좌석 정보 조회
        Database -->> Concert: 예약 정보 및 좌석 정보 반환
        Concert -->> Concert: 좌석 임시 배정 상태 만료 검증
        alt 좌석 임시 배정 상태 유지
            Concert -->> API: 예약 정보 및 좌석 정보 반환
            API -->> User: 사용자 잔액 정보 요청
            User ->> Database: 사용자 잔액 정보 조회
            Database -->> User: 사용자 잔액 정보 반환
            User -->> API: 잔액 정보 반환
            API ->> API: 결제 금액(예약 정보)과 잔액 비교
            alt 잔액이 충분할 경우
                API ->> Payment: 결제 완료 처리 요청
                Payment ->> Payment: 결제 성공 처리
                Payment ->> Database: 결제 및 결제 기록 저장
                Database -->> Payment: 결제 및 결제 기록 저장 성공
                Payment -->> API: 결제 완료
                API ->> User: 잔액 차감 요청
                User ->> Database: 잔액 업데이트
                Database -->> User: 잔액 업데이트 성공
                User -->> API: 잔액 업데이트 성공
                API ->> Concert: 예약 완료 처리
                Concert ->> Database: 예약 및 좌석 업데이트
                Database -->> Concert: 예약 및 좌석 업데이트 성공
                Concert -->> API: 예약 완료 처리 성공
                API ->> Queue: 대기열 만료 처리 요청
                Queue ->> Queue: 대기열 만료 처리
                Queue ->> Database: 대기열 업데이트
                Database -->> Queue: 대기열 업데이트 성공
                Queue -->> API: 대기열 만료 처리 성공
                API -->> Client: 결제 성공 응답
            else 잔액이 부족할 경우
                Payment -->> API: 결제 실패(잔액 부족) 반환
                API -->> Client: 결제 실패 응답
            end
        else 좌석 임시 배정 상태가 만료되었을 경우
            Concert -->> API: 결제 실패(좌석 점유 시간 만료)
            API -->> Client: 결제 실패 응답
        end
    else 대기열 정보가 유효하지 않을 경우
        API -->> Client: 오류 메시지 응답
    end
```
### 8-2. description
todo
<br/>