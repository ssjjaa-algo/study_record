# 5. index

태그: B+tree

## 구조

- Btree, B+tree, Hash, Bitmap 등으로 구현 가능
    - 대부분 B+tree
- index 생성 시 특정 column 기준으로 정렬하여 데이터의 물리적 위치와 함께 별도의 파일에 저장
- index에 저장되는 속성 값 : search-key
- 실제 데이터의 물리적 위치에 저장한 값을 pointer
    - search-key와 pointer만을 저장하기 때문에 table보다 적은 공간을 차지

<aside>
💡 특정 column을 기준으로 search-key 값으로 설정하여 index를 생성하면 (search-key, pointer) 를 별도에 파일에 저장한 것이 index

</aside>

## 사용 이유

- Full Table Scan에 비해 searck-key가 정렬되어 저장되있기 때문에 **`SELECT WHERE 속도 증가`**

## 클러스터형 인덱스 & 보조 인덱스

- clustering index
    - 특정 column을 Primary Key로 지정 시 자동으로 생성, column을 기준으로 정렬이 되고 Table 자체가 index가 된다.

- secondary index
    - 일반 책의 찾아보기
    - unique key 또는 create index시 보조 index 생성
        - CREATE INDEX example_name ON table명 ( column )

## 장단점

- 장점
    - **`검색 속도 향상 (SELECT ~ WHERE)`**
        - Full Table Scan에 비해 훨씬 빠르게.

- 단점
    - 추가 공간
        - 보통 table 크기의 10% 차지하나 인덱스가 많아질수록 많이 차지함
    - 느린 **`데이터 변경`** 작업
        - 보통 B+tree 구조의 데이터 추가 삭제시 트리의 재구성이 필요
            - **`index의 재구성`**

## 자료구조

- B+Tree
    - 항상 정렬되있다
    - 등호 연산에 유리하게 작용
- Hash-Table
    - 사용되지 않음
    - 얘는 정렬도 되있지 않고, 등호 연산에 유리한 자료구조라 적절하지 않음.

[[DB] 10. B-Tree (B-트리)](https://rebro.kr/169)
