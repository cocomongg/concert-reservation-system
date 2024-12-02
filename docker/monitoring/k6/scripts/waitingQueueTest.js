import http from 'k6/http';
import { sleep, check } from 'k6';

const BASE_URL = 'http://concert-service:8080';
const ISSUE_TOKEN_URL = `${BASE_URL}/api/v1/queues/tokens`;
const GET_TOKEN_INFO_URL = `${BASE_URL}/api/v1/queues/tokens/order-info`;

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

export default function () {
  const token = issueToken();

  if (token) {
    waitForTokenActivation(token);
  }

  sleep(1);
}

function issueToken() {
  const response = http.post(ISSUE_TOKEN_URL);

  const isSuccess = check(response, {
    'Token issued successfully': (res) => res.status === 200,
  });

  if (!isSuccess) {
    console.error(`Failed to issue token: ${response.status}`);
    return null;
  }

  const token = JSON.parse(response.body).data.token;
  if (!token) {
    console.error('Token not found in the response.');
    return null;
  }

  return token;
}

function getTokenInfo(token) {
  const headers = { 'X-QUEUE-TOKEN': token };
  const response = http.get(GET_TOKEN_INFO_URL, { headers });
  let body = JSON.parse(response.body);

  const isSuccess = check(response, {
    'Fetched token info successfully': (res) => res.status === 200,
  });

  if (!isSuccess) {
    console.error(`Failed to fetch token info: ${response.status_text}, message: ${body.message}, token: ${token}`);
    return null;
  }

  return body.data;
}

function waitForTokenActivation(token) {
  let isActive = false;

  while (!isActive) {
    const info = getTokenInfo(token);

    if (!info) {
      break;
    }

    if (info.tokenStatus === 'ACTIVE') {
      isActive = true;
    } else {
      sleep(2);
    }
  }
}