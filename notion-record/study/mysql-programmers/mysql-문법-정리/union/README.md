# union

## 개념

- 여러 개의 SELECT 문의 결과를 단일 결과 세트로 연결 표현할 때 사용
    - 합친 결과에서 중복된 행은 한 번만 표시
    - **`UNION ALL은 중복된 행까지 표시함`**

## 특징

- UNION 내의 각 SELECT문은 같은 수의 열을 가져야 한다
- SELECT문의 열도 동일하게 위치해야 한다
- 열은 호환되는 데이터 순서를 가져야 함
- 프로그래머스 **오프라인 온라인 판매 데이터 통합하기 문제**

```sql
SELECT date_format(SALES_DATE,"%Y-%m-%d") AS SALES_DATE, PRODUCT_ID, USER_ID, SALES_AMOUNT
FROM ONLINE_SALE
WHERE SALES_DATE LIKE("2022-03%")

UNION ALL

SELECT date_format(SALES_DATE,"%Y-%m-%d") AS SALES_DATE, PRODUCT_ID, NULL AS USER_ID, SALES_AMOUNT
FROM OFFLINE_SALE
WHERE SALES_DATE LIKE("2022-03%")

ORDER BY SALES_DATE, PRODUCT_ID, USER_ID
```
