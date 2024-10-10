# Sequence Diagram
<br/>

## 0. 도메인 정의
- User
  - 사용자 정보
  - 사용자 잔액 정보
- Queue
  - 대기열 정보 (토큰)
- Concert
  - 콘서트 정보
  - 콘서트 스케줄 정보
  - 콘서트 좌석 정보
  - 콘서트 예약 정보
- Payment
  - 결제 정보


## 1. 사용자 대기열 토큰 발급
```mermaid
sequenceDiagram
    autonumber
Actor U as 사용자
    participant API
    participant User
    participant Queue
    participant Database

    U ->> API: 대기열 토큰 발급 요청 (userId)
    API ->> User: 사용자 정보 요청 (userId)
    activate User
    User ->> Database: 사용자 정보 조회 요청 (userId)
    Database -->> User: 사용자 정보 반환
    User-->>API: 사용자 정보 반환
    deactivate User
    alt 사용자가 존재하지 않을 경우
        API-->>U: 오류 메시지 응답
    else 사용자가 존재할 경우
        activate Queue
        API->>Queue: 대기열 토큰 정보 요청 (userId)
        Queue->>Database: 대기열 토큰 조회 요청 (userId)
        Database -->> Queue: 대기열 토큰 반환
        opt 대기열 토큰 존재하지 않는 경우
            Queue ->> Queue: 대기열 토큰 생성
            Queue ->> Database: 대기열 토큰 저장
            Database -->> Queue: 대기열 토큰 반환
        end
        Queue-->>API: 대기열 토큰 반환
        deactivate Queue
        API-->>U: 토큰 및 대기열 정보 응답
    end
```

<br/>

## 2. 사용자 대기열 관련 polling
```mermaid
sequenceDiagram
    autonumber
    Actor U as 사용자
    participant API
    participant Queue
    participant Database
    loop polling
        U->>API: 대기열 토큰 정보 요청 (tokenId)
        API->>Queue: 대기열 토큰 정보 요청 (tokenId)
        activate Queue
        Queue ->> Database: 대기열 토큰 정보 조회 (tokenId)
        Database -->> Queue: 대기열 토큰 정보 반환
        Queue -->> API: 대기열 토큰 정보 반환
        API ->> Queue: 대기열 토큰 유효성 검증 요청
        Queue ->> Queue: 대기열 토큰 유효성 검증
        deactivate Queue
        opt 대기열 토큰이 유효하지 않을 경우
            Queue -->> API: 예외
            API -->> U: 오류 메시지 응답
        end
        API ->> Queue: 대기열 토큰 활성화 여부 요청
        activate Queue
        Queue ->> Database: 대기열 활성화 상태 수 조회,<br/>현재 사용자 앞 대기상태 수 조회
        Database -->> Queue: 조회 결과 반환
        Queue ->> Queue: 현재 대기열 토큰 활성화 가능여부 계산
        opt 대기열 토큰 활성 가능
            Queue -->> Database: 대기열 토큰 활성화 업데이트
            Database -->> Queue: 대기열 토큰 정보 반환
        end
        Queue -->> API: 대기열 토큰 정보 반환(순번, 앞에 대기 상태 수 등)
        deactivate Queue
        API -->> U: 대기열 토큰 정보 반환
    end

```

<br/>

## 3. 예약 가능 날짜 조회
```mermaid
sequenceDiagram
    autonumber
    Actor U as 사용자
    participant API
    participant Queue
    participant Concert
    participant Database

    U->>API: 예약 가능 날짜 목록 조회 요청 (concertId, tokenId)
    API->>Queue: 대기열 토큰 정보 요청 (tokenId)
    activate Queue
    Queue ->> Database: 대기열 토큰 정보 조회 (tokenId)
    Database -->> Queue: 대기열 토큰 정보 반환
    Queue -->> API: 대기열 토큰 정보 반환
    API ->> Queue: 대기열 토큰 유효성 검증 요청
    Queue ->> Queue: 대기열 토큰 유효성 검증
    opt 대기열 토큰이 유효하지 않을 경우
        Queue -->> API: 예외
        deactivate Queue
        API -->> U: 오류 메시지 응답
    end
    API ->> Concert: 예약 가능 날짜 목록 요청 (concertId)
    activate Concert
    Concert ->> Database: 예약 가능 날짜 목록 조회 (concertId)
    Database -->> Concert: 예약 가능 날짜 목록 반환
    Concert -->> API: 예약 가능 날짜 목록 반환
    deactivate Concert
    API -->> U: 예약 가능 날짜 목록 응답
```
<br/>

