# BOJ2233 - 사과나무

## 주어진 이진수를 바탕으로 각 정점이 출현하는 위치를 기록한다

- position[2 * N + 1]의 배열을 선언한다.
- 예를 들어 position[4] = 2라고 했으면, 4번째 출현한 수가 2라는 것

## 문제에서 주어진 조건대로 트리를 구성해보자.

- 0이 나오다가 1이 나오는 순간은 어떤 숫자가 2번 나왔다는 의미

- 0이 나온 경우
    - position[현재 위치] = 숫자(cur)
    - stack에 cur을 넣어둔다
    - depth++ (여기에서 depth를 기록하지 않는다. 아직 depth가 정해지지 않음)

- 1이 나온 경우
    - stack에서 pop하면 해당하는 수가 나온다.
        - pop한 수를 temp에 넣어두고,
        - 이제 stack의 top에 있는 수가 temp의 부모가 된다.
    - 해당 숫자의 parent를 기록하고, depth를 기록해준다.
        - 현재 숫자의 parent와 depth를 기록한다?
            - parent[temp][0] = parent
            - parent[temp][1] = depth
        - 나의 부모와 depth를 기록했으면 depth를 다시 낮춰줘야 한다.
    

## lca를 찾기 위해 depth를 맞춘다.

- depth는 해당 숫자의 parent[cur][1]에 저장되있을 것이다.
- parent[x][1]과 parent[y][1]의 depth를 맞추고 공통 조상을 찾아준다.
