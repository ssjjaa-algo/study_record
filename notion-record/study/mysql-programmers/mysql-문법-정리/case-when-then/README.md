# CASE WHEN THEN

**CASE**

**WHEN 조건**

**THEN ‘반환 값’**

**WHEN 조건**

**THEN ‘반환 값’**

**ELSE ‘WHEN 조건에 해당 안되는 경우 반환 값’**

**END**

```sql
SELECT HISTORY_ID,
       CAR_ID,
       date_format(START_DATE, "%Y-%m-%d") AS START_DATE,
       date_format(END_DATE, "%Y-%m-%d") AS END_DATE,
       CASE WHEN datediff(END_DATE, START_DATE) >= 29
                THEN "장기 대여"
            ELSE "단기 대여"
           END AS RENT_TYPE
FROM CAR_RENTAL_COMPANY_RENTAL_HISTORY
WHERE START_DATE LIKE "2022-09%"
ORDER BY HISTORY_ID DESC;
```
