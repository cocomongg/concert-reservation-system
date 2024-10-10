# API 명세서

## 목차
1. [인증 정보]()
2. [API 목록]()
<br/>

## 1. 인증 정보
`대기열 토큰 발급 API` 와 `콘서트 목록 조회 API`를 제외한, <br/>
모든 API는  X-QUEUE-TOKEN 헤더에 대기열 토큰을 포함해야 한다

- 헤더 이름: X-QUEUE-TOKEN
- 위치: 헤더
<br/>

## 2. API 목록
## 대기열
### 유저 대기열 토큰 발급 API
- **설명**: userId를 통해 해당 유저를 대기열에 진입시키고, 대기열 토큰을 반환한다.

**Request**
- **URL**: `/api/v1/queues/token`
- **Http Method**: `POST`
- **Request Body**
```json
{
  "userId": integer
}
```
**Response**
- 성공 (http status code: 200)
```json
{
  "status": 200,
  "data": {
    "token": string,
    "order": integer,
    "queueStatus": string,
    "expiredAt": string (datetime)
  }
}
```
- 실패 (http status code: 404)
```json
{
  "code": "user_404_1",
  "message": "user not found"
}
```
<br/>

### 유저 대기열 상태 조회 API
**설명**: polling을 통해 token에 해당하는 대기열 상태 및 순번 반환한다.

**Request**
- **URL**: `/api/v1/queues/token`
- **Http Method**: `GET`
- **Http Header**: [X-QUEUE-TOKEN:  토큰 값]

**Response**
- 성공 (http status code: 200)
```json
{
  "status": 200,
  "data": {
    "order": integer,
    "remainingWaitingCount": integer,
    "queueStatus": string,
    "expiredAt": string (datetime)
  }
}
```
- 실패 - 존재하지 않은 토큰 (http status code: 404)
```json
{
  "code": "token_404_1",
  "message": "token not found"
}
```
- 실패 - 유효하지 않은 토큰 (http status code: 400)
```json
{
  "code": "token_400_1",
  "message": "invalid token"
}
```
<br/>

## 콘서트
### 콘서트 목록 API
**설명**: 콘서트 목록을 반환한다.

**Request**
- **URL**: `/api/v1/concerts`
- **Http Method**: `GET`

**Response**
- 성공 (http status code: 200)
```json
{
  "status": 200,
  "data": [
    {
      "concertId": integer,
      "concertTitle": string,
      "concertDescription": string,
      "createdAt": string (datetime)
    }
  ]
}
```
<br/>

### 예약 가능 날짜 조회 API
**설명**: concertId에 해당하는 콘서트의 예약 가능 날짜 목록 반환

**Request**
- **URL**: `/api/v1/concerts/{concertId}/schedules`
- **Http Method**: `GET`
- **Http Header**: [X-QUEUE-TOKEN:  토큰 값]
- **Path Variable**: `concertId` (integer)

**Response**
- 성공 (http status code: 200)
```json
{
  "status": 200,
  "data": [
    {
      "concertScheduleId": integer,
      "concertScheduledDate": string (date),
      "concertStartAt": string (datetime),
      "concertEndAt": string (datetime)
    }
  ]
}
```
- 실패 - 존재하지 않은 토큰 (http status code: 404)
```json
{
  "code": "token_404_1",
  "message": "token not found"
}
```
- 실패 - 유효하지 않은 토큰 (http status code: 400)
```json
{
  "code": "token_400_1",
  "message": "invalid token"
}
```
- 실패 - 존재하지 않은 콘서트 (http status code: 404)
```json
{
  "code": "concert_404_1",
  "message": "concert not found"
}
```
<br/>

### 예약 가능 좌석 조회 API
**설명**: 예약 가능한 좌석 목록 반환

**Request**
- **URL**: `/api/v1/concerts/{concertId}/schedules/{scheduleId}/seats`
- **Http Method**: `GET`
- **Http Header**: [X-QUEUE-TOKEN:  토큰 값]
- **Path Variable**: `concertId` (integer), `scheduleId` (integer)

**Response**
- 성공 (http status code: 200)
```json
{
  "status": 200,
  "data": [
    {
      "concertSeatId": integer,
      "seatNumber": integer,
      "priceAmount": integer
    }
  ]
}
```
- 실패 - 존재하지 않은 토큰 (http status code: 404)
```json
{
  "code": "token_404_1",
  "message": "token not found"
}
```
- 실패 - 유효하지 않은 토큰 (http status code: 400)
```json
{
  "code": "token_400_1",
  "message": "invalid token"
}
```
- 실패 - 존재하지 않은 콘서트 (http status code: 404)
```json
{
  "code": "concert_404_1",
  "message": "concert not found"
}
```
- 실패 - 존재하지 않은 콘서트 스케줄(http status code: 404)
```json
{
  "code": "concert_404_2",
  "message": "concert schedule not found"
}
```
<br/>

