# [PCCP 기출문제] 1번

[](https://school.programmers.co.kr/learn/courses/30/lessons/250137)

# 문제 재정의

`bandage`는 [`시전 시간`, `초당 회복량`, `추가 회복량`] 형태의 길이가 3인 정수 배열입니다.

[5, 1, 5]라고 했을 때

- 5초 동안 붕대를 감으면서
- 초당 회복량은 1이며
- 5초 동안 붕대를 감았을 때 성공했을 시 추가 회복량 5

## 예외 조건

- 최대 체력보다 높은 체력이 될 수 없다.
- 몬스터에게 공격을 당하면 연속 성공 시간은 0으로 초기화된다.

## 계산 로직 정의

- (nextTime - prevTime - 1) / 시전 시간 = 추가 회복량의 가능성
- (nextTime - prevTime - 1) % 시전 시간 = 초당 회복량
- currentHp에 위의 값을 더하고 health보다 큰 경우 health로 초기화
- 더해진 값에서 next의 공격을 뺀 값이 0 이하라면 return -1
- 아닌 경우 쭉 계산하여 return currentHp
