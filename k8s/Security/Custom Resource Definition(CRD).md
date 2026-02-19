## 기존 리소스와 컨트롤러

- Deployment를 생성하면 Kubernetes는 Deployment를 생성하고 그 정보를 **etcd에 저장**
- 아래와 같은 것을 할 수 있음
    - Deployment 생성
    - Deployment 목록 조회 및 상태 확인
    - Deployment 삭제
- 이 모든 작업은 결국 etcd 데이터 저장소에 있는 **deployment**를 생성/조회/수정/삭제하는 것
- Deployment를 만들면, Deployment에 지정한 replica 수만큼 Pod가 생성된다 (예: 3개)
    - 그 일을 누가 하는가?
    - **`컨트롤러(controller)`** 의 역할이며, 이 경우에는 **deployment controller** 가 수행
    - deployment controller는 Kubernetes에 기본으로 포함되어 있으므로 따로 만들 필요 없음
- 컨트롤러는 백그라운드에서 실행되는 프로세스이며, 자신이 관리해야 하는 리소스의 상태를 지속적으로 감시
    - deployment를 예시로 생성/수정/삭제하면, 우리가 한 작업과 클러스터 실제 상태가 일치하도록 필요한 변경을 수행
        - controller가 ReplicaSet을 생성하고
        - ReplicaSet이 deployment 정의 파일에서 지정한 수만큼 Pod를 생성
- 각 리소스들에는 해당 오브젝트 상태를 감시하고, 상태를 유지하기 위해 클러스터에 필요한 변경을 수행하는 컨트롤러가 존재

---

## Custom Resource 아이디어 예시: FlightTicket

- “비행기 티켓 예약”을 위한 오브젝트

```yaml
apiVersion: flights.com/v1
kind: FlightTicket
metadata:
  name: my-flight-ticket
spec:
  from: Mumbai
  to: London
  number: 2
```

- 이 오브젝트를 생성하면 "FlightTicket 리소스가 생성되길” 원함
    - flight ticket 생성
    - flight ticket 목록 조회
    - flight ticket 삭제
- 하지만 이것은 결국 etcd 데이터 저장소에 flight ticket 오브젝트를 생성/삭제하는 것 뿐
    - 실제로 비행기 티켓이 예약되지는 않는다
- 예를 들어 `bookflight.com/api` 같은 외부 API가 있다고 하자
    - FlightTicket 오브젝트를 만들 때마다 이 API를 호출해서 실제 예약을 하려면 무엇이 필요한가?
      - **`컨트롤러가 필요하다.`**

---

## Custom Controller 필요

- 따라서 **FlightTicket controller** 를 만든다.
    - 컨트롤러는 FlightTicket 리소스의 생성/수정/삭제 이벤트를 감시
- 리소스 생성 시: book flight API 호출 → 티켓 예약 수행
- 리소스 삭제 시: 예약 삭제 API 호출
    - FlightTicket 오브젝트 = **Custom Resource**
    - 이를 처리하는 컨트롤러 = **Custom Controller**
- 즉 **`Custom Resource + Custom Controller`** 조합

---

## 현재 상태에서 FlightTicket 생성이 실패하는 이유

- Kubernetes 클러스터에서 FlightTicket 리소스를 만들려고 하면 실패함

> flights.com/v1 버전의 FlightTicket kind에 대한 매칭이 없다
>
>
> (There are no matches for the kind "FlightTicket" in version "flights.com/v1")
>
- Kubernetes API에 “FlightTicket이라는 리소스를 허용한다”는 정의가 없기 때문
    - Kubernetes는 아무 리소스나 마음대로 만들 수 없고, 먼저 Kubernetes에 그 리소스 타입을 등록해야 한다.
- 등록을 위해 필요한 것이 **`CRD(Custom Resource Definition)`**

---

## CRD(Custom Resource Definition) 작성

- CRD는 Kubernetes에게 “앞으로 FlightTicket kind의 오브젝트를 만들겠다”는 것을 알려주는 객체
- CRD 예시 구성:
    - apiVersion: `apiextensions.k8s.io/v1`
    - kind: `CustomResourceDefinition`
    - metadata.name: `flights.com` 도메인 아래의 리소스 이름
        - 예: `flighttickets.flights.com`

그리고 spec에 다음을 정의한다.

### 1) scope

- scope는 이 리소스가 **namespaced** 인지 **cluster-scoped** 인지 정의
- Kubernetes에는 namespaced 리소스와 non-namespaced(= cluster-scoped) 리소스 존재
    - namespaced 예: pods, replica sets, deployments
    - cluster-scoped 예: persistent volumes, nodes, namespaces(자체)
- 여기서는 일단 namespaced로 설정한다.

### 2) group

- API 그룹을 정의, apiVersion에 쓸 그룹 이름
    - group: `flights.com`

### 3) names

- 리소스 이름 정의
    - kind: `FlightTicket`
    - singular: `flightticket`
    - plural: `flighttickets`
    - shortNames: 예) `ft`
- plural은 Kubernetes API 리소스 경로와 `kubectl api-resources` 출력에 사용
- shortNames를 지정하면 `kubectl get ft`

### 4) versions

- 리소스는 alpha/beta를 거쳐 v1으로 가는 식의 라이프사이클을 가질 수 있음
- 여러 버전을 동시에 둘 수도 있는데, 그 경우 다음을 지정한다.
    - served: API 서버를 통해 제공되는지 여부
    - storage: etcd에 저장되는 스토리지 버전인지 여부
        - storage는 여러 버전 중 **하나만** true가 될 수 있다.

### 5) schema

- spec 아래에 어떤 필드가 올 수 있는지 정의
    - OpenAPI v3 schema를 사용

예시로 spec에 다음을 정의한다.

- from: string
- to: string
- number: integer

---

## CRD YAML 예시

```yaml
apiVersion: apiextensions.k8s.io/v1
kind: CustomResourceDefinition
metadata:
  name: flighttickets.flights.com
spec:
  scope: Namespaced
  group: flights.com
  names:
    kind: FlightTicket
    singular: flightticket
    plural: flighttickets
    shortNames:
      - ft
  versions:
    - name: v1
      served: true
      storage: true
      schema:
        openAPIV3Schema:
          type: object
          properties:
            spec:
              type: object
              properties:
                from:
                  type: string
                to:
                  type: string
                number:
                  type: integer
                  minimum: 1
                  maximum: 10
```

```bash
kubectl create -f flightticket-crd.yaml
```

- CRD가 생성되면 이제 FlightTicket 오브젝트를 만들 수 있고, get/delete도 가능해짐

```bash
kubectl create -f my-flight-ticket.yaml
kubectl get flighttickets
kubectl delete flightticket my-flight-ticket
```

---

## CRD로 해결되는 것과 남는 문제

- CRD를 사용하면 Kubernetes에서 원하는 타입의 오브젝트를 만들 수 있다.
    - 원하는 리소스 타입을 정의 가능
    - 스키마 정의 가능
    - validation 추가 가능
    - 하지만 이것은 **etcd에 저장될 뿐**
- 컨트롤러가 없으면 Custom Resource는 단지 데이터로만 존재함