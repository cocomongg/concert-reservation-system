# 쿼리 성능 개선 보고서
## 목차
### 1. 쿼리 성능 개선 방법
### 2. 인덱스란?
### 3. 인덱스  생성 기준
### 4. 인덱스 사용 전/후 성능 비교 분석
### 5. 결론
<br/>

## 1. 쿼리 성능 개선 방법
쿼리의 성능(속도)를 향상 시킬 수 있는 방법에는 아래와 같이 여러가지 방법이 있다. 

1.  select 시 필요한 컬럼만 조회
2.  distinct의 사용 지양
3.  복잡한 쿼리에 대한 쿼리 튜닝 (subquery 제거 등)
4.  **인덱스 생성 및 활용**
5.  limit(paging) 등을 활용하여 조회 데이터 수 제한 
<br/>

이번 글에서는 **인덱스**를 통해 데이터 조회 속도를 개선하는 방법에 대해 알아보려고 한다.
<br/><br/>

## 2. 인덱스란?
책에서 원하는 내용을 찾을 때 목차 혹은 색인을 통해 빠르게 찾을 수 있다. 데이터베이스에서의 인덱스는 책의 목차, 색인처럼 원하는 내용(데이터)을 쉽고 빠르게 찾을 수 있게 도와준다.

인덱스는 데이터의 조회 속도를 향상 시킨다는 장점이 있지만, 아래와 같은 단점도 존재한다.
-   인덱스를 관리하기 위한 데이터베이스의 공간이 추가적으로 필요하다.
-   인덱스가 적용된 테이블에 대한 데이터 변경작업(INSERT, UPDATE, DELETE) 시 인덱스에 대한 데이터 재정렬 작업이 발생하여, 성능 저하 문제가 발생할 수 있다.
<br/>

## 3. 인덱스  생성 기준

아래와 같은 기준을 통해 인덱스를 생성하면 위에서 언급한 단점들을 피하고, 인덱스의 효율성을 극대화할 수 있다.

**Cardinality가 높은(중복도가 낮은) 컬럼에 대한 인덱스를 생성한다.**
-   인덱스로 조회 성능을 개선시키려면, 생성한 인덱스로 최대한 많은 데이터를 걸러내어야 한다. 예를 들어 성별 컬럼을 기준으로 인덱스를 생성하면 50%밖에 걸러내지 못한다. 하지만 주민번호, 휴대폰 번호으로 인덱스를 생성할 경우 해당 값들은 중복도가 낮은 값들이기에 데이터의 대부분을 걸러낼 수 있어, 인덱스의 효율을 극대화할 수 있다.

**WHERE 절에 자주 사용되는 컬럼에 인덱스를 생성한다.**
-  WHERE 절에 사용되는 열에 해당하는 인덱스를 통해 테이블 탐색 범위가 정해지기 때문에, WHERE 절에 자주 사용되지 않는 열에 인덱스를 설정했다면 해당 인덱스는 거의 사용이 되지 않기에 자주 사용되는 열에 인덱스를 생성하는 것이 좋다.

**자주 변경되거나 삽입되는 테이블에 대해서는 인덱스 생성을 고려한다.**
-   인덱스를 적용하게 되면 해당 인덱스를 기준으로 데이터를 정렬하기 때문에, 데이터가 삽입되거나 변경될 때 다시 재정렬이 이뤄지고, 재정렬에 인해 성능이 저하될 수 있다. 따라서, 데이터 변경이 잦은 테이블에 대해서는 인덱스 생성을 고려해야 한다.
<br/>

## 4. 인덱스 적용 전/후 성능 비교 분석
인덱스 생성 기준에 부합하는 쿼리(테이블)에 인덱스를 적용해보고, 인덱스 적용 전/후 성능 비교를 진행해보자.
<br/>

### 테스트 환경
정확한 테스트를 위해 AWS RDS로 진행

-   instance class: db.t4g.micro
-   cpu: 2 core
-   memory: RAM 1GB
-   mysql version: 8.0.39
-   테스트 데이터 수 : 총 1,000만건
<br/>

### 테이블 목록
<details>
  <summary>더보기</summary>
  
