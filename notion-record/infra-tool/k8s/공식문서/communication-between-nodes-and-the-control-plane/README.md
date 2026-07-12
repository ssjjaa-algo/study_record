# Communication between Nodes and the Control Plane

- k8s는 hub-and-spoke API 패턴을 사용한다.
    - 노드에서 발생하는 모든 api가 api 서버에서 종료된다
    - 점장이 api-server이고, 요리사가 spoker라고 한다면
        - 요리사들은 점장이랑만 통화. 즉 요리사는 api-server와 연결되어 있는 컴포넌트들
        - 말하고자 하는 포인트는 component들끼리 통신하지 않는다.

- aws에서 해석한 원문을 읽어보는게 좋을 거 같음.
    - https://whchoi98.gitbook.io/k8s/kubernetes-concept/cluster-architecture/control-plane-node-communication
