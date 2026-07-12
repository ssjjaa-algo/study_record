# 목록 쿼리 최적화

- 여기서 말하는 목록 쿼리이란 페이징이랑 동일
- 목표 : 전체 데이터 처리를 n-Row 방식으로 처리

```sql
SELECT 종류, 제목, 발송일자, 크기
  FROM
     ( SELECT ROWNUM 순번, 종류, 제목, 발송일자
         FROM
            ( SELECT 번호, 종류, 제목, 발송일자
                FROM 메일
               WHERE 메일_아이디 = 'KKK'
               ORDER BY 발송일자 DESC
            )
      )
 WHERE 순번 BETWEEN 1 AND 20;
```

- 조건을 만족하는 데이터가 1만건이라면 1만건의 액세스 필요
    - 1만건 액세스 이후 발송일자 정렬로 인한 다시 1만건의 정렬 필요
    - 1만 건의 데이터 중 가장 최근 데이터는 순번 1, 맨 마지막은 순번 20000
- 순번 20개를 확인하기 위한 추가적인 전체 데이터 액세스 과정필요
- **`테이블을 총 3번 탐색`**

- 조건을 만족하는 모든 데이터 추출
- ORDER BY 절에 의한 조건을 만족하는 모든 데이터에 대해 정렬 발생
- 순번 할당을 위한 전체 대상 데이터를 다시 액세스
- 원하는 순번 값을 결과로 추출하기 위해 모든 대상 데이터를 다시 액세스
    - 이 문제는 **`ROWNUM ≤ 20`**으로 해결 가능

## 개선해보자

- 전체 데이터 처리의 목록 쿼리를 n-Row 쿼리로 변경

```sql
SELECT 종류, 제목, 도착시간, 크기
  FROM
     ( SELECT ROWNUM 순번, 종류, 제목, 도착시간, 크기
         FROM
            ( SELECT 번호, 종류, 제목, 도착시간, 크기
                FROM 메일_내역
               WHERE 메일_ID= 'ABC'
               ORDER BY 도착시간 DESC
            )
     )
 WHERE 순번 BETWEEN 1 AND 10
   AND ROWNUM <= 10;
```

```sql
SELECT 종류, 제목, 도착시간, 크기
  FROM
     ( SELECT ROWNUM 순번, 종류, 제목, 도착시간, 크기
         FROM
            ( SELECT 번호, 종류, 제목, 도착시간, 크기
                FROM 메일_내역
               WHERE 메일_ID= 'ABC'
            )
     )
 WHERE 순번 BETWEEN 1 AND 10;
```

- ORDER BY 절 제거
    - 원하는 결과를 보장하지 못함
    - 따라서 메일ID + 도착시간의  index 필요

```sql
SELECT 종류, 제목, 도착시간, 크기
  FROM
     ( SELECT ROWNUM 순번, 종류, 제목, 도착시간, 크기
         FROM
            ( SELECT /*+ INDEX_DESC(메일_내역 메일_아이디_도착시간) */
                          번호, 종류, 제목, 도착시간, 크기
                FROM 메일_내역
               WHERE 메일_ID= 'ABC'
            )
     )
 WHERE 순번 BETWEEN 1 AND 10;
```

```sql
SELECT 종류, 제목, 도착시간, 크기
  FROM
     ( SELECT /*+ INDEX_DESC(메일_내역 메일_아이디_도착시간) */
              ROWNUM 순번, 번호, 종류, 제목, 도착시간, 크기
         FROM 메일_내역
        WHERE 메일_ID= 'ABC'
          AND ROWNUM <= 10
     )
 WHERE 순번 BETWEEN 1 AND 10;
```

## 조인 테이블

```sql
SELECT 등록일자, 민원인_성명, 민원연락처, 민원요약내용, 해결여부, 처리부서
  FROM
     ( 
       SELECT ROWNUM 순번, 등록일자, 민원인_성명, 민원연락처,
                 민원요약내용, 해결여부, 처리부서
         FROM
            ( 
              SELECT AA.등록일자, BB.민원인_성명, BB.민원인연락처,
                     AA.민원요약내용, AA.해결여부, AA.처리부서
                FROM 민원요청 AA, 민원인 BB
               WHERE AA.ID = BB.ID
                 AND AA.등록일자 > '20070101'
               ORDER BY AA.등록일자 DESC
            )
     )
 WHERE 순번 BETWEEN 1 AND 10;
```

