## 모니터링 대상

- 노드 레벨 메트릭
    - 클러스터의 노드 개수, 정상(healthy) 노드 개수, 기타 성능 메트릭 등
- Pod 레벨 메트릭
    - Pod 개수, Pod 성능 메트릭 등
- 위 메트릭을 모니터링, 저장, 분석하는 기능 필요
    - `Observability`
- 사용 가능한 오픈소스 솔루션 예시
    - Metrics Server
    - Prometheus
    - Elastic Stack
- 사용 가능한 상용 솔루션 예시
    - Datadog
    - Dynatrace

## Metrics Server 특성

- Hipster를 축소한 버전이 Metrics Server라고 함
- Kubernetes 클러스터당 Metrics Server 1개 배치 가능
- Metrics Server가 각 노드와 Pod에서 메트릭을 수집하는 동작
    - 수집한 메트릭을 집계하고 메모리에 저장
    - Metrics Server는 in-memory 모니터링 솔루션
- 디스크에 메트릭을 저장하지 않음
    - 과거성능 데이터를 볼 수 없음
    - 과거 데이터를 보려면 앞에서 언급한 고급 모니터링 솔루션에 의존 필요

## Pod 메트릭 생성 방식

- 각 노드에 Kubelet이라는 에이전트가 실행되는 구성
- Kubelet이 Kubernetes API / master server로부터 명령을 받아 노드에서 Pod를 실행
    - Kubelet 내부에 `cAdvisor(Container Advisor)` 서브컴포넌트 존재
    - cAdvisor가 Pod의 성능 메트릭을 수집
    - cAdvisor가 Kubelet API를 통해 메트릭을 노출
    - Metrics Server가 이 메트릭을 사용할 수 있도록 한다
- 모니터링 툴은 endpoint를 노출하여 지표를 제공하고 그것을 가져가는 것들이 대부분의 형태
    - spring actuator는 지표를 노출하고 prometheus는 지표를 수집할 수 있다. 그것과 비슷한 개념처럼 보임

## Metrics Server 설치(실습)

- 로컬 클러스터로 minikube 사용 시
    - `minikube addons` 명령 실행
    - metrics-server 애드온 활성화

```bash
minikube addons enable metrics-server
```

- 그 외 환경에서
    - GitHub 저장소에서 metrics-server 배포 파일을 클론
    - kubectl create 명령으로 필요한 컴포넌트 배포

```bash
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```

```yaml
serviceaccount/metrics-server created
clusterrole.rbac.authorization.k8s.io/system:aggregated-metrics-reader created
clusterrole.rbac.authorization.k8s.io/system:metrics-server created
rolebinding.rbac.authorization.k8s.io/metrics-server-auth-reader created
clusterrolebinding.rbac.authorization.k8s.io/metrics-server:system:auth-delegator created
clusterrolebinding.rbac.authorization.k8s.io/system:metrics-server created
service/metrics-server created
deployment.apps/metrics-server created
apiservice.apiregistration.k8s.io/v1beta1.metrics.k8s.io created
```

- Metrics Server가 노드에서 성능 메트릭을 polling 할 수 있도록 구성되는 동작

## 수집/처리 대기

- 배포 후 Metrics Server가 데이터를 수집하고 처리할 시간을 필요로 함

## 메트릭 조회 명령

- 성능 조회는 `k top node/pod`로 수행

```bash
kubectl top node
```

```bash
kubectl top pod
```