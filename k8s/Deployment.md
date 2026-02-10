## 쿠버네티스 디플로이먼트(Deployments) 상세 정리

운영 환경에서 애플리케이션을 배포하고 관리하기 위한 최상위 객체인 디플로이먼트에 대한 상세 내용

---

### 1. 운영 환경에서의 배포 요구사항

단순히 포드나 레플리카셋을 실행하는 것을 넘어, 실제 서비스 운영 시에는 다음과 같은 기능이 필수적임

- **무중단 업데이트:** 새로운 버전 배포 시 인스턴스를 하나씩 차례대로 교체하여 서비스 중단을 방지하는 **롤링 업데이트(Rolling Updates)** 기능
- **롤백(Rollback):** 업데이트 중 예기치 못한 오류 발생 시 이전 버전으로 즉시 되돌리는 기능
- **일시 중지 및 재개 (Pause/Resume):** 환경 설정 변경, 리소스 할당 수정 등 여러 변경 사항을 한꺼번에 적용하기 위해 작업을 잠시 멈췄다가 한 번에 반영하는 기능

---

### 2. 쿠버네티스 객체 계층 구조

디플로이먼트는 포드와 레플리카셋을 아우르는 상위 개념임

1. **Pod:** 애플리케이션의 최소 단위(컨테이너 캡슐화)
2. **ReplicaSet:** 지정된 수의 포드 복제본을 유지 및 관리
3. **Deployment:** 레플리카셋을 관리하며 업데이트 및 롤백 등 배포 전략을 수행

---

### 3. 디플로이먼트 정의 및 생성

### **YAML 정의 파일 구조**

디플로이먼트 정의 파일은 `kind` 필드를 제외하고 레플리카셋과 완전히 동일한 구조를 가짐

- **apiVersion:** `apps/v1`
- **kind:** `Deployment`
- **metadata:** 디플로이먼트 이름 및 라벨
- **spec:** 레플리카 수, 선택기(Selector), 포드 템플릿(Template) 포함

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: myapp-deployment
  labels:
    app: myapp
    type: front-end
spec:
  template:
    metadata:
      name: myapp-pod
      labels:
        app: myapp
        type: front-end
    spec:
      containers:
      - name: nginx-container
        image: nginx
  replicas: 3
  selector:
    matchLabels:
      type: front-end
```

---

### 4. 생성 시 발생하는 내부 동작

디플로이먼트를 생성하면 하위 객체들이 연쇄적으로 생성됨

1. **Deployment 생성:** 사용자가 정의한 배포 객체 생성
2. **ReplicaSet 자동 생성:** 디플로이먼트가 자신의 이름을 딴 레플리카셋을 자동으로 생성함
3. **Pods 생성:** 레플리카셋이 정의된 복제본 수만큼 포드들을 생성함
