### **1. 라벨과 선택기의 기본 개념**

- **라벨(Labels):** 각 아이템에 부착된 속성(Property)
    - 예: 동물을 분류할 때 '포유류/조류', '가축/야생', '초록색/빨간색' 등의 속성을 태그로 붙이는 것과 같음
- **선택기(Selectors):** 라벨을 기반으로 아이템을 필터링하는 도구
    - 예: "색상이 초록색인 포유류를 찾아줘"라는 조건을 통해 특정 그룹을 추출함
- **실생활 사례:** 유튜브 영상의 태그, 온라인 쇼핑몰의 카테고리 필터 등에서 흔히 볼 수 있음

---

### **2. 쿠버네티스에서의 활용**

클러스터에 수천 개의 오브젝트(포드, 서비스, 레플리카셋 등)가 있을 때, 이를 기능별, 애플리케이션별 또는 환경별(Dev/Prod)로 분류하기 위해 사용함

### **A. 오브젝트 필터링**

- 사용자가 특정 카테고리의 오브젝트만 조회하고 싶을 때 사용
- **명령어 예시:** `kubectl get pods --selector app=app1`

### **B. 객체 간 내부 연결 (가장 중요한 용도)**

- 쿠버네티스는 라벨과 선택기를 사용하여 서로 다른 리소스를 결합함
- **레플리카셋(ReplicaSet):** 자신이 관리해야 할 포드를 찾기 위해 선택기를 사용함
- **서비스(Service):** 트래픽을 전달할 대상 포드들을 식별하기 위해 선택기를 사용함

---

### **3. YAML 정의 및 주의사항**

### **포드(Pod) 정의 예시**

`metadata` 섹션 내의 `labels` 항목에 키-값 쌍으로 정의함

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: myapp-pod
  labels:
    app: myapp        # 라벨 정의
    function: front-end
spec:
  containers:
  - name: nginx-container
    image: nginx
```

### **레플리카셋(ReplicaSet)에서의 라벨 구분**

많이 실수하는 부분으로, 라벨이 두 곳에 정의됨

1. **최상위 metadata 라벨:** 레플리카셋 객체 자체의 라벨 (다른 객체가 레플리카셋을 찾을 때 사용)
2. **template 내부 라벨:** 생성될 **포드에 부여될 라벨**
3. **selector 라벨:** 레플리카셋이 감시할 포드의 라벨 (2번과 반드시 일치해야 함)

```yaml
apiVersion: apps/v1
kind: ReplicaSet
metadata:
  name: myapp-replicaset
  labels:             # 1. RS 자체의 라벨
    app: myapp
spec:
  replicas: 3
  selector:           # 3. 이 라벨을 가진 포드를 관리함
    matchLabels:
      app: myapp
  template:
    metadata:
      labels:         # 2. 생성될 포드에 붙는 라벨 (3번과 일치 필수)
        app: myapp
    spec:
      containers:
      - name: nginx
        image: nginx
```

---

### **4. 어노테이션 (Annotations)**

라벨이 객체를 그룹화하고 선택하는 **기능적** 용도라면, 어노테이션은 **정보 기록** 용도로 사용함

- **목적:** 도구 정보(이름, 버전, 빌드 정보), 연락처(전화번호, 이메일), 통합 작업을 위한 메타데이터 기록
- **특징:** 쿠버네티스가 어노테이션을 사용하여 오브젝트를 필터링하거나 선택하지는 않음 (단순 정보성)

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: myapp-pod
  annotations:
    build-version: "1.2.3"
    contact: "admin@example.com"
spec:
  containers:
  - name: nginx
    image: nginx
```

---

### **5. 요약 및 명령어**

| 기능 | 명령어/설정 | 비고 |
| --- | --- | --- |
| **라벨 기반 조회** | `kubectl get pods -l app=myapp` | `-l`은 `--selector`의 축약형 |
| **다중 조건 조회** | `kubectl get pods -l app=myapp,env=prod` | 쉼표로 여러 조건 결합 가능 |
| **라벨 수정/추가** | `kubectl label pods <이름> <키>=<값>` | 실행 중인 포드에 즉시 라벨 부여 |