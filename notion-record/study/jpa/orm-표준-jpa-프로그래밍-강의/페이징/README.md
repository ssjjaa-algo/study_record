# 페이징

태그: spring data jpa

## 페이징과 정렬 파라미터

- org.springframework.data.domain.Sort
- org.springframework.data.domain.Pageable : 페이징 기능 (내부에 Sort 포함)

### 반환 타입

- [org.springframework.data.domain.Page](http://org.springframework.data.domain.Page) : 추가 Count 쿼리 결과를 포함하는 페이징
- [org.springframework.data.domain.](http://org.springframework.data.domain.Page)Slice : count 쿼리 결과 포함 x
    - limit 보다 1개의 사이즈를 더 늘려서, 다음 페이지가 있는 경우 일종의 **`더보기`** 제공임.

```java
PageRequest pageRequest = PageRqeust.of(몇 page, size, 정렬조건);

Page<Member> page = respository.findBy(?, pageRequest);
```

- Page는 인자로 Pagable이라는 interface를 받으므로, 구현체인 PageRequest를 삽입
- 받은 것을 dto로 변환
    - Page<Dto> toDto = page.map(entity → new Dto(내용));
