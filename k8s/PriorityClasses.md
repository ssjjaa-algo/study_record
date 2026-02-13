## 개요

- Kubernetes 컨트롤 플레인 구성 요소도 클러스터 내부에서 Pod로 실행되는 구조
- 컨트롤 플레인 워크로드는 어떤 일이 있어도 항상 실행되어야 하는 워크로드
- 중요한 데이터베이스, 크리티컬 애플리케이션, 백그라운드 잡 같은 낮은 우선순위 워크로드가 함께 존재하는 상황
- 이러한 상황에서 **높은 우선순위 워크로드가 낮은 우선순위 워크로드에 의해 방해받지 않고 항상 스케줄링되도록** 보장하는 필요
- 이를 위한 기능으로 **`PriorityClass`** 사용

## PriorityClass의 역할

- 워크로드별 우선순위 정의 기능
- **높은 우선순위 워크로드가 낮은 우선순위 워크로드보다 우선권을 갖도록 보장**
- 높은 우선순위 Pod가 스케줄링되지 못하면 스케줄러가 낮은 우선순위 워크로드를 종료(terminate)하여 스케줄링 시도 가능

## PriorityClass의 범위

- PriorityClass는 non-namespaced 오브젝트
    - 특정 네임스페이스에 속하지 않는 클러스터 **전역 리소스**
- 한 번 생성되면 어떤 네임스페이스의 Pod에도 설정 가능

## 우선순위 값 범위

- 애플리케이션/워크로드용 범위는 최대 10억, 최소 약 -20억 수준
- 값이 클수록 더 높은 우선순위
- Kubernetes 내부 system critical 구성 요소는 별도 범위 사용
- system critical 구성 요소는 항상 최고 우선순위를 가져야 하는 전제
    - 기본적으로 2개의 요소가 존재
    - 공식문서 내용
      - **Kubernetes already ships with two PriorityClasses: `system-cluster-critical` and `system-node-critical`. These are common classes and are used to [ensure that critical components are always scheduled first](https://kubernetes.io/docs/tasks/administer-cluster/guaranteed-scheduling-critical-addon-pods/).**
          - kube-proxy, CoreDns같은 것이 예시
    - system용 우선순위는 20억 수준까지 사용 가능

## PriorityClass 생성

- description은 optional

```yaml
apiVersion: scheduling.k8s.io/v1
kind: PriorityClass
metadata:
  name: critical-app-priority
value: 7000000
description: "Priority class for critical applications" # 생략 가능
```

## Pod에 PriorityClass 연결

- Pod 정의 내부에서 priorityClassName 속성 사용
- Pod 생성 시 해당 PriorityClass에 할당된 우선순위를 가진 것으로 간주
- priorityClassName을 지정하지 않으면 기본 우선순위 값 0 적용

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: critical-app
spec:
  priorityClassName: critical-app-priority # PriorityClass와 매칭
  containers:
    - name: app
      image: nginx
```

## globalDefault로 기본 우선순위 변경

- 기본 우선순위를 0이 아닌 값으로 바꾸려면 **새 PriorityClass를 만들어야함**
- globalDefault 속성을 true로 설정 필요
- priorityClassName이 없는 모든 Pod의 기본 우선순위를 정의하는 용도
- globalDefault는 단 하나의 PriorityClass에만 설정 가능
- 기본값이 여러 개일 수 없기 때문에 중복 설정 불가

```yaml
apiVersion: scheduling.k8s.io/v1
kind: PriorityClass
metadata:
  name: global-default-priority
value: 100
globalDefault: true
description: "Default priority for pods without priorityClassName"

# globalDefault가 false인 경우 : Pod에 명시할 경우에만 PriorityClass가 적용
# globalDefault가 true인 경우 : Pod에 명시하지 않아도 모두 Default값을 가지게 된다
```

## Pod Priority 효과 시나리오 1

- 두 워크로드가 스케줄링되는 상황
    - critical app 우선순위 7
    - jobs app 우선순위 5
- critical app이 더 높은 우선순위이므로 먼저 배치
- 클러스터 자원이 남아 있으면 jobs app도 다음으로 배치

## Pod Priority 효과 시나리오 2

- 더 높은 우선순위 잡이 새로 들어오는 상황
    - 새 워크로드 우선순위 6
    - 클러스터에 남은 자원이 없는 상태
- **기존 워크로드를 종료/대기할 지의 선택 발생 = `PreemptionPolicy`**

## PreemptionPolicy 동작

- 동작은 새 워크로드에 할당된 PriorityClass의 preemptionPolicy로 결정
- preemptionPolicy 미설정 시 기본값은 낮은 우선순위 선점 동작
- 기본값은 기존 낮은 우선순위 워크로드를 종료시키고 자리를 차지하는 방식
- 기존 워크로드를 죽이거나 퇴거시키지 않고 기다리려면 preemptionPolicy를 Never로 설정 필요
- preemptionPolicy: Never 설정 시 non-preempting 상태
- 자원이 부족하면 스케줄링 큐에서 대기
- 대기 중에도 스케줄링 우선순위는 적용되어 낮은 우선순위 대기 Pod보다 먼저 스케줄링 가능
- 스케줄링될 때의 우선권과 기존 Pod를 죽여 선점하는지 여부의 분리 개념 포함
- 선점 여부는 preemptionPolicy가 결정

```yaml
apiVersion: scheduling.k8s.io/v1
kind: PriorityClass
metadata:
  name: preempt-lower-priority
value: 600
preemptionPolicy: PreemptLowerPriority
description: "선점 가능"

```

```yaml
apiVersion: scheduling.k8s.io/v1
kind: PriorityClass
metadata:
  name: never-preempt
value: 600
preemptionPolicy: Never
description: "선점 불가하고 스케줄링 큐에서 대기"

```