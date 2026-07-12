# 5. multi process vs multi thread

태그: Context, 자원

## 유사점

- 동시에 여러작업을 수행한다

### Multi thread

<aside>
💡 **`데이터 공유가 빈번, many context switching, 효율적 자원 관리 필요 시 유리`**

</aside>

- 적은 메모리 공간 차지
- Context Switching이 빠르다
    - 캐시 메모리를 **`초기화할 필요가 없어서`**
- 동기화 문제
    - 하나의 쓰레드 장애로 전체 종료될 위험 존재

### Multi process

<aside>
💡 **`메모리 구분이 필요할 때 사용한다면 유리`**

</aside>

- 많은 메모리 공간, CPU시간 차지
- Context Switching이 느리다
    - 캐시 메모리 초기화 필요
- 하나의 process가 죽더라도 다른 process에 영향을 주지 않는다
    - **`안정성`**

## Multi thread가  Multi process 보다 좋은 점

- 메모리 공간과 시스템 자원 소모 줄어듬
- process를 생성하고 자원을 할당하는 system call 생략
    - 자원을 효율적으로 관리
- Context Switching이 빠르다
    - 캐시 메모리 **`초기화할 필요가 없어서`**
- 통신으로 인한 오버헤드가 적다
    - 통신 시 별도의 자원을 이용하지 않고 process에 할당된 Heap 영역 등을 이용하여 데이터를 주고받기 때문

## Multi thread가  Multi process 보다 안좋은 점

- 동기화 문제
    - 스레드 하나 장애가 전체의 장애를 불러일으킬 수 있다