```sql
  
create table concert  
(  
    id          bigint auto_increment comment 'primary key'  
        primary key,  
    title       varchar(255) null comment '콘서트 제목',  
    description varchar(255) null comment '콘서트 설명',  
    created_at  datetime(6)  null comment '생성일시',  
    updated_at  datetime(6)  null comment '수정일시'  
);  
  

create table concert_reservation  
(  
    id              bigint auto_increment comment 'primary key'  
        primary key,  
    concert_seat_id bigint      null comment 'concert seat pk',  
    member_id       bigint      null comment 'member pk',  
    status          varchar(20) null comment '예약 상태 (COMPLETED: 예약 완료, PENDING: 예약 대기)',  
    reserved_at     datetime(6) null comment '예약 일시',  
    created_at      datetime(6) null comment '생성일시',  
    updated_at      datetime(6) null comment '수정일시'  
);  
  
create table concert_schedule  
(  
    id           bigint auto_increment comment 'primary key'  
        primary key,  
    concert_id   bigint      null comment 'concert pk',  
    scheduled_at datetime(6) null comment '콘서트 예약 가능 일시',  
    start_at     datetime(6) null comment '콘서트 시작 일시',  
    end_at       datetime(6) null comment '콘서트 종료 일시',  
    created_at   datetime(6) null comment '생성일시',  
    updated_at   datetime(6) null comment '수정일시'  
);  
  
create table concert_seat  
(  
    id                  bigint auto_increment comment 'primary key'  
        primary key,  
    concert_schedule_id bigint      null comment 'concert_schedule pk',  
    seat_number         int         null comment '좌석 번호',  
    price_amount        int         null comment '좌석 가격',  
    status              varchar(20) null comment '좌석 상태 (AVAILABLE: 예약가능, RESERVED_COMPLETE: 예약 완료)',  
    temp_reserved_at    datetime(6) null comment '임시 배정 일시',  
    reserved_at         datetime(6) null comment '예약 일시',  
    version             bigint      null comment '낙관적 락을 위한 식별자',  
    created_at          datetime(6) null comment '생성일시',  
    updated_at          datetime(6) null comment '수정일시'  
);  
  
create table member  
(  
    id         bigint auto_increment comment 'primary key'  
        primary key,  
    email      varchar(255) null comment '사용자 이메일',  
    name       varchar(255) null comment '사용자 이름',  
    created_at datetime(6)  null comment '생성일시',  
    updated_at datetime(6)  null comment '수정일시'  
);  
  
create table member\_point  
(  
    id           bigint auto_increment comment 'primary key'  
        primary key,  
    member_id    bigint      null comment 'member pk',  
    point_amount int         null comment '잔여 포인트',  
    created_at   datetime(6) null comment '생성일시',  
    updated_at   datetime(6) null comment '수정일시'  
);  
  
create table payment  
(  
    id             bigint auto_increment comment 'primary key'  
        primary key,  
    member_id      bigint      null comment 'member pk',  
    reservation_id bigint      null comment 'reservation pk',  
    paid_amount    int         null comment '결제 금액',  
    status         varchar(20) null comment '결제 상태 (PAID: 결제 완료, REFUND: 환불)',  
    paid_at        datetime(6) null comment '결제 일시',  
    created_at     datetime(6) null comment '생성일시',  
    updated_at     datetime(6) null comment '수정일시'  
);  
  
create table payment_history  
(  
    id         bigint auto_increment comment 'primary key'  
        primary key,  
    payment_id bigint      null comment 'payment pk',  
    amount     int         null comment '금액',  
    status     varchar(20) null comment '결제 상태 (PAID: 결제, REFUND: 환불)',  
    created_at datetime(6) null comment '생성일시'  
);  
```
</details>
<br/>

### 4-1. 사용자 포인트 조회
- **기능**
  - 사용자에 해당하는 포인트 데이터를 조회한다.
<br/>
  
- **실행 쿼리**
  ```sql
  # 총 1,000만개의 데이터, 서로 다른 member_id 총 1,000만개
  select
    *
  from
    member_point
  where
    member_id = 1;
  ```
<br/>

- **인덱스가 필요한 이유**
  - ```member_point``` 테이블은 생성/수정 보다 조회가 빈번히 일어나고, 사용자가 늘어날 수록 데이터가 많아지기에 성능 저하 원인이 될 수 있다.
  - 결제 진행 또는 사용자 포인트 조회 기능에서 자주 조회되는 쿼리다.
<br/>

- **인덱스 적용**
  - 인덱스 적용 컬럼: ```member_id```
    - ```member```와 ```member_point```는 1대1 관계이기 때문에 ```member_id```의 중복도는 매우 낮다.
    - WHERE절에 자주 사용되는 조건이다.
    - ```member_id```에 중복이 발생하지 않아야 되기 때문에 unique index로 생성한다.
  - 인덱스 생성 쿼리
    ```sql
    create unique index member_point_member_id_uindex on member_point (member_id);
    ``` 
