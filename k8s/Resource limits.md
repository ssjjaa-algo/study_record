### **1. 클러스터 리소스와 스케줄러의 역할**

- **노드 자원:** 3개 노드로 구성된 클러스터에서 각 노드는 사용 가능한 CPU와 메모리 자원 세트를 보유함
- **자원 소비:** 포드 실행 시 노드의 자원을 소비, 예를 들어 특정 포드가 2 CPU와 1 메모리 유닛을 요구할 수 있음
- **스케줄러의 배치 결정:**
    - 포드가 요구하는 자원량과 노드의 가용 자원량을 고려하여 최적의 노드를 식별함
    - 가용 자원이 충분한 노드에 포드를 배치함
    - 모든 노드에 자원이 부족할 경우 배치를 보류하고 **Pending(대기)** 상태로 유지함
- **이벤트 확인:** `kubectl describe pod` 명령어를 통해 'Insufficient CPU' 등 자원 부족 관련 이벤트 확인 가능함

---

### **2. 리소스 요청**

컨테이너 실행을 위해 필요한 **최소한의 자원량** 정의

- **정의:** 컨테이너가 요청하는 CPU 또는 메모리의 최소량임
- **작동:** 스케줄러가 포드를 배치할 노드를 찾을 때 이 수치를 기준으로 충분한 공간이 있는지 확인하며, 배치 후 해당 자원량을 보장(Guaranteed)함
- `spec.containers` 아래 `resources` 섹션을 추가하고 그 하위에 `requests` 필드를 사용하여 메모리와 CPU 값 입력함
    - 예: 메모리 4Gi, CPU 2개 설정

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: resource-demo-pod
  labels:
    app: data-processor
spec:
  containers:
  - name: processor-container
    image: nginx
    resources:
      requests:
        cpu: "2"           
        memory: "4Gi"      

# cpu와 memory는 수치를 정할 수 있음 = 0.1로도 표현 가능하고, 500m 로도 표현 가능함
```

---

### **3. 리소스 단위**

### **CPU 유닛**

- **최소 단위:** 0.1 CPU 또는 100m(milli)로 표현 가능하며, 최소 1m까지 설정 가능함
- **상대적 가치:** 1 CPU는 클라우드 환경에 따라 다음과 같이 간주됨
    - **AWS:** 1 vCPU
    - **GCP / Azure:** 1 Core
    - **기타:** 1 Hyperthread
- **확장성:** 노드 자원이 충분하다면 1개 이상의 높은 수치(예: 5 CPU) 요청도 가능함

### **Memory 유닛**

- **표기 방식:** 256Mi(Mebibyte) 등 접미사 사용 또는 정수 값 사용함
- **단위 차이점:**
    - **G (Gigabyte):** 1000×1000×1000 바이트 (1000MB)
    - **Gi (Gibibyte):** 1024×1024×1024 바이트 (1024MB)
    - M/Mi, K/Ki 단위에도 동일한 기준이 적용됨

---

### **4. 리소스 제한 (Resource Limits)**

- **기본 동작:** 별도의 제한이 없으면 **컨테이너는 노드의 모든 자원을 소비할 수 있으며**, 이는 노드 내 다른 프로세스나 컨테이너의 자원을 고갈(Suffocate)시킬 위험이 있음
- **제한 설정:** `resources` 섹션 아래 `limits` 필드를 사용하여 메모리와 CPU의 상한선 정의함
- **다중 컨테이너:** 포드 내 각 컨테이너별로 독립적인 요청 및 제한 설정이 가능함
    - 아래 yaml은 container마다 requests, limits를 설정할 수 있는 예시

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: multi-container-resource-limits
  labels:
    app: complex-app
spec:
  containers:
  - name: main-app                
    image: nginx
    resources:
      requests:
        cpu: "1"                  
        memory: "512Mi"           
      limits:
        cpu: "2"                  
        memory: "1Gi"             

  - name: log-sidecar             
    image: busybox
    resources:
      requests:
        cpu: "100m"               
        memory: "64Mi"            
      limits:
        cpu: "500m"              
        memory: "128Mi"           
```

---

### **5. 제한 초과 시 동작 (Throttling vs OOMKilled)**

