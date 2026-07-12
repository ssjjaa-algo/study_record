# 엔티티 매핑

# 객체와 테이블 매핑

## @Entity

- Entity가 붙은 클래스를 JPA가 관리

### 주의사항

- 기본 생성자(파라미터가 없는 public, 또는 protected 생성자)
- final, enum, interface, inner 클래스 사용 x

# 데이터베이스 스키마 자동 생성

- DDL을 애플리케이션 실행 시점에 자동 생성
- 운영 장비에 create, create-drop, update 사용하지 말 것
    - 개발 초기 단계에 create, update
    - 테스트 서버는 update, validate
    - 스테이징과 운영 서버는 validate, none

# 필드와 컬럼 매핑

- Temporal
    - 날짜 매핑 타입
-
