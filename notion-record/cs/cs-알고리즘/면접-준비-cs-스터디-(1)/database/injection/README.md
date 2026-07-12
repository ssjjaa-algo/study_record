# Injection

날짜: 2023년 3월 21일

# 안녕!! 👋 성재의 블로그~.~

- SQL injection

데이터베이스에 전송되는 SQL 쿼리문을 조작하여 데이터를 변조하거나, 허가되지 않은 정보에 접근하는 공격

웹 해킹의 대표적인 방식에 속함

- 발생 원인

→ **사용자 입력값에 대한 필터링이 존재하지 않아 발생한다. 즉, 사용자 입력값에 대한 검증이 미흡**

 → User의 행동(클릭, 입력  등)에 따라 DB에 있는 데이터를 서로 다르게 표시

 → Query는 User 가 입력한 데이터를 포함하여 Dynamic 하게 변하므로 개발자가 의도하지 않은 정보를 열람 가능

- 예시
- 구문 우회
    
    →  id와  pw를 입력해 로그인, 로그인 시 뒷단의 데이터베이스와 SQL문의 허점을 이용하여 정보 탈취
    
    → select * from user where id = ‘id값’ and pw = ‘pw값’
    
    - 아래와 같이 id와 pw를 입력하였을 경우
    
     → id = 1
    
     → pw = ' 1 or '1' = '1
    
    - where id = 1 and pw = ‘’ 의 값은 false
    - ‘1’ = ‘1’의 값은  true
    - 결론적으로  false or true = true의 형태가 되어 로그인에 성공하여 접근.
- Union based
    - Union based : Union을 하면 추가 정보를 얻을 수 있다는 것은 알겠는데, 마땅한 예시가 떠오르지가 않습니다….
    
        → 정상적인 Query문에 Union 키워드 사용 = 원하는 쿼리문
    
        → Union하는 테이블의 컬럼수가 같아야 하고, 데이터 형도 같아야한다
    
        → Group by 절의 column을 1, 2, 3 …. 올려서 일치하는 경우의 결과값은 다르므로 행의 개수 파악 가능
    
- Blind SQL
    
     → 정보를 직접적으로 알 수 없더라도 True/False 값을 통해 정보를 알아내는 공격
    
    [SQL Injection : Blind SQL Injection](https://hanuscrypto.tistory.com/entry/SQL-Injection-Blind-SQL-Injection)
    
- 해결 방법
    
    1. 입력값 검증
    
        → 입력된 값이 개발자가 의도한 값인가? = 유효성 검사
    
        → 의도하지 않은 입력값에 대한 검증을 하고 차단
    
      
    
       2. 저장 프로시저 사용
    
         → Query에 미리 형식을 지정, 지정한 형식이 아니라면 Query 미실행
    
       3. 사용하지 않는 저장 프로시저, 내장함수 제거 및 권한 제어 등
    
       4. **PreparedStatement** 구문 사용 (!! 가장 유명한 방법인 듯..?)
    
         → **[공식 홈페이지에서도 해당 방법 사용을 권장한다고 함]**
    
         → 사용자의 입력값을 DBMS가 미리 컴파일 : 실행하지 않고 대기,
    
             그 후 사용자의 입력값을 ‘문자열’로.
    
             이렇게 되면 사용자의 입력값이 단순 문자열이 되어버리므로..!
    
             컴퓨터가 prepare의 인자로 들어간 SQL문 전체를 문자열로 인식,
    
             공격자가 파라미터를 조작하기가 어려워진다
    
       5. Error Message 노출 금지
    

  

[ 참고 사이트 좋은 것 같아요 ]

[[웹해킹] SQL Injection 총정리](https://gomguk.tistory.com/118)

[[웹해킹 #2] SQL Injection](https://g-idler.tistory.com/11)
