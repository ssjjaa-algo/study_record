# Controller

https://whchoi98.gitbook.io/k8s/kubernetes-concept/cluster-architecture/controller

- 클러스터의 상태를 관찰하고 생성 / 변경을 요청하는 컨트롤 루프
    - 컨트롤 루프 : 실내 온도 조절기와 같은 역할.
        - 로보틱스 자동화에서는 시스템 상태를 조절하는 **`“종료되지 않는 루프”`**

## API 서버

- Job 컨트롤러 : 쿠버네티스 내장 컨트롤러
    - 클러스터 API 서버와 상호작용 하면서 상태 관리
    - 새로운 작업을 확인하면 클러스터 어딘가에 노드 집합의 kublet이 작업을 수행하기에 적합한 수의 파드를 실행한다.
        - 잡 컨트롤러가 직접 파드나 컨테이너를 스스로 실행하지 않는다.
            - 모든 Component들은 API 서버에게만 지시 (hub and spoke)

## 내장된 컨트롤러 집합

- kube-controller-manager.
- 쿠버네티스 외부에서 컨트롤러를 Custom에서 쓴다는 것은 어떤 경우일까
    - 일단 모두 API 서버랑 통신한다고 하는데.
        - Ingress Controller와 같이 Public end-point를 노출시킨 곳과 통신하게 된다.
        - 이건 그냥 어떻게 구현하기 나름. proxy 서버 두던지 등.
    - Operator, CI / CD
