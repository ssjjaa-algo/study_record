# Container Runtime Interface

https://kubernetes.io/docs/concepts/architecture/cri/

- 각각 Node에는 Container Runtime이 필수로 있어야 함.
- kublet과 Container Runtime이 통신하기 위해 CRI를 이용한다.
- CRI는 gRPC를 main protocol로 사용하도록 정의
    - HTTP/2를 이용하는 특성에 따라 TLS와 결합하여 사용하나?