## 4. 예약 가능 좌석 조회
### 4-1. sequence diagram
```mermaid
sequenceDiagram
    autonumber
    Actor U as 사용자
    participant API
    participant Queue
    participant Concert
    participant Database

    U->>API: 예약 가능 좌석 목록 조회 요청 (concertId, scheduleId, tokenId)
    API->>Queue: 대기열 토큰 정보 요청 (tokenId)
    activate Queue
    Queue ->> Database: 대기열 토큰 정보 조회 (tokenId)
    Database -->> Queue: 대기열 토큰 정보 반환
    Queue -->> API: 대기열 토큰 정보 반환
    API ->> Queue: 대기열 토큰 유효성 검증 요청
    Queue ->> Queue: 대기열 토큰 유효성 검증
    opt 대기열 토큰이 유효하지 않을 경우
        Queue -->> API: 예외
        deactivate Queue
        API -->> U: 오류 메시지 응답
    end
    API ->> Concert: 예약 가능 좌석 목록 요청 (concertId, scheduleId)
    activate Concert
    Concert ->> Database: 예약 가능 좌석 목록 조회 (concertId, scheduleId)
    Database -->> Concert: 예약 가능 좌석 목록 반환 (concertId, scheduleId)
    Concert -->> API: 예약 가능 좌석 목록 반환
    deactivate Concert
    API -->> U: 예약 가능 좌석 목록 응답
```

<br/>

## 5. 좌석 예약 요청
```mermaid
sequenceDiagram
    autonumber
    Actor U as 사용자
    participant API
    participant Queue
    participant Concert
    participant Database

    U->>API: 좌석 예약 요청 (seatId, tokenId)
    API->>Queue: 대기열 토큰 정보 요청 (tokenId)
    activate Queue
    Queue ->> Database: 대기열 토큰 정보 조회 (tokenId)
    Database -->> Queue: 대기열 토큰 정보 반환
    Queue -->> API: 대기열 토큰 정보 반환
    API ->> Queue: 대기열 토큰 유효성 검증
    Queue ->> Queue: 대기열 토큰 유효성 검증
    opt 대기열 토큰이 유효하지 않을 경우
        Queue -->> API: 예외
        deactivate Queue
        API -->> U: 오류 메시지 응답
    end
    
    API ->> Concert: 좌석 정보 요청 (seatId)
    activate Concert
    Concert ->> Database: 좌석 조회 (seatId)
    Database -->> Concert: 좌석 반환
    Concert -->> API: 좌석 정보 반환
    deactivate Concert
    API ->> Concert: 좌석 임시 배정 여부 확인 요청
    Concert ->> Concert: 좌석 상태 확인
    alt 임시 배정되지 않은 상태
        Concert ->> Concert: 예약 정보 생성 및 좌석 업데이트
        Concert ->> Database: 예약 정보 저장 및 좌석 업데이트
    else 임시 배정된 상태
        Concert ->> Concert: 조회된 좌석정보의 임시예약일시와 <br/>요청한 일시를 비교하여 예약 가능 여부 판단
        opt 좌석 예약 불가
            Concert -->> API: 예외
            API -->> U: 좌석 예약 실패 응답
        end
        Concert ->> Concert: 예약 정보 생성 및 좌석 업데이트
        Concert ->> Database: 예약 정보 저장 및 좌석 업데이트
    end
    Database -->> Concert: 예약 정보 및 좌석 정보 반환
    Concert -->> API: 예약 정보 및 좌석 정보 반환
    API -->> U: 좌석 예약 성공 응답

```
<br/>

