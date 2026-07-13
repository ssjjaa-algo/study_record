import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';

const API_BASE_URL = __ENV.API_BASE_URL || 'http://localhost:8080';
const RATE = Number(__ENV.RATE || 100);
const DURATION = __ENV.DURATION || '30s';
const TOTAL_QUANTITY = Number(__ENV.TOTAL_QUANTITY || 100);

const success = new Counter('ticket_issue_success');
const soldOut = new Counter('ticket_issue_sold_out');
const duplicated = new Counter('ticket_issue_duplicated');
const unexpected = new Counter('ticket_issue_unexpected');

http.setResponseCallback(http.expectedStatuses({ min: 200, max: 499 }));

export const options = {
  scenarios: {
    issue: {
      executor: 'constant-arrival-rate',
      rate: RATE,
      timeUnit: '1s',
      duration: DURATION,
      preAllocatedVUs: Number(__ENV.PREALLOCATED_VUS || 100),
      maxVUs: Number(__ENV.MAX_VUS || 1000),
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<500'],
    ticket_issue_unexpected: ['count==0'],
  },
};

export function setup() {
  const response = http.post(
    `${API_BASE_URL}/api/ticket-events`,
    JSON.stringify({
      name: `k6-${Date.now()}`,
      totalQuantity: TOTAL_QUANTITY,
    }),
    {
      headers: {
        'Content-Type': 'application/json',
      },
    },
  );

  check(response, {
    'event created': (res) => res.status === 201,
  });

  const event = response.json();
  console.log(`created eventId=${event.id}, totalQuantity=${TOTAL_QUANTITY}`);
  return {
    eventId: event.id,
  };
}

export default function (data) {
  const userId = `k6-user-${__VU}-${__ITER}-${Math.random()}`;
  const response = http.post(
    `${API_BASE_URL}/api/ticket-events/${data.eventId}/issues`,
    JSON.stringify({ userId }),
    {
      headers: {
        'Content-Type': 'application/json',
      },
    },
  );

  if (response.status === 202) {
    success.add(1);
  } else if (response.status === 410) {
    soldOut.add(1);
  } else if (response.status === 409) {
    duplicated.add(1);
  } else {
    unexpected.add(1);
  }

  check(response, {
    'accepted or rejected by business rule': (res) =>
      res.status === 202 || res.status === 410 || res.status === 409,
  });

  sleep(0);
}

export function teardown(data) {
  const response = http.get(`${API_BASE_URL}/api/ticket-events/${data.eventId}`);
  console.log(`final event state: ${response.body}`);
}
