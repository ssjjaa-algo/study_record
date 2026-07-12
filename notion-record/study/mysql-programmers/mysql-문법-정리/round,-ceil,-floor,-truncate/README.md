# round, ceil, floor, truncate

- 프로그래머스 평균 일일 대여 요금 구하기

```sql
SELECT round(avg(DAILY_FEE),0) AS AVERAGE_FEE
FROM CAR_RENTAL_COMPANY_CAR
WHERE CAR_TYPE ="SUV"
```

- round(값, 자릿수)
    - 반올림
    - 자리수를 넣지 않으면 소수점 모두 반올림
    - 자리수 넣으면 위치까지 반올림
- ceil(값)
    - 소수점을 모두 올림
- floor(값)
    - 소수점을 모두 내림
- truncate(값, 자릿수)
    - 소수점을 모두 버리고 자리수까지 버릴 수 있음
