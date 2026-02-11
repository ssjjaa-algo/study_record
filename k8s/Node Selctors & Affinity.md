## 1. 노드 셀렉터 (Node Selector)

가장 간단하고 기초적인 포드 배치 제한 방법

- **예시:** 3개의 노드 중 2개는 저사양, 1개는 고사양인 클러스터에서 데이터 처리 작업(고성능 필요)을 고사양 노드에만 할당하려는 상황
- **기본 동작:** 별도 설정이 없으면 스케줄러가 모든 노드에 포드를 무작위로 배치하므로, 특정 노드에만 실행되도록 제한이 필요함
- **작동 원리:** 포드 정의 파일의 `spec` 섹션에 `nodeSelector` 속성을 추가하여 특정 라벨을 지정함
- **라벨링:** 스케줄러가 노드를 식별할 수 있도록 **먼저 노드에 라벨을 부여해야 함**
    - `kubectl label nodes <노드이름> <키>=<값>` (예: `kubectl label nodes node1 size=large`)
- **한계점:** 복잡한 요구사항(예: Large 또는 Medium 노드에 배치, Small이 아닌 노드에 배치 등)을 처리할 수 있는 고급 표현식(OR, NOT 등) 지원 불가

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: myapp-pod
spec:
  containers:
  - name: data-processor
    image: data-processor
  nodeSelector:
    size: large # size=large라는 라벨을 붙임
```

---

## 2. 노드 어피니티 (Node Affinity)

노드 셀렉터의 한계를 극복하고 복잡한 배치 로직을 지원하기 위해 도입된 기능

- **주요 목적:** 고급 표현식을 사용하여 포드가 특정 노드에 호스팅되도록 보장
- **구조적 특징:** `spec.affinity.nodeAffinity` 아래에 정의
- **연산자(Operators) 종류:**
    - **In:** 라벨 값이 지정된 리스트 중 하나에 포함되는지 확인 (예: `values: [large, medium]`)
    - **NotIn:** 라벨 값이 리스트에 포함되지 않는지 확인 (예: `size NotIn [small]`)
    - **Exists:** 특정 라벨 키가 노드에 존재하는지만 확인 (값 비교 불필요)
    - 기타 여러 연산자가 존재하며 공식 문서에서 확인 가능

```yaml
spec:
  affinity:
    nodeAffinity:
      requiredDuringSchedulingIgnoredDuringExecution:
        nodeSelectorTerms:
        - matchExpressions:
          - key: size
            operator: In
            values:
            - large
            - medium
```

---

### 1. 두 가지 생애주기 단계 (Lifecycle Stages)

노드 어피니티 속성명에 포함된 두 단계의 의미는 다음과 같음

- **DuringScheduling:** 포드가 **`처음 생성되어`** 어느 노드에 배치될지 결정하는 시점임
- **DuringExecution:** 포드가 이미 특정 노드에 배치되어 **`실행 중인 상태`**를 의미함

---

### 2. 현재 사용 가능한 주요 타입

### **A. requiredDuringSchedulingIgnoredDuringExecution (강제 규칙)**

- **스케줄링 시점:** 지정된 어피니티 규칙을 **반드시** 만족해야 함
- **동작:** 조건에 맞는 노드가 클러스터에 없을 경우 포드는 스케줄링되지 않고 보류(Pending) 상태로 유지됨
- **실행 시점:** 포드가 실행된 후 노드의 라벨이 변경되어 규칙과 맞지 않게 되더라도, 이미 실행 중인 포드는 퇴출되지 않고 계속 실행됨(Ignored)

### **B. preferredDuringSchedulingIgnoredDuringExecution (선호 규칙)**

- **스케줄링 시점:** 규칙을 만족하는 노드 배치를 **최대한 시도**하지만 필수는 아님
- **동작:** 조건에 맞는 노드를 우선적으로 찾되, 만약 일치하는 노드가 없다면 스케줄러가 규칙을 무시하고 사용 가능한 다른 노드에 포드를 배치함
- **실행 시점:** 강제 규칙과 마찬가지로 포드 실행 중에 발생하는 노드 라벨 변경은 기존 포드에 영향을 주지 않음(Ignored)

---

### 3. 향후 계획된 타입

### **requiredDuringSchedulingRequiredDuringExecution (강제 및 즉시 퇴출)**

- **특징:** 스케줄링 시점뿐만 아니라 실행 중에도 규칙 준수를 강제함
- **동작:** 포드가 실행 중인 노드의 라벨이 관리자에 의해 삭제되거나 변경되어 규칙을 더 이상 만족하지 못하게 되면, 해당 포드를 즉시 노드에서 퇴출시키고 다시 스케줄링