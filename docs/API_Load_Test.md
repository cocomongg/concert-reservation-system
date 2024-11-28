# API 부하 테스트 결과 보고서

## 1. 개요 및 목표
- 본 보고서는 유저 시나리오를 기반으로 부하 테스트 대상을 선정하고, SLO / SLA 서비스 수준을 수립한 뒤, 부하 테스트 수행 결과를 분선한다.
- 부하테스트 결과를 통해 예상되는 트래픽을 시스템이 처리할 수 있는지 확인하고, 적절한 배포 스펙을 결정한다.
<br/>

## 2. 부하 테스트 목적
- **시스템 성능 검증**: 시스템 성능 측정을 통해 예상되는 트래픽을 처리할 수 있는지 확인한다.
- **안정성 검증**: 트래픽이 급증할 때, 시스템이 안정적으로 유지되는지 확인한다.
- **배포 스펙 선정**: 예상되는 트래픽을 처리한 결과를 통해 적절한 배포 스펙을 선정한다.
<br/>

## 3. 부하 테스트 대상 선정 
### 대기열 진입
- 트래픽 제어 없이 무제한의 사용자가 수행할 수 있는 시나리오로, 예측 불가능한 트래픽 급증이 발생할 수 있다.
### 콘서트 예약/결제 
- 스케줄링(배치)을 통해 일정 시간마다 제한된 사용자만이 수행할 수 있는 시나리오로, 예측 가능한 트래픽이 발생한다.
<br/>

### 선정 기준
- **실제 사용자 행동 반영**: 개별 API를 독립적으로 테스트하는 것도 의미가 있지만, 실제 사용자들이 서비스를 이용하는 방식을 정확히 반영하기 위해 유저 행동에 따라 순차적으로 호출되는 API들을 통합적으로 테스트하는 것이 더 효과적이라고 판단했다.
- **시나리오별 특성 분리**: 트래픽이 급증하는 시나리오와 예측 가능한 시나리오를 분리하여 각각의 특성에 맞게 부하 테스트를 수행했다.
<br/>

#### 시나리오별 API 호출 목록
<details>
  <summary>대기열 진입 호출 API 목록</summary>
  
- 대기열 토큰 발급 API
- 대기 순번 및 토큰 상태 조회 API
</details>

<details>
  <summary>콘서트 예약/결제 호출 API 목록</summary>
  
- 예약 가능한 콘서트 스케줄 목록 조회 API
- 예약 가능한 좌석 목록 조회 API
- 좌석 예약 API
- 사용자 포인트 잔액 조회 API
- 사용자 포인트 충전 API
- 결제 요청 API
</details>
<br/>

## 4. 서비스 수준 선정
### SLO ( Servie Level Object, 서비스 수준 목표)

**ResponseTime(RT) <= 300ms**
- 사용자 경험에 영향을 끼치지 않을 정도라고 판단하여 위와 같이 선정했다.

**P(99) <= 500ms**
- 나머지 1%에서 오래걸릴 수도 있음을 감안하여 평균 응답시간보다 조금 더 높게 선정했다.

**Error Rate <= 1%**
- 가용성을 99%로 보장하기 위해 에러율은 1%이하로 선정했다.

**TPS: 약 860TPS**
- 대기열 시나리오 기준으로 선정했다.
  <details>
    <summary>계산 방식</summary>
    
  - 동시접속자 4천명으로 예상
  - 대기열 시나리오 분당 transaction = 대기열 진입 API (1회) + 대기 순번 조회 API (약 12회) => 총 분당 13회
  - 13 (분당 트랜잭션) * 4000(유저) / 60s (1m) => 약 867 TPS
  </details>
<br/>

### SLA ( Service Level Agreements, 서비스 수준 협약)
**가용성 보장**
- 99%의 가용성을 보장한다.

**응답시간 보장**
- 평균 300ms 이하의 응답시간과, 99프로의 응답시간은 500ms 이하로 보장한다.
<br/>

## 5. 테스트 환경 및 도구
### 테스트 환경
**CPU**: 1 Core <br/>
**RAM**: 2GB
<br/>

**Application Framework**: Spirng Boot 3.3<br/>
**Database**: MySQL 8.0<br/>
**Cache**: Redis 7.1<br/>
**Message Broker**: Kafka 8.x<br/>
**Container Platform**: Docekr<br/>
<br/>

### 테스트 도구
**부하 테스트 도구**: k6
- 스크립트 기반으로 쉽게 부하테스트 시나리오를 구현할 수 있어서 채택했다.

**모니터링 도구**: Prometheus, Grafana
- 실시간 성능 모니터링과 각종 지표에 대한 시각화를 위해 사용했다.

**기타 도구**: Docker
- 테스트 환경 구성을 위해 사용했다.

<br/>