<br/>

- **인덱스 적용 전 테스트 결과**
  - **실행 시간 평균: 10s 113ms**
  - 실행 계획 (explain)

    | id | select\_type | table | partitions | type | possible\_keys | key | key\_len | ref | rows | filtered | Extra |
    | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
    | 1 | SIMPLE | member\_point | NULL | ALL | NULL | NULL | NULL | NULL | 9714742 | 10 | Using where |
  - 실행 계획 분석 (explain analyze)

    | EXPLAIN |
    | :--- |
    | -&gt; Filter: \(member\_point.member\_id = 10000\)  \(cost=998978 rows=971474\) \(actual time=18.9..9800 rows=1 loops=1\)<br/>    -&gt; Table scan on member\_point  \(cost=998978 rows=9.71e+6\) \(actual time=0.774..9207 rows=10e+6 loops=1\)<br/> |
  - 요약
    - 실행계획을 확인하면, 테이블의 풀 스캔이 발생하고(```type: ALL```), 약 9,714,742 행(```rows: 9714742```)을 탐색할 수 있음을 확인할 수 있다.
    - 또한 WHERE 조건으로 필터링(```filtered: 10```)이 거의 되지 않을 것이라고 볼 수 있다. 
    - 실제 실행을 기반으로 한 분석을 통해 실행 계획에서 예상한 테이블 풀 스캔이 발생했고, 약 천만개에 해당하는 데이터를 조회했다.
    - 쿼리 실행 시간은 약 10s 이다.
<br/>

- **인덱스 적용 후 테스트 결과**
  - **실행 시간 평균: 190ms**
  - 실행 계획 (explain)

    | id | select\_type | table | partitions | type | possible\_keys | key | key\_len | ref | rows | filtered | Extra |
    | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
    | 1 | SIMPLE | member\_point | NULL | ref | member\_point\_member\_id\_index | member\_point\_member\_id\_index | 9 | const | 1 | 100 | NULL |
  - 실행 계획 분석 (explain analyze)
  
    | EXPLAIN |
    | :--- |
    | -&gt; Index lookup on member\_point using member\_point\_member\_id\_index \(member\_id=10000\)  \(cost=1.08 rows=1\) \(actual time=0.0301..0.0411 rows=1 loops=1\)<br/> |
  - 요약
    - 실행계획을 확인하면, 적용한 인덱스를 사용하여(```key```) 인덱스를 사용한 탐색(```type: ref```)이 발생함을 확인할 수 있다.
    - 또한 인덱스를 통해 WHERE 조건으로 거의 모든 데이터가 필터링(```filtered: 100```) 됨을 확인할 수 있다.
    - 실행 속도가 인덱스를 적용하기 전보다 훨씬 줄었음을 확인할 수 있다. cost와 탐색하는 row의 수도 현저히 줄었다.
<br/>

### 4-2. 예약 가능한 콘서트 스케줄 목록 조회
- **기능**
  - 요청한 시점을 기점으로 사용자가 선택한 콘서트에 해당하는 예약 가능한 스케줄 목록을 조회한다.
<br/>

- **실행 쿼리**
  ```sql
  # 총 1,000만개의 데이터, 서로 다른 concert_id 총 100,000개
  select
      *
  from
      concert_schedule
  where
      concert_id = 1 # (선택한 콘서트)
      and scheduled_at > '2024-01-01 00:00:00' # (요청 시간)
  limit 500;
  ```
<br/>

- **인덱스가 필요한 이유**
  - ```concert_schedule``` 테이블에 대해서 데이터 삽입/수정 보다 조회가 빈번하게 일어난다. 
  - 콘서트 예약을 하기 이전에 항상 콘서트 스케줄을 조회하기 때문에, 조회가 자주 발생하는 쿼리다.
<br/>

- **인덱스 적용**
  - 인덱스 적용 컬럼: ```concert_id, scheduled_at```
    - WHERE조건에 사용되는 컬럼들을에 대해 복합인덱스를 생성한다.
    - ```concert_id```의 순서가 ```scheduled_at```보다 우선인 이유는, WHERE절에서 ```scheduled_at```은 범위 조건이기 때문에, 해당 컬럼은 인덱스를 타지만, 그 뒤에 인덱스 컬럼들은 인덱스가 사용되지 않기 때문이다.
  - 인덱스 생성 쿼리
    ```sql
    create index concert_schedule_concert_id_scheduled_at_index on concert_schedule (concert_id, scheduled_at);
    ``` 
