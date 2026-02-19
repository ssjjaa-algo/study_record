> (Custom Resource Definition에서 사용한 예시)
etcd에 저장된 FlightTicket 오브젝트의 상태를 감시하고
>
>
> 실제로 항공권 예약 API를 호출해 예약/수정/취소를 수행하는 것
>
- 이를 위해 **Custom Controller** 가 필요하다.

---

## Controller

- 루프(loop) 형태로 계속 실행되는 프로세스
- Kubernetes 클러스터를 지속적으로 모니터링
- 특정 오브젝트의 변경 이벤트를 감지
- 변경에 따라 필요한 작업을 수행
- 예시의 경우
    - FlightTicket 오브젝트의 생성/수정/삭제 이벤트를 감시
    - 변경이 발생하면 항공권 예약 API 호출
- 이론적으로는 어떤 언어로든 가능함
    - Python은 자체적으로 큐잉과 캐싱을 구현해야 한다고 하고
    - Go로 개발하는 것을 추천함 (친화적인 듯)

---

## Custom Controller 개발 흐름

- GitHub의 sample-controller 저장소를 클론
- controller.go 파일을 수정해 원하는 로직을 추가
- 코드를 빌드하고 실행

---

### 1️⃣ Go 설치

Go가 설치되어 있어야 한다.

설치되어 있지 않다면 먼저 설치한다.

---

### 2️⃣ sample-controller 저장소 클론

```bash
git clone https://github.com/kubernetes/sample-controller.git
cd sample-controller
```

---

### 3️⃣ controller.go 수정

`controller.go` 파일에 원하는 로직을 추가한다.

예를 들어:

- FlightTicket 오브젝트 생성 감지
- bookflight API 호출
- 삭제 감지 시 예약 취소 API 호출

```go
package flightticket
var controllerKind = apps.SchemeGroupVersion.WithKind("FlightTicket")

func (dc *FlightTicketController) Run(workers int, stopCh <-chan struct{})

func (dc *FlightTicketController) callBokkFlightAPI (obj interface())
```

---

### 4️⃣ 빌드 및 실행

```bash
go build -o sample-controller .
```

- 실행할 때는 kubeconfig 파일을 지정해 Kubernetes API에 인증

```bash
./sample-controller --kubeconfig=$HOME/.kube/config
```

- 로컬에서 컨트롤러 프로세스가 시작
- FlightTicket 오브젝트 생성 감시
- 필요 시 외부 API 호출

---

## 배포 방법

- 컨트롤러를 매번 로컬에서 빌드하고 실행하는 것은 비효율적
- 따라서 보통
    1. 컨트롤러를 Docker 이미지로 패키징
    2. Kubernetes 클러스터 내부에서 Pod 또는 Deployment로 실행
- 아래는 작성한 예시

```
FROM golang:1.20-alpine
WORKDIR /app
COPY . .
RUN go build -o controller
CMD ["./controller"]
```

```bash
docker build -t my-flight-controller .
```

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: flight-controller
spec:
  replicas: 1
  selector:
    matchLabels:
      app: flight-controller
  template:
    metadata:
      labels:
        app: flight-controller
    spec:
      containers:
        - name: controller
          image: my-flight-controller
```

---

## 요약

- CRD는 새로운 리소스 타입을 정의
- Custom Controller는 해당 리소스를 감시하고 실제 동작을 수행
- 컨트롤러는 지속적으로 루프를 돌며 오브젝트 변경을 감시한
- Go client는 Shared Informer 등을 제공해 효율적인 구현을 도움
- 실제 배포 시에는 Docker 이미지로 패키징해 Pod/Deployment로 실행