- 민원요청 테이블의 등록일자 컬럼을 기준으로 정렬하기에 Driving table은 민원요청 테이블
- 민원요청 테이블의 인덱스 필요 (ID + 등록일자)
- 성능 보장을 위한 Inner(민원인) 테이블의 조인 조건에 인덱스 필요

```sql
SELECT 등록일자, 민원인_성명, 민원연락처, 민원요약내용, 해결여부, 처리부서
  FROM
     ( SELECT /*+ ORDERED USE_NL(BB) INDEX_DESC(AA,등록일자_IDX) */
              ROWNUM 순번,AA.등록일자, BB.민원인_성명, BB.민원인연락처,
              AA.민원요약내용, AA.해결여부, AA.처리부서
         FROM 민원요청 AA, 민원인 BB
        WHERE AA.ID = BB.ID
          AND AA.등록일자>'20070101'
          AND ROWNUM <= 10
     )
 WHERE 순번 BETWEEN 1 AND 10;
```

## 조인 테이블 서로 다른 테이블의 정렬 조건

```sql
SELECT 등록일자, 민원인_성명, 민원연락처, 민원요약내용, 해결여부, 처리부서
  FROM
     ( 
       SELECT ROWNUM 순번, 등록일자, 민원인_성명, 민원연락처,
                 민원요약내용, 해결여부, 처리부서
         FROM
            ( 
              SELECT AA.등록일자, BB.민원인_성명, BB.민원인연락처,
                     AA.민원요약내용, AA.해결여부, AA.처리부서
                FROM 민원요청 AA, 민원인 BB
               WHERE AA.ID = BB.ID
                 AND AA.등록일자 > '20070101'
                 AND BB.민원인_성명 LIKE '김%'
               ORDER BY AA.등록일자 DESC, BB.민원인_성명
            )
     )
 WHERE 순번 BETWEEN 1 AND 10;

```

- 먼저 액세스되는 테이블의 인덱스에 의해 정렬된다
    - 민원요청 테이블의 등록일자 인덱스를 이용한다면 order by 추출 없이 데이터 추출 가능
- inner 테이블의 민원인은 민원인_성명만으로 정렬을 시도할 수 있는가.
    - 불가
    - 등록일자 컬럼이 UNIQUE이고 민원인 인덱스가 (ID + 민원인_성명)인 경우는 가능

### ORDER BY 절에 민원인_성명 컬럼 대신 조인 조건인 ID 컬럼

```sql
SELECT 등록일자, 민원인_성명, 민원연락처, 민원요약내용, 해결여부, 처리부서
  FROM
     ( 
       SELECT ROWNUM 순번, 등록일자, 민원인_성명, 민원연락처,
                 민원요약내용, 해결여부, 처리부서
         FROM
            ( 
              SELECT AA.등록일자, BB.민원인_성명, BB.민원인연락처,
                     AA.민원요약내용, AA.해결여부, AA.처리부서
                FROM 민원요청 AA, 민원인 BB
               WHERE AA.ID = BB.ID
                 AND AA.등록일자 > '20070101'
                 AND BB.민원인_성명 LIKE '김%'
               ORDER BY AA.등록일자 DESC, BB.ID
            )
     ) 
 WHERE 순번 BETWEEN 1 AND 10;
```

- 민원요청 테이블 : (등록일자 + ID)
- 민원요청 테이블로부터 조건을 만족하는 테이블을 ID 순으로 넘겨받는다
- 민원인 테이블은 넘겨받은 ID 순으로 작업 가능

```sql
SELECT 등록일자, 민원인_성명, 민원연락처, 민원요약내용, 해결여부, 처리부서
  FROM
     ( 
       SELECT /*+ INDEX_DESC(AA, 등록일자_ID_IDX) */
              ROWNUM 순번, AA.등록일자, BB.민원인_성명, BB.민원인연락처,
              AA.민원요약내용, AA.해결여부, AA.처리부서
         FROM 민원요청 AA, 민원인 BB
        WHERE AA.ID = BB.ID
          AND AA.등록일자 > '20070101'
          AND BB.민원인_성명 LIKE '김%'
          AND ROWNUM <= 10
     )
 WHERE 순번 BETWEEN 1 AND 10;
```
