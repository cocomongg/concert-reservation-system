import http from 'k6/http';
import { sleep, check, fail } from 'k6';
import {randomIntBetween, randomItem} from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

const BASE_URL = 'http://concert-service:8080';
const testToken = 'test';

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
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(95)', 'p(99)'],
}

export default function (i) {
  const memberId = randomIntBetween(1, 4000);
  const concertId = randomIntBetween(1, 1000);

  const concertSchedules = getReservableSchedule(concertId);
  sleep(1);

  let selectedSchedule = randomItem(concertSchedules);

  let concertScheduleId = selectedSchedule.concertScheduleId;
  const reservableSeats = getReservableSeats(concertId, concertScheduleId);
  sleep(1);

  let selectedSeat = randomItem(reservableSeats);
  let concertSeatId = selectedSeat.concertSeatId;
  let reservationId = reserveSeat({
    memberId: memberId,
    seatId: concertSeatId,
    concertId: concertId,
    concertScheduleId: concertScheduleId,
  });
  sleep(1);

  getMemberPoint(memberId);
  sleep(1);

  chargeMemberPoint(memberId, 100000);
  sleep(1);

  payment(reservationId);
  sleep(1);
}

function getReservableSchedule(concertId) {
  const headers = { 'X-QUEUE-TOKEN':  testToken};
  let response = http.get(`${BASE_URL}/api/v1/concerts/${concertId}/schedules`, { headers });

  check(response, {
    'Get reservable schedule': (res) => res.status === 200,
  });

  let concertSchedules = response.json().data;
  if(!concertSchedules || concertSchedules.length === 0) {
    fail('No reservable schedules');
  }

  return concertSchedules;
}

function getReservableSeats(concertId, concertScheduleId) {
  const headers = { 'X-QUEUE-TOKEN':  testToken};
  let response = http.get(`${BASE_URL}/api/v1/concerts/${concertId}/schedules/${concertScheduleId}/seats`, { headers });

  let isSuccess = check(response, {
    'Get reservable seats': (res) => res.status === 200,
  });

  if (!isSuccess) {
    fail('Failed to get reservable seats');
  }

  let concertSeats = response.json().data;
  if(!concertSeats || concertSeats.length === 0) {
    fail('No reservable seats');
  }

  return concertSeats;
}

function reserveSeat(data) {
  const headers = {
    'X-QUEUE-TOKEN':  testToken,
    'Content-Type': 'application/json',
  };
  const payload = JSON.stringify({
    seatId: data.seatId,
    memberId: data.memberId,
  });

  let concertId = data.concertId;
  let concertScheduleId = data.concertScheduleId;
  let response = http.post(`${BASE_URL}/api/v1/concerts/${concertId}/schedules/${concertScheduleId}/reservation`, payload, { headers });

  let isSuccess = check(response, {
    'Reserve seat': (res) => res.status === 200,
  });

  if(!isSuccess) {
    fail('Failed to reserve seat');
  }

  let resData = response.json().data;
  if(!resData) {
    fail('No reservation data');
  }

  return resData.reservationId;
}

function getMemberPoint(memberId) {
  const headers = { 'X-QUEUE-TOKEN':  testToken};
  let response = http.get(`${BASE_URL}/api/v1/members/${memberId}/points`, { headers });

  let isSuccess = check(response, {
    'Get member point': (res) => res.status === 200,
  });

  if(!isSuccess) {
    fail('Failed to get member point');
  }

  let resData = response.json().data;
  if(!resData) {
    fail('No member point');
  }

  return resData.pointAmount;
}

function chargeMemberPoint(memberId, pointAmount) {
  const payload = JSON.stringify({
    amount: pointAmount,
  });
  const headers = {
    'X-QUEUE-TOKEN':  testToken,
    'Content-Type': 'application/json',
  };
  let response = http.patch(`${BASE_URL}/api/v1/members/${memberId}/points`, payload, { headers });

  let isSuccess = check(response, {
    'Charge member point': (res) => res.status === 200,
  });

  if (!isSuccess) {
    fail('Failed to charge member point');
  }
}

function payment(reservationId) {
  const payload = JSON.stringify({
    reservationId: reservationId,
  });
  const headers = {
    'X-QUEUE-TOKEN':  testToken,
    'Content-Type': 'application/json',
  };
  let response = http.post(`${BASE_URL}/api/v1/payments`, payload, { headers });

  let isSuccess = check(response, {
    'Payment': (res) => res.status === 200,
  });

  if (!isSuccess) {
    fail('Failed to payment');
  }
}