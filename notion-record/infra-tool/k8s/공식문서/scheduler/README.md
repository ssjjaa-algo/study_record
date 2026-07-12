# Scheduler

# kube-scheduler

- default scheduler
    - control plane의 일부
    - 자신만의 scheduling 만들어서 Customizing 가능

- 새로 생성된 파드 / 아직 scheduling 되지 않은 pod를 실행할 최적의 node를 찾아낸다
- 파드 안의 컨테이너 / 파드 자체의 요구사항은 제각각
    - 따라서 요구사항에 충족되지 않는 것들부터 우선 filtering
    - api를 통해 특정 노드도 지정 가능, 이거는 특수한 경우에만 사용된다고 한다(공식문서)

- 파드의 스케줄링 요구를 충족하는 노드 = **feasible node**
    - 적합한 노드 못찾으면 pod는 **`pending`, 스케줄링  되지 않은 채로 남아있는다.**

- 스케줄러는 적합한 노드들을 찾고, 그들을 점수화하고 가장 높은 점수를 받은 노드에 파드 배치
    - 점수화한다고? 이거는 조건이 얼마나 맞는지를 따지는 건가?
        - 여러 요소가 있을 것으로 추정.. 모름
    - 이 결정을 바인딩 과정에서 api 서버에 알린다.