<br/>

- **인덱스 적용 전 테스트 결과**
  - **실행 시간 평균: 17s 528ms**
  - 실행 계획 (explain)
    
    | id | select\_type | table | partitions | type | possible\_keys | key | key\_len | ref | rows | filtered | Extra |
    | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
    | 1 | SIMPLE | concert\_schedule | NULL | ALL | NULL | NULL | NULL | NULL | 9954866 | 3.33 | Using where |
  - 실행 계획 분석 (explain analyze)
    
    | EXPLAIN |
    | :--- |
    | -&gt; Filter: \(\(concert\_schedule.concert\_id = 100\) and \(concert\_schedule.scheduled\_at &gt; TIMESTAMP'2024-01-01 00:00:00'\)\)  \(cost=1.04e+6 rows=331796\) \(actual time=30.7..20522 rows=100 loops=1\)<br/>    -&gt; Table scan on concert\_schedule  \(cost=1.04e+6 rows=9.95e+6\) \(actual time=1.93..19435 rows=10e+6 loops=1\)<br/> |
  - 요약
    - 실행계획을 확인하면, 테이블의 풀 스캔이 발생하고(```type: ALL```), 약 9,714,742 행(```rows: 9954866```)을 탐색할 수 있음을 확인할 수 있다.
    - 또한 WHERE 조건으로 필터링(```filtered: 3.33```)이 거의 되지 않을 것이라고 볼 수 있다.
    - 실제 실행을 기반으로 한 분석을 통해 실행 계획에서 예상한 테이블 풀 스캔이 발생했고, 약 천만개에 해당하는 데이터를 조회했다.
    - 쿼리 실행 시간은 약 19s 이다.
<br/>

- **인덱스 적용 후 테스트 결과**
  - **실행 시간 평균: 233ms**
  - 실행 계획 (explain)
    
    | id | select\_type | table | partitions | type | possible\_keys | key | key\_len | ref | rows | filtered | Extra |
    | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
    | 1 | SIMPLE | concert\_schedule | NULL | range | concert\_schedule\_concert\_id\_scheduled\_at\_index | concert\_schedule\_concert\_id\_scheduled\_at\_index | 17 | NULL | 100 | 100 | Using index condition |
  - 실행 계획 분석 (explain analyze)
  
    | EXPLAIN |
    | :--- |
    | -&gt; Index range scan on concert\_schedule using concert\_schedule\_concert\_id\_scheduled\_at\_index over \(concert\_id = 100 AND '2024-01-01 00:00:00.000000' &lt; scheduled\_at\), with index condition: \(\(concert\_schedule.concert\_id = 100\) and \(concert\_schedule.scheduled\_at &gt; TIMESTAMP'2024-01-01 00:00:00'\)\)  \(cost=121 rows=100\) \(actual time=0.902..1.43 rows=100 loops=1\)<br/> |
  - 요약
    - 실행계획을 확인하면, 적용한 인덱스를 사용하여(```key```) Index Range Scan(```type: range```)이 발생함을 확인할 수 있다.
    - 또한 인덱스를 통해 WHERE 조건으로 거의 모든 데이터가 필터링(```filtered: 100```) 됨을 확인할 수 있다.
    - 실행 속도가 인덱스를 적용하기 전보다 훨씬 줄었음을 확인할 수 있다. cost와 탐색하는 row의 수도 현저히 줄었다.
<br/>

### 4-3. 특정 콘서트 스케줄에 해당하는 좌석 목록 조회
- **기능**
  - 사용자가 선택한 콘서트 스케줄에 해당하는 좌석 목록을 조회한다.
<br/>

- **실행 쿼리**
  ```sql
  # 총 1,000만개의 데이터, 서로 다른 concert_schedule_id 총 50,000개
  
  select
      *
  from
      concert_seat
  where
      concert_schedule_id = 10000
  limit 500;
  ```
<br/>

- **인덱스가 필요한 이유**
  - ```concert_seat```은 테이블에 대해서 데이터의 수정도 빈번하게 발생하지만, 수정 성능보다 조회 성능이 더 높아야 하는 데이터라고 판단된다.
<br/>

