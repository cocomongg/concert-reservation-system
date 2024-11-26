import http from 'k6/http';
import { check } from 'k6';

export let options = {
  vus: 1, // 가상 사용자 1명
  duration: '5s', // 5초 동안 테스트 실행
  thresholds: {
    http_req_duration: ['p(95)<500'],
  },
};

export default function () {
  const res = http.get('http://concert-service:8080/health-check');
  // 요청의 성공 여부를 체크
  check(res, {
    'status is 200': (r) => r.status === 200,
  });
}