# 4. process VS thread 간단설명

## process

- 운영체제로부터 자원을 할당받는 작업의 단위
- 실행파일(program)이 메모리에 적재되어 CPU를 할당받아 실행되는 것
- 메모리 공간에 code, data, heap, stack 영역

## thread

- process가 할당받은 자원을 이용하는 실행의 단위
- 한 프로세스 내에서 실행되는 동작의 단위
- process 내에서 stack을 제외한 code, data, heap 영역 공유