- **인덱스 적용**
  - 인덱스 적용 컬럼: ```concert_schedule_id```
    - 각 콘서트 스케줄은 여러 좌석이 포함되어 있지만, 각 스케줄당 좌석 수가 많지 않을 것이라고 판단되어 ```concert_schedule_id```의 중복도가 낮을 것이다.
    - WHERE절에 자주 사용되는 조건이다.
  - 인덱스 생성 쿼리
    ```sql
    create index concert_seat_concert_schedule_id_index on concert_seat (concert_schedule_id);
    ``` 
<br/>

- **인덱스 적용 전 테스트 결과**
  - **실행 시간 평균: 15s 579ms**
  - 실행 계획 (explain)
    
    | id | select\_type | table | partitions | type | possible\_keys | key | key\_len | ref | rows | filtered | Extra |
    | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
    | 1 | SIMPLE | concert\_seat | NULL | ALL | NULL | NULL | NULL | NULL | 9688378 | 10 | Using where |
    
  - 실행계획 분석 (explain analyze)
    
    | EXPLAIN |
    | :--- |
    | -&gt; Filter: \(concert\_seat.concert\_schedule\_id = 10000\)  \(cost=1.03e+6 rows=993051\) \(actual time=1485..17834 rows=200 loops=1\)<br/>    -&gt; Table scan on concert\_seat  \(cost=1.03e+6 rows=9.93e+6\) \(actual time=1.48..17000 rows=10e+6 loops=1\)<br/> |
  - 요약
    - 실행계획을 확인하면, 테이블의 풀 스캔이 발생하고(```type: ALL```), 약 9,688,378 행(```rows: 9688378```)을 탐색할 수 있음을 확인할 수 있다.
    - 또한 WHERE 조건으로 필터링(```filtered: 10```)이 거의 되지 않을 것이라고 볼 수 있다.
    - 실제 실행을 기반으로 한 분석을 통해 실행 계획에서 예상한 테이블 풀 스캔이 발생했고, 약 천만개에 해당하는 데이터를 조회했다.
    - 쿼리 실행 시간은 약 17s 이다.
<br/>

- **인덱스 적용 후 테스트 결과**
  - **실행 시간 평균: 230ms**
  - 실행 계획 (explain)
  
    | id | select\_type | table | partitions | type | possible\_keys | key | key\_len | ref | rows | filtered | Extra |
    | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
    | 1 | SIMPLE | concert\_seat | NULL | ref | concert\_seat\_concert\_schedule\_id\_index | concert\_seat\_concert\_schedule\_id\_index | 9 | const | 200 | 100 | NULL |
 
  - 실행 계획 분석 (explain analyze)

    | EXPLAIN |
    | :--- |
    | -&gt; Index lookup on concert\_seat using concert\_seat\_concert\_schedule\_id\_index \(concert\_schedule\_id=10000\)  \(cost=216 rows=200\) \(actual time=0.203..0.417 rows=200 loops=1\)<br/> |
    
  - 요약
    - 실행계획을 확인하면, 적용한 인덱스를 사용하여(```key```) 인덱스를 사용한 탐색(```type: ref```)이 발생함을 확인할 수 있다.
    - 또한 인덱스를 통해 WHERE 조건으로 거의 모든 데이터가 필터링(```filtered: 100```) 됨을 확인할 수 있다.
    - 실행 속도가 인덱스를 적용하기 전보다 훨씬 줄었음을 확인할 수 있다. cost와 탐색하는 row의 수도 현저히 줄었다.
<br/>


### 성능 개선 결과 요약
- 사용자 포인트 조회
  - 10s 113ms -> 190ms 로 조회속도가 약 98.12% 개선되었다.
- 예약 가능한 콘서트 스케줄 목록 조회
  - 17s 528ms -> 233ms 로 조회속도가 약 98.67% 개선되었다.
- 특정 콘서트 스케줄에 해당하는 좌석 목록 조회
  - 15s 579ms -> 230ms 로 조회속도가 약 98.52% 개선되었다.
<br/>

## 5. 결론
현재 구현한 기능에서 자주 조회하는 쿼리를 분석하여 인덱스 적용을 통해 조회 속도를 평균 약 98%까지 개선했다. 실제 데이터가 아닌 임의 데이터로 테스트를 진행하면서 임의로 컬럼의 중복도를 설정해서 정확한 결과가 아닐 수 있지만,
위에서 제시한 인덱스 생성 기준을 통해 인덱스를 적용하면 어느정도의 성능 개선이 이뤄질 수 있음을 확인할 수 있었다.
