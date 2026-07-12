# 1. Primary Key

## 정의

- 후보 키(candidate key) 중 선택된 key
- 각 row를 unique하게 구분하는 column 또는 column의 집합
    - Null 안되고
    - 중복된 값 안되고
    - 테이블 당 1개만

## Relation

- 테이블에서 데이터베이스에서 사용하기 위한 조건을 갖춘 것
    - 테이블에서 cell은 단일 값을 가짐
    - 어떤 두 개의 row도 동일하지 않다.

### Super Key

- 각 row를 유일하게 식별할 수 있는 하나 이상의 속성들의 집합
    - (학번, 이름)
    - (학번)
    - …

### Candidate Key

- Super Key 중에서 쪼개질 수 없는 Super Key
- 각 row를 식별할 수 있는 **`최소한`**의 속성들의 집합
    - 최소성
    - (학번)
    - (주민등록번호)
- **`Primary Key가 될 수 있는 Key`**

### Alternate Key

- 후보키 중에서 기본키로 지정이 되지 못하고 남은 키
-
