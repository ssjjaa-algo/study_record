# 4.  Queue

태그: Q vs PQ, 스택 1개 큐 2개

- 선입선출(FIFO) 자료구조
    - 시간 순서상 먼저 집어 넣은 데이터가 나오는 구조
- 시간복잡도 enqueue O(1), dequeue O(1)
- Cache, 프로세스 관리, 너비우선 탐색 등 시 구현

- 활용 예시,  Circular queue

- 구현 방식
    - Array-based
        - enqueue와  dequeue 과정에서 메모리 낭비 발생
        - 따라서 **`원형 큐` 방식 구현이 좋다**
    - List-based
        - 재할당 및 메모리 낭비 걱정 X
    
- 확장 / 활용
    - 양쪽 enqueue, dequeue 가능한 **`deque`**
    - **`Priority-queue`**
    - scheduling, Cache 구현 등

- 원형 큐
    - Array-based 사용 시 메모리 낭비 공간 발생
        - resize시 퍼포먼스가 느려짐
    - 메모리를 효율적으로 사용하기 위해.
    - List-based
        - singly-linked-list
            - enqueue할 때마다 memory allocation, 전반적인 runtime 시 느릴 수 있음
            

# 큐 2개로 스택 1개 구현

- 삽입 큐 Q1
    - 그냥 삽입한다.

- 삭제 큐 Q2
    - Q1의 데이터를 1개를 제외하고 pop하여 Q2에 집어넣는다.
    - Q1에 남아있는 하나의 데이터를 pop한다. (LIFO)
    - Q2와 Q1의 이름을 바꾼다.
        - 데이터를 옮기는 작업에서 무조건  **`O(n)`**의 시간을 가짐
