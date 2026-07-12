# BOJ11437 - LCA

태그: 최적화

[11437번: LCA](https://www.acmicpc.net/problem/11437)

1. 각 정점에 대하여 정점의 정보를 저장해둔다(fillInfoOfNode)
    - int[] infoOfNode[N][2] 선언
    - 배열에 저장되는 것은 나의 부모 / 나의 깊이

1. 두 정점의 깊이를 맞춘다 (matchDepth)

1. 깊이를 맞춘 상태에서 깊이를 1개씩 내려가며 둘의 공통 조상을 찾을 때 까지 반복

## 최적화

- 희소 배열 이용
    - 몰라..
