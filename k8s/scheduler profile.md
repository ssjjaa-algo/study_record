## 스케줄링 예시 시나리오

- Kubernetes 클러스터에 4개의 노드 존재, Pod가 4개 노드 중 하나에 스케줄링되기를 대기 중인 상태
    - 각각 CPU가 `4,` `4`, `12`, `16`이라고 가정
- Pod의 리소스 요구사항이 CPU 10인 상태
- CPU 10이 남아 있는 노드에만 스케줄링 가능한 조건
- 해당 Pod 외에도 스케줄링을 기다리는 다른 Pod들이 존재하는 상황

## 1. Scheduling Queue 단계

- Pod는 scheduling queue에서 스케줄링 대기 수행
- 높은 priority를 가진 Pod가 queue의 앞쪽으로 이동하여 먼저 스케줄링된다

```yaml
apiVersion: scheduling.k8s.io/v1
kind: PriorityClass
metadata:
  name: high-priority
value: 1000000
description: "high priority pods"

```

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: my-pod
spec:
  priorityClassName: high-priority
  containers:
    - name: app
      image: nginx
      resources:
        requests:
          cpu: "10"
```

## 2. Filter 단계

- Pod를 실행할 수 없는 노드들은 제외된다
- yaml파일에서 cpu는 10을 요구하기 때문에 10이 없는 노드들은 제외

## 3. Scoring 단계

- 남은 노드들에 대해 점수를 매기는 단계
- Pod에 CPU를 예약한 뒤 남는 여유 자원을 기준으로 점수 산정
    - 위의 Pod가 10을 요구하고, 남은 노드의 리소스가 각각 12, 16
        - 16의 노드는 할당 후 **`6이 남아서 선택됨 (더 많이 남아서)`**
- 점수가 더 높은 노드가 선택

## 4. Binding 단계

- 점수가 가장 높은 노드에 Pod를 최종적으로 바인딩하는 단계

## 각 단계(1, 2, 3, 4)는 모두 플러그인으로 동작

- `scheduling queue` 단계
    - Priority Sort 플러그인이 `priority 기준 정렬` 수행
- `filter` 단계
    - NodeResourcesFit 플러그인이 노드 별 리소스를 확인하고 필터링
    - NodeName 플러그인이 Pod spec에 nodeName이 있는지 확인
    - NodeUnschedulable 플러그인이 Unschedulable 플래그가 true인 노드를 필터링하는 동작
- `scoring` 단계
    - 할당 가능한 노드에 점수를 부여
    - Pod가 할당된 뒤 남는 리소스를 기준으로 점수 산정 수행
    - image locality 플러그인이 Pod가 사용할 컨테이너 이미지가 이미 존재하는 노드에 높은 점수 부여
        - 이건 **제한이 아니며** 이미지가 있는 노드가 없으면 이미지가 없는 노드에도 Pod 배치 가능

## Binding 단계 플러그인

- binding 단계에서 default binder 플러그인이 바인딩 메커니즘 제공

## Extension Points 개념

- 자체 플러그인이 필요하다면 작성 후 `원하는 위치(=단계)`에 연결 할 수 있다

## 주요 Extension Points와 플러그인 연결 위치

- scheduling queue에 sort extension 존재
- sort extension에 Priority Sort 플러그인 연결
- filter extension 존재
- score extension 존재
- bind extension 존재
- 각 extension point에 해당 단계 플러그인들이 연결되는 구조

## 추가 Extension Points 목록

- https://kubernetes.io/ko/docs/reference/scheduling/config/ (공식문서)
    - queueSort, preFilter, filter, postFilter.. 이런 개념들
        - 이것은 Spring bean lifecycle이나 filter, interceptor 등의 흐름과 비슷함

## Kubernetes 1.18의 Multi Scheduling Profiles

- Kubernetes 1.18 릴리스에서 단일 스케줄러 내 다중 프로필 지원 기능 도입
- 단일 스케줄러 설정 파일의 profiles 리스트에 여러 엔트리를 추가하는 방식
- 각 profile마다 서로 다른 schedulerName 지정 방식
- 각 schedulerName이 별도 스케줄러처럼 동작하는 구조
- 별도 바이너리 대신 하나의 바이너리 안에서 여러 스케줄러 프로필이 동작하는 구조

```yaml
apiVersion: kubescheduler.config.k8s.io/v1
kind: KubeSchedulerConfiguration

profiles:
  - schedulerName: default-scheduler
  - schedulerName: scheduler-two
  - schedulerName: scheduler-three

```

## 프로필별로 동작을 다르게 만드는 방법

- 단순히 이름만 다르면 기본 스케줄러와 동일 동작 수행
- 동작 차별화를 위해 profile별 plugins 설정 커스터마이징 필요
- profile 아래 plugins 섹션에서 extension point별 플러그인 enable/disable 수행 가능
- 플러그인 이름 또는 패턴으로 enable/disable 수행 가능
- scheduler2 프로필에서 Taint and Toleration 플러그인을 비활성화하
- scheduler3 프로필에서 custom plugins를 활성화
- scheduler3 프로필에서 PreScore와 Score 플러그인을 모두 비활성화

```yaml
apiVersion: kubescheduler.config.k8s.io/v1
kind: KubeSchedulerConfiguration
profiles:
  - schedulerName: default-scheduler
  - schedulerName: scheduler-two
    plugins:
      filter:
        disabled:
          - name: TaintToleration
  - schedulerName: scheduler-three
    plugins:
      preScore:
        disabled:
          - name: "*"
      score:
        disabled:
          - name: "*"
      filter:
        enabled:
          - name: MyCustomPlugin

```