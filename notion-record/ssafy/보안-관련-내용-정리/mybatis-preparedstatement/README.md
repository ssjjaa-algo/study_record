# mybatis - preparedStatement

MyBatis에서 #와 $는 SQL 매핑에 사용되며, 다른 동작과 용도를 가지고 있습니다.

1. **(순수한 JDBC 파라미터 형식):**
    - #을 사용하면 MyBatis는 JDBC의 PreparedStatement를 사용하여 SQL 문을 실행하기 전에 파라미터 값을 자동으로 처리합니다.
    - #은 자동으로 값에 대한 적절한 따옴표 처리 및 SQL 인젝션 방지를 수행합니다. 이는 일반적으로 매개변수로 사용자 입력을 처리할 때 권장되는 방법입니다.
    - 예시:이 경우 MyBatis는 **`#{userId}`**를 JDBC의 PreparedStatement 매개변수로 변환하고 값을 적절하게 처리하여 SQL을 실행합니다.
        
        ```
        xmlCopy code
        SELECT * FROM users WHERE id = #{userId}
        
        ```
        
2. $ (문자 치환):
    - $를 사용하면 MyBatis는 주어진 문자열 그대로 SQL 문에 치환합니다. 따라서 문자열 치환이 발생하고 SQL 문이 동적으로 생성될 수 있습니다.
    - $는 문자열 보간에 사용되며, 자동 따옴표 처리나 SQL 인젝션 방지 기능이 없습니다. 따라서 주의해야 합니다.
    - 예시:이 경우 MyBatis는 **`${userId}`**를 주어진 값으로 치환하고, 그 결과로 만들어진 SQL을 실행합니다. 이는 동적인 SQL 구문을 작성할 때 유용할 수 있습니다.
        
        ```
        xmlCopy code
        SELECT * FROM users WHERE id = ${userId}
        
        ```
        

주의할 점은 $를 사용하면 입력 값에 대한 적절한 따옴표 처리 및 SQL 인젝션 방지를 수동으로 처리해야 하므로 보안 문제에 노출될 수 있습니다. 따라서 사용자 입력과 같은 외부 데이터를 처리할 때는 주로 #을 사용하는 것이 좋습니다. 그러나 동적인 SQL 작성이 필요한 경우에는 $를 사용할 수 있습니다.
