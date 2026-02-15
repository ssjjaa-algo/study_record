## Microservices와 함께 쓰는 이유

- 큰 monolithic 애플리케이션을 microservices라는 하위 컴포넌트로 분리
- 독립적이고 작고 재사용 가능한 코드 단위로 개발과 배포 가능
- 전체 애플리케이션을 통째로 바꾸는 대신 서비스별로 수정 가능
- 서비스별로 scale up/down 가능

## Multi-container pod의 핵심 성질

- 같은 **`lifecycle`** 공유
    - 함께 생성
    - 함께 삭제
- 같은 network space 공유
    - 서로를 localhost로 참조 가능
- 같은 storage volume 접근 가능
- Pod 간 통신을 위해 별도로 서비스나 볼륨 공유를 구성하지 않아도 됨

## Multi-container pod 생성

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: webapp-with-mainapp
spec:
  containers:
    - name: webapp
      image: webapp
    - name: main-app
      image: main-app
```

## Multi-container pod 디자인 패턴 3가지

### 1) Co-located containers

- multi-container의 가장 기본 형태
- Pod 안에 컨테이너 2개가 함께 실행
- 두 컨테이너가 Pod 생명주기 전체 동안 계속 실행
- 두 서비스가 서로 의존적인 경우에 주로 사용

### 2) Init containers

- Pod 시작 시 메인 애플리케이션 실행 전에 초기화 단계를 수행할 때 사용
- 예시로 DB가 준비될 때까지 기다린 뒤 메인 앱 시작
- init container는 작업 수행 후 종료
- init container 종료 후 메인 앱 시작

### 3) Sidecar container

- init container처럼 먼저 시작해서 작업 수행
- 종료하지 않고 Pod 생명주기 동안 계속 실행
- sidecar가 시작된 후 메인 앱 시작
- 예시로 log shipper가 필요할 때 사용
    - 메인 앱 시작 전에 먼저 떠서 startup 로그 수집
    - 메인 앱이 실행되는 동안 계속 동작
    - 메인 앱 종료 후 종료

## Co-located vs Sidecar 구분

- init container는 **`“시작 후 종료 → 메인 앱 실행”`**
- co-located containers 특징
    - 어떤 컨테이너가 먼저 시작할지 순서를 정의할 수 없음
    - containers 배열의 요소로만 존재
    - 둘이 **`함께 시작`**
    - 한쪽이 먼저 시작된다는 보장 없음
    - 시작 순서가 중요하지 않으면 선택 가능
- sidecar containers 특징
    - **`시작 순서를 지정 가능`**
    - 먼저 실행된 뒤 Pod 생명주기 동안 계속 실행 가능
    - 이후 슬라이드에서 구성 방법을 보는 구성

## Co-located 예시 YAML

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: colocated-pod
spec:
  containers:
    - name: webapp
      image: webapp
    - name: main-app
      image: main-app
```

## Init container 예시 YAML

- containers 배열에 넣지 않고 initContainers 별도 필드 사용
- init container 여러 개 정의 가능
- 첫 번째 init container 종료 후 두 번째 init container 실행
- 두 번째 init container 종료 후 main app 실행
- main app은 Pod 종료까지 계속 실행

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: multi-init-pod
spec:
  initContainers:
    - name: wait-for-db
      image: busybox
      command: ["sh", "-c", "until nslookup mysql; do echo waiting for db; sleep 2; done"]
    - name: api-checker
      image: busybox
      command: ["sh", "-c", "until wget -qO- http://api:8080/health; do echo waiting for api; sleep 2; done"]
  containers:
    - name: main-app
      image: main-app
```

## Sidecar 패턴 YAML

- initContainers 방식을 사용
- init container가 먼저 시작되도록 구성
- 준비가 되면 main app 시작
- init container가 restartPolicy: Always로 계속 실행되도록 구성
- main app이 멈춘 뒤 init container도 종료되도록 구성
    - **main app이 실행되는 중에는 계속 실행된다**
- log shipper가 main container의 startup/termination 로그를 모두 수집 가능

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: sidecar-pod
spec:
  initContainers:
    - name: log-shipper
      image: filebeat
      restartPolicy: Always
      command: ["sh", "-c", "filebeat -e"]
  containers:
    - name: main-app
      image: main-app
```

## 현실적인 예시: Elasticsearch + Kibana + Filebeat

- Elasticsearch와 Kibana 스택 예시
- Elasticsearch
    - 여러 엔드포인트/애플리케이션의 로그를 수집하는 컴포넌트
- Kibana
    - 로그를 시각화하는 컴포넌트
    - 사용자/관리자가 보는 대시보드 역할
- 앱과 함께 Filebeat sidecar 추가
- Filebeat는 Elasticsearch와 함께 사용하는 로그 수집기
- Filebeat가 main app보다 먼저 시작하여 startup 로그 수집
- main app이 끝난 뒤 Filebeat도 끝나며 termination 로그 수집
- 버그로 앱이 종료된 경우 termination 로그가 진단에 필요하므로 수집 대상