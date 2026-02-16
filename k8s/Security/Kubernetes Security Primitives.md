## 클러스터 호스트 보안

- 클러스터를 구성하는 호스트 접근 보안 필요
    - root 접근 비활성화
    - 비밀번호 기반 인증 비활성화
    - SSH 키 기반 인증만 허용
    - 등등
- Kubernetes를 호스팅하는 물리/가상 인프라 보안 조치 필요
- 인프라가 침해되면 전체가 침해

## Kubernetes 보안의 초점

- Kubernetes 관련 보안에 초점
- 위험 요소와 클러스터를 보호하기 위한 조치 확인

## API Server 접근 통제

- kube-apiserver가 Kubernetes 모든 작업의 중심
- kubectl로 kube-apiserver와 상호작용
- API를 직접 호출해서도 상호작용 가능
- 클러스터 거의 모든 작업이 API server를 통해 수행 가능
- 첫 번째 방어선은 **`API server 접근 통제`**

## 두 가지 결정

- **`누가`** 클러스터에 접근 가능한지
- 접근한 주체가 **`무엇`**을 할 수 있는지

## Authentication

- API server에 누가 접근할 수 있는지는 authentication으로 정의
- 인증 방식 예시
    - certificates
    - LDAP 같은 외부 인증 제공자 연동
    - 머신(프로그램)에는 service accounts 사용

## Authorization

- 접근한 주체가 무엇을 할 수 있는지는 authorization으로 정의
- RBAC
- ABAC
- Node Authorizer
- Webhooks

## 컴포넌트 간 통신 보안(TLS)

- 클러스터 내부 컴포넌트 간 통신은 **`TLS`**로 암호화
- 대상 컴포넌트 예시
    - etcd cluster
    - kube-controller-manager
    - kube-scheduler
    - kube-apiserver
    - worker node의 kubelet
    - worker node의 kube-proxy

## Pod 간 통신과 NetworkPolicy

- 기본적으로 모든 Pod는 클러스터 내 다른 모든 Pod에 접근 가능
- network policies로 Pod 간 접근 제한 가능