## 6. 잔액 조회
```mermaid
sequenceDiagram
    autonumber
    Actor U as 사용자
    participant API
    participant Queue
    participant User
    participant Database
    
    U->>API: 잔액 조회 요청 (userId, tokenId)
    API->>Queue: 대기열 토큰 정보 요청 (tokenId)
    activate Queue
    Queue ->> Database: 대기열 토큰 정보 조회 (tokenId)
    Database -->> Queue: 대기열 토큰 정보 반환
    Queue -->> API: 대기열 토큰 정보 반환
    API ->> Queue: 대기열 토큰 유효성 검증
    Queue ->> Queue: 대기열 토큰 유효성 검증
    opt 대기열 토큰이 유효하지 않을 경우
        Queue -->> API: 예외
        deactivate Queue
        API -->> U: 오류 메시지 응답
    end
    API ->> User: 잔액 조회 요청 (userId)
    activate User
    User ->> Database: 잔액 조회 (userId)
    Database -->> User: 잔액 반환
    User -->> API: 잔액 반환
    deactivate User
    API -->> U: 잔액 응답
```
<br/>

## 7. 잔액 충전
```mermaid
sequenceDiagram
    autonumber
    Actor U as 사용자
    participant API
    participant Queue
    participant User
    participant Database

    U->>API: 잔액 충전 요청 (userId, amount, tokenId)
    API->>Queue: 대기열 토큰 정보 요청 (tokenId)
    activate Queue
    Queue ->> Database: 대기열 토큰 정보 조회 (tokenId)
    Database -->> Queue: 대기열 토큰 정보 반환
    Queue -->> API: 대기열 토큰 정보 반환
    API ->> Queue: 대기열 토큰 유효성 검증
    Queue ->> Queue: 대기열 토큰 유효성 검증
    opt 대기열 토큰이 유효하지 않을 경우
        Queue -->> API: 예외
        deactivate Queue
        API -->> U: 오류 메시지 응답
    end
    API ->> API: 충전 금액 유효성 검사
    opt 충전 금액이 0 이하일 경우
        API -->> U: 오류 메시지 응답
    end
    API ->> User: 충전 요청 (amount, userId)
    activate User
    User ->> Database: 잔액 조회 (userId)
    Database -->> User: 잔액 반환
    User ->> User: 잔액에 충전금액(amount) 더하기
    User ->> Database: 잔액 업데이트
    Database -->> User: 잔액 반환
    User -->> API: 잔액 반환
    deactivate User
    API -->> U: 충전된 잔액 응답
```
<br/>

## 8. 결제
```mermaid
sequenceDiagram
    autonumber
    Actor U as 사용자
    participant API
    participant Queue
    participant Concert
    participant User
    participant Payment
    participant Database

    U->>API: 결제 요청 (reservationId, tokenId)
    API->>Queue: 대기열 토큰 정보 요청 (tokenId)
    activate Queue
    Queue ->> Database: 대기열 토큰 정보 조회 (tokenId)
    Database -->> Queue: 대기열 토큰 정보 반환
    Queue -->> API: 대기열 토큰 정보 반환
    API ->> Queue: 대기열 토큰 유효성 검증
    Queue ->> Queue: 대기열 토큰 유효성 검증
    opt 대기열 토큰이 유효하지 않을 경우
        Queue -->> API: 예외
        deactivate Queue
        API -->> U: 오류 메시지 응답
    end
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
            API -->> U: 결제 성공 응답
        else 잔액이 부족할 경우
            Payment -->> API: 결제 실패(잔액 부족) 반환
            API -->> U: 결제 실패 응답
        end
    else 좌석 임시 배정 상태가 만료되었을 경우
        Concert -->> API: 결제 실패(좌석 점유 시간 만료)
        API -->> U: 결제 실패 응답
    end
```
<br/>