## 6. 부하테스트 결과
### (1) 대기열 시나리오
#### 부하 테스트 시나리오 설정
- 대기열 서비스의 특성상 갑작스러운 트래픽 급증을 구현하기 위해 **Spike Test**를 선택했다.
<br/>

#### k6 테스트 스크립트 옵션 설정
```js
export const options = {
  scenarios: {
    spike_test: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '20s', target: 5000 },
        { duration: '1m', target: 0 },
      ],
      gracefulStop: '0s',
      gracefulRampDown: '0s',
    },
  },
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(95)', 'p(99)'],
};
```
- **executor: 'ramping-vus'**
  - 가상 사용자의 수를 일정 시간 동안 증가시키거나 감소시키는 시나리오를 구현하기 위해 사용하였다.
- **startVUs: 0**
  - 테스트 시작 시 가상 사용자의 수를 0명으로 설정하여 초기 부하가 없도록 하였다.
- **stages:**
  - `{ duration: '20s', target: 5000 }`
	- 증가 단계: 20초 동안 가상 사용자(VUs)를 5000명까지 빠르게 증가시켜 트래픽 급증 상황을 재현하였다.
  - `{ duration: '1m', target: 0 }`
	- 감소 단계: 이후 1분 동안 VUs를 0명으로 감소시켜 트래픽이 줄어드는 상황에서 시스템의 안정성을 확인하였다.
<br/>

#### 테스트 결과 지표
![스크린샷 2024-11-29 오전 6 14 18](https://github.com/user-attachments/assets/2b533901-9a13-4960-840e-ddea1502da2d)
- ResponseTime(Rt): 3.19ms
- p(99): 37.55ms
- ErrorRate: 0.05%
- TPS: 1370
<br/>

#### 테스트 그래프
![스크린샷 2024-11-29 오전 6 13 41](https://github.com/user-attachments/assets/a7f480c6-37e7-4823-bd1e-4890f1ccf3a6)
<br/>

#### CPU 부하
![스크린샷 2024-11-29 오전 6 14 40](https://github.com/user-attachments/assets/79c75a28-300a-4646-9857-2dbf7a421b6f)
<br/>

### (2) 콘서트 예약/결제 시나리오
#### 부하 테스트 시나리오 설정
- 콘서트 예약/결제 시나리오의 특성상 예측 가능한 트래픽이 지속적으로 발생하는 시나리오를 구현하기 위해 **Load Test**를 선택했다.
- 현재 시스템은 30초마다 1,000명의 사용자를 활성 상태로 전환하도록 스케줄링되어 있으므로, 테스트도 이에 맞춰 진행했다.
<br/>

#### k6 테스트 스크립트 옵션 설정
```js
export const options = {
  scenarios: {
    constant_rate_scenario: {
      executor: 'constant-arrival-rate',
      rate: 1000,
      timeUnit: '30s',
      duration: '2m',
      preAllocatedVUs: 4000,
      maxVUs: 4200,
    },
  },
}
```
- **executor: ‘constant-arrival-rate’**
  - 일정한 비율로 새로운 가상 사용자를 생성하여 지속적인 트래픽을 발생시키기 위해 사용했다.
- **rate: 1000**
  - timeUnit 동안 시작될 VU(가상 사용자)의 수를 설정했다. 해당 설정을 통해 30초마다 1000명의 VU가 시작된다.
- **timeUnit: ‘30s’**
  - rate에서 정의한 VU 수가 시작되는 시간을 지정한다.
- **duration: ‘2m’**
  - 전체 테스트 실행 시간을 2분으로 설정하였다.
- **preAllocatedVUs: 4000**
  - 사전에 할당할 VU의 수를 설정하여 테스트 시작 시 필요한 자원을 미리 확보하였다.
- **maxVUs: 4200**
  - 최대 VU의 수를 제한하여 시스템 자원 사용을 통제하였다.
<br/>

#### 테스트 결과 지표
![스크린샷 2024-11-29 오전 3 44 14](https://github.com/user-attachments/assets/5fab2ac2-1ce7-4e25-96a4-0cf6330ad5e1)
- ResponseTime(Rt): 4s
- p(99): 7.41s
- ErrorRate: 0.00%
- TPS: 181
<br/>

#### 테스트 그래프
![스크린샷 2024-11-29 오전 3 44 30](https://github.com/user-attachments/assets/72e64df4-6091-4106-a3d4-c206312c28a1)
<br/>

#### CPU 부하
![스크린샷 2024-11-29 오전 3 44 47](https://github.com/user-attachments/assets/5ac491d5-282b-45db-ba41-6592a3ef5f21)
<br/>

#### Hikari CP
![스크린샷 2024-11-29 오전 3 45 07](https://github.com/user-attachments/assets/7335f4b2-2f89-4fbb-8c51-18e804cd8bad)
<br/>