- **CPU 초과 시:** 시스템이 CPU 사용량을 스로틀링(Throttling)하여 지정된 제한을 넘지 못하도록 억제함 (한도 이상의 CPU 사용 불가)
- **메모리 초과 시:** 컨테이너는 일시적으로 한도 이상의 메모리를 사용할 수 있으나, 지속적으로 한도를 초과할 경우 포드가 강제 종료(Terminated)됨
    - **OOMKilled:** 'Out of Memory Kill' 에러가 로그나 `describe` 출력에 표시되며 포드가 종료됨

---

### **6. 리소스 설정 시나리오 분석**

| 시나리오 | 동작 방식 및 특징 | 비고 |
| --- | --- | --- |
| **요청/제한 모두 미설정** | 포드가 노드 자원을 무제한 소비하여 타 프로세스에 영향을 줄 수 있음 | 쿠버네티스 기본 상태 |
| **제한만 설정** | 쿠버네티스가 **요청(Requests) 값을 제한(Limits) 값과 동일하게** 자동 설정함 | 요청=제한으로 간주 |
| **요청 & 제한 모두 설정** | 요청만큼 보장받으며, 유휴 자원이 있을 때 제한까지 확장 가능함 | 이상적인 시나리오 중 하나 |
| **요청만 설정** | 요청만큼 보장받고 상한선 없이 가용 자원 모두 소비 가능함 | CPU 활용에 효율적이나 메모리 시 위험 |

---

### **7. 네임스페이스 수준의 제어 (LimitRange & ResourceQuota)**

### **LimitRange (포드별 기본값 및 제약)**

- **목적:** 자원 설정이 누락된 경우 자동으로 기본값을 설정하거나 최소/최대 범위를 강제함
- **주요 필드:**
    - **default:** 기본 제한(Limit) 값
    - **defaultRequest:** 기본 요청(Request) 값
    - **max / min:** 컨테이너가 가질 수 있는 자원량의 상한 및 하한선
- **특징:** 네임스페이스 단위로 적용되며, **생성 이후 시점의 새 포드에만 영향을 줌**

```yaml
apiVersion: v1
kind: LimitRange
metadata:
  name: cpu-mem-limit-range
  namespace: dev               # 해당 네임스페이스에 적용
spec:
  limits:
  - type: Container            # 컨테이너 단위로 제약 설정
    default:                   # 1. 기본 제한(Limit) 값: 설정 누락 시 자동 적용
      cpu: "500m"
      memory: "512Mi"
    defaultRequest:            # 2. 기본 요청(Request) 값: 설정 누락 시 자동 적용
      cpu: "200m"
      memory: "256Mi"
    max:                       # 3. 상한선: 컨테이너가 이 이상의 값을 가질 수 없음
      cpu: "2"
      memory: "1Gi"
    min:                       # 4. 하한선: 컨테이너가 이 이하의 값을 요청할 수 없음
      cpu: "100m"
      memory: "128Mi"
```

### **ResourceQuota (네임스페이스 전체 총량)**

- **목적:** 특정 네임스페이스 내 모든 포드가 사용하는 **자원의 합계를 제한**함
- **설정 항목:** 요청(Requests)의 총합 및 제한(Limits)의 총합에 대한 하드 리밋(Hard Limit) 설정함
    - 예: 네임스페이스 전체 CPU 합계 4, 메모리 합계 4Gi 등

```yaml
apiVersion: v1
kind: ResourceQuota
metadata:
  name: compute-resources-quota
  namespace: dev               # 해당 네임스페이스 전체에 적용
spec:
  hard:
    requests.cpu: "4"          # 네임스페이스 내 모든 포드의 CPU 요청 합계 제한
    requests.memory: "4Gi"     # 네임스페이스 내 모든 포드의 메모리 요청 합계 제한
    limits.cpu: "10"           # 네임스페이스 내 모든 포드의 CPU 제한 합계 제한
    limits.memory: "10Gi"      # 네임스페이스 내 모든 포드의 메모리 제한 합계 제한
```

- 공식문서 resourcequota 잔여 확인 참고
    - The output shows the quota along with how much of the quota has been used. You can see that the memory and CPU requests and limits for your Pod do not exceed the quota.

    ```bash
    kubectl get resourcequota mem-cpu-demo --namespace=quota-mem-cpu-example --output=yaml
    ```

    ```bash
    status:
      hard:
        limits.cpu: "2"
        limits.memory: 2Gi
        requests.cpu: "1"
        requests.memory: 1Gi
      used:
        limits.cpu: 800m
        limits.memory: 800Mi
        requests.cpu: 400m
        requests.memory: 600Mi
    ```