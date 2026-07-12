# JOIN

날짜: 2023년 3월 21일
사람: 유영

## JOIN이란?

> 두 개 이상의 테이블을 **결합**하는 SQL문
> 

풀어 설명하면, 두 개 이상의 테이블의 **record**를 묶어 하나의 **column**으로 표현하여 결과 set을 반환한다.

여기서 **하나의 column**이라고 표현한 이유는 OUTER JOIN인지에 따라 null record의 존재 여부와 ON 절에 충족하지 않는 값에 대한 제외 여부 때문에 하나의 column이라고 서술한 듯.

<aside>
💡 두 개 이상의 테이블이라 확정 지을 수 없다.
⇒ 자신 테이블을 **SELF JOIN** 할 수 있기 때문 (후술).

</aside>

### JOIN의 목적

관계형 데이터베이스의 특징 : **정규화** (이것은 정규화 파트에서 자세히 설명해줄 것입니다~)

위 이유 때문에 각각 분리된 table에서 필요한 결과 set을 얻기 위해선 다시 **관련**된 **테이블**들을 **조합**하여 결과를 추출할 필요가 있다.

⇒ 독립된 테이블을 상호연관성(**ON**)에 의해 연결(**JOIN**)해 하나의 결과 set을 보여줄 수 있음

## JOIN의 종류

### INNER JOIN

```sql
SELECT `fields1`, `fields2`, `fields3`
FROM `table1` t1
**INNER JOIN** `table2` t2 **ON** t1.`field` = t2.`field`
```

- 두 table **모두** **ON 절**을 **충족**하는 행을 반환 (**교집합**)
- `JOIN`만 기술했을 때는 `INNER JOIN`으로 **default**
- 가장 많이 사용하는 JOIN

### LEFT JOIN

```sql
SELECT `fields1`, `fields2`, `fields3`
FROM `table1` t1
**LEFT (**OUTER**) JOIN** `table2` t2 **ON** t1.`field` = t2.`field`
-- WHERE t2.`field` IS NULL
```

- 왼쪽 table의 **모든** 행과, **ON 절**을 **충족**하는 오른쪽 table의 행을 결합
- ON 절을 충족하는 행이 없을 시 **null**로 표기 ⇒ t2 table의 column에 null 값 존재 가능
- 여기서 오른쪽이란 `LEFT JOIN` 뒤의 table을 뜻함
- `WHERE t2.`field` IS NULL` 절 추가 시, t2에 해당하는 값을 **제외**하는 **차집합** 형태로 반환 가능
    - 모든 t2의 data가 null

### RIGHT JOIN

```sql
SELECT `fields1`, `fields2`, `fields3`
FROM `table1` t1
**RIGHT (**OUTER**) JOIN** `table2` t2 **ON** t1.`field` = t2.`field`
-- WHERE t1.`field` IS NULL
```

- **ON 절**을 **충족**하는 왼쪽 table의 행과, 오른쪽 table의 **모든** 행을 결합
- ON 절을 충족하는 행이 없을 시 **null**로 표기 ⇒ t1 table의 column에 null 값 존재 가능
- 위와 같이, 왼쪽이란 `RIGHT JOIN` 앞의 table을 뜻함
- `WHERE t1.`field` IS NULL` 절 추가 시, t1에 해당하는 값을 **제외**하는 **차집합** 형태로 반환 가능
    - 모든 t1의 data가 null

### OUTER JOIN

```sql
SELECT `fields1`, `fields2`, `fields3`
FROM `table1` t1
(FULL) **OUTER JOIN** `table2` t2 **ON** t1.`field` = t2.`field`
-- WHERE t1.`field` IS NULL OR t2.`field` IS NULL
```

💫 **MySQL**에는 `FULL OUTER JOIN`이 존재하지 않음!

⇒ `LEFT JOIN`과 `RIGHT JOIN`을 **UNION** 해주는 방식으로 만듦

- 왼쪽 table의 **모든** 행과,  오른쪽 table의 **모든** 행을 결합 (**합집합**)
- ON 절을 충족하는 행이 없을 시 **null**로 표기 ⇒ t1 table, t2 table의 column에 null 값 존재 가능
- `WHERE t1.`field` IS NULL OR t2.`field` IS NULL`절 추가 시, 교집합을 **제외**하는 **차집합** 형태로 반환 가능

### CROSS JOIN

```sql
SELECT `fields1`, `fields2`, `fields3`
FROM `table1` t1
**CROSS JOIN** `table2` t2
-- FROM `table1` t1, `table2` t2
```

- 왼쪽 table의 **모든** 행과,  오른쪽 table의 **모든** 행을 **조건 없이** 결합 (**합집합**)
- 그냥 각 table에 comma(,)로 연결해준 것과 같은 결과 set 반환
- 실사용 예시로, 게시글에 공통된 첨부파일을 첨부하고 싶을 때 WHERE 절로 공통 파일을 찾아 모든 게시글에 첨부 가능
- 사실 왜 굳이 CROSS JOIN SQL문을 쓰는지 모르겠음 그냥 가독성 때문에 쓰는 것 같기도 하고… 그냥 콤마 쓰면 될듯

### SELF JOIN

```sql
SELECT `fields1`, `fields2`, `fields3`
FROM `**table1**` t1
JOIN `**table1**` t2 **ON** t1.`field` = t2.`field`
```

- **자기 자신** table을 별칭만 바꿔 결합
- 하나의 table에서 서로 다른 레코드를 연결하고 싶을 때 사용 (ON 절이 중요)
- 실사용 예시로, 직원 table에서 직원 행과 매니저 행을 결합하고 싶을 때 사용 가능

- **Reference** 🪶
    
    💖 *Thanks to…*
    
    [*https://rh-cp.tistory.com/44*](https://rh-cp.tistory.com/44)
    
    [*https://hongong.hanbit.co.kr/sql-기본-문법-joininner-outer-cross-self-join/*](https://hongong.hanbit.co.kr/sql-%EA%B8%B0%EB%B3%B8-%EB%AC%B8%EB%B2%95-joininner-outer-cross-self-join/)
    
    *그리고  만인의 연인 ChatGPT*
