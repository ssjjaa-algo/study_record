# 1. Array

- 연관된 data를 메모리상에 **`연속적, 순차적`**으로 미리 할당된 크기만큼 저장

- 미리 예상한 것보다 더 많은 수의 data를 저장해서 Array의 size가 넘어선 경우
    - 기존의 size보다 더 큰 Array 만든 후 옮기는 수밖에 없다.
    

그래서, **`Dynamic Array`**

- 저장공간이 가득 차게 되면 resize하여 유동적으로 size를 조절하여 데이터를 저장하는 자료구조

- resize 방식
    - Doubling
        - 이 때는 O(n)의 시간복잡도
