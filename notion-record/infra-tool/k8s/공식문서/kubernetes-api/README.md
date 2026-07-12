# Kubernetes API

- Kubernetes controle plane의 core가 API server
- HTTP API 제공
- API 객체(Pod, namespace, ConfigMap, 이벤트)의 상태를 쿼리하고 조작한다.
- kubectl, kubeadm
    - api 사용
- Discovery API
    - Kubernetes API에 대한 정보 제공
        - API 이름, 리소스, 버전..
    - Kubernetes 특정 용어. OpenAPI와 별도이다.
- OpenAPI
    - https://kubernetes.io/docs/concepts/overview/kubernetes-api/#openapi-interface-definition
    - DIscovery API 사양보다 훨씬 크다는데 문서 참고해야 할 듯.

## Discovery API

- 아.. 이해 못하겠다. 아직은.

## OpenAPI interface definition

- https://www.openapis.org/
- openapi v3.0이 선호됨
-
