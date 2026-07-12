# RDB vs NoSQL

태그: NoSQL

<aside>
💡 프로젝트마다 **`유리한 것`**을 사용

</aside>

## 관계형 데이터베이스(RDB)

- 사전에 엄격하게 정의된 DB Schema를 요구하는 table 기반 데이터 구조를 가짐
    - 엄격하게 정의되어 데이터 중복이 없음
    - **`데이터 update에 유리`**
    - 데이터 구조가 명확한 경우

## NoSQL - Not Only SQL

- 테이블 형식이 아닌 **`비정형 데이터`**를 저장할 수 있도록 지원
    - update가 적고 **`조회가 많을 때`** 유리
    - 정확한 데이터 구조가 정해지지 않은 경우 유리
    - scale out이 가능하므로 데이터 양이 많을 때 유리
    

### 등장 이유

- Big Data를 처리하기 위한 하나의 방식
    - 최신형 데이터들을 꼭 관계형으로 처리할 이유 x
- key-value storage system
    - 보통 SQL, Transaction 지원 x
- 종류
    - MongoDB, Bigtable, DynamoDB, Cassandra 등 …

### MongoDB

```sql

**Json 형식으로 지원**

db.createCollection("test")

db.test.insert({"id":12345, "name" : "test", "class" : ["a1", "a2"]})
```
