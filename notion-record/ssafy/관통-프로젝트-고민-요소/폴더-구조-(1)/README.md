# 폴더 구조 (1)

```java
com.ssafy.trip

├── OpenDataTourismBackApplication.java
├── config //각종 설정, 인터셉터, 필터 
│   └── datasource
├── controller //controller advice, controller
│   └── UserController.java
├── domain //각 도메인 로직 -> DAO(MyBatis)에서 가져온 값들과 매핑
│   └── user
│       ├── Nickname.java
│       └── User.java
├── dto
│   ├── request
│   └── response
├── exception
│   
├── service
│ 
└── util
    └── UrlParser.java
```

-