### 좌석 예약 요청 API
**설명**: 날짜와 좌석 정보를 입력받아 좌석을 예약 처리

**Request**
- **URL**: `/api/v1/concerts/{concertId}/schedules/{scheduleId}/reservation`
- **Http Method**: `POST`
- **Http Header**: [X-QUEUE-TOKEN:  토큰 값]
- **Path Variable**: `concertId` (integer), `scheduleId` (integer)
- **Request Body**
```json
{
  "userId": integer,
  "seatId": integer
}
```

**Response**
- 성공 (http status code: 200)
```json
{
  "status": 200,
  "data": {
    "reservationId": integer,
    "priceAmount": integer
  }
}
```
- 실패 - 존재하지 않은 토큰 (http status code: 404)
```json
{
  "code": "token_404_1",
  "message": "token not found"
}
```
- 실패 - 유효하지 않은 토큰 (http status code: 400)
```json
{
  "code": "token_400_1",
  "message": "invalid token"
}
```
- 실패 - 존재하지 않은 콘서트 (http status code: 404)
```json
{
  "code": "concert_404_1",
  "message": "concert not found"
}
```
- 실패 - 존재하지 않은 콘서트 스케줄(http status code: 404)
```json
{
  "code": "concert_404_2",
  "message": "concert schedule not found"
}
```
- 실패 - 존재하지 않은 콘서트 좌석(http status code: 404)
```json
{
  "code": "concert_404_3",
  "message": "concert seat not found"
}
```
- 실패 - 좌석 점유 실패(http status code: 400)
```json
{
  "code": "concert_400_1",
  "message": "reservation fail"
}
```
<br/>

## 유저 API
### 유저 잔액 조회 API
- **설명**: userId에 해당하는 유저의 잔액을 반환합니다.

**Request**
- **URL**: `/api/v1/users/{userId}/balances`
- **Http Method**: `GET`
- **Http Header**: [X-QUEUE-TOKEN:  토큰 값]
- **Path Variable**: `userId` (integer) 

**Response**
- 성공 (http status code: 200)
```json
{
  "status": 200,
  "data": {
    "balanceAmount": integer
  }
}
```
- 실패 (http status code: 404)
```json
{
  "code": "user_404_1",
  "message": "user not found"
}
```
- 실패 - 존재하지 않은 토큰 (http status code: 404)
```json
{
  "code": "token_404_1",
  "message": "token not found"
}
```
- 실패 - 유효하지 않은 토큰 (http status code: 400)
```json
{
  "code": "token_400_1",
  "message": "invalid token"
}
```
<br/>

### 유저 잔액 충전 API
**설명**: userId에 해당하는 유저의 잔액을 입력한 금액만큼 충전

**Request**
- **URL**: `/api/v1/users/{userId}/balances`
- **Http Method**: `POST`
- **Http Header**: [X-QUEUE-TOKEN:  토큰 값]
- **Path Variable**: `userId` (integer)
- **Request Body**
```json
{
  "amount": integer
}
```

**Response**
- 성공 (http status code: 200)
```json
{
  "status": 200,
  "data": {
    "amount": integer
  }
}
```
- 실패 (http status code: 404)
```json
{
  "code": "user_404_1",
  "message": "user not found"
}
```
- 실패 - 존재하지 않은 토큰 (http status code: 404)
```json
{
  "code": "token_404_1",
  "message": "token not found"
}
```
- 실패 - 유효하지 않은 토큰 (http status code: 400)
```json
{
  "code": "token_400_1",
  "message": "invalid token"
}
```
<br/>

## 결제 API
### 결제 처리 API
- **설명**: 콘서트 예약 ID를 통해 해당 예약에 대한 결제를 처리

**Request**
- **URL**: `/api/v1/payments`
- **Http Method**: `POST`
- **Http Header**: [X-QUEUE-TOKEN:  토큰 값]
- **Request Body**
```json
{
  "reservationId": integer
}
```

**Response**
- 성공 (http status code: 200)
```json
{
  "status": 200,
  "data": {
    "concertId": integer,
    "concertTitle": string,
    "seatNumber": integer,
    "paymentId": integer,
    "paidAmount": integer
  }
}
```
- 실패 - 예약 내역 존재하지 않음 (http status code: 404)
```json
{
  "code": "concert_404_4",
  "message": "reservation not found"
}
```
- 실패 - 잔액 부족 (http status code: 400)
```json
{
  "code": "payment_400_1",
  "message": "insufficient balance"
}
```
- 실패 - 좌석 임시 상태 만료 (http status code: 400)
```json
{
  "code": "concert_400_2",
  "message": "Temporary seat assignment expires"
}
```
- 실패 - 존재하지 않은 토큰 (http status code: 404)
```json
{
  "code": "token_404_1",
  "message": "token not found"
}
```
- 실패 - 유효하지 않은 토큰 (http status code: 400)
```json
{
  "code": "token_400_1",
  "message": "invalid token"
}
```
<br/>