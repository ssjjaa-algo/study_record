# Validating and Mutating Admission Controllers

## Validating admission controller

- NamespaceExists 또는 NamespaceLifecycle admission controller
    - namespace가 존재하는지 확인하고 존재하지 않으면 요청을 거절

## Mutating admission controller

- DefaultStorageClass plugin
    - 기본으로 활성화된 플러그인
- PVC 생성 요청이 왔을 때 요청이 authentication → authorization을 거친 뒤 admission controller
    - DefaultStorageClass admission controller가 PVC 생성 요청을 감시
    - PVC 스펙에 storage class가 지정되어 있는지 확인
    - storage class가 없으면 요청을 수정하여 default storage class를 추가
    - default storage class는 클러스터에서 기본으로 지정된 storage class 값 사용
- PVC 생성 후 확인하면 storage class가 자동으로 붙어 있는 상태 확인 가능
- 오브젝트가 `생성되기 전에` 요청/오브젝트를 변경하는 동작
- storageClassName 없이 PVC를 만드는 예시

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: data-pvc
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 1Gi
```

## Admission controller 타입 정리

- mutating admission controller
    - 요청을 변경 가능
- validating admission controller
    - 요청을 검증하고 허용 또는 거절 가능
- 두 기능을 모두 수행하는 admission controller도 가능

## 호출 순서

- 일반적으로 mutating admission controller가 먼저 호출
    - mutating이 만든 변경 사항을 validating이 반영해서 검사하기 위한 순서
- namespace auto provisioning admission controller 예시
    - namespace가 없으면 namespace를 자동 생성
    - mutating admission controller에 해당
- namespace exists admission controller 예시
    - namespace가 없으면 요청을 거절
    - validating admission controller에 해당
- namespace auto provisioning이 먼저 실행되고 namespace exists가 나중에 실행
    - 반대로 실행된다고 생각하면 namespace exists가 먼저 거절하여 auto provisioning이 namespace를 만들 기회가 없어짐
- admission controller 중 하나라도 요청을 거절하면 전체 요청이 거절되고 사용자에게 에러 반환

## Built-in admission controllers와 한계

- 지금까지 예시는 Kubernetes 소스코드에 포함되어 컴파일되어 함께 제공되는 built-in admission controllers
- 자체 로직으로 mutation/validation을 하고 싶은 경우에는 External admission controllers 이용

## External admission controllers: Webhook

- Kubernetes에서 external admission controllers 지원
- 이를 위한 특수 admission controller 2개 존재
    - MutatingAdmissionWebhook
    - ValidatingAdmissionWebhook

## Webhook 동작 방식

- webhook이 가리키는 서버를 클러스터 내부 또는 외부에 둘 수 있는 구성
- 서버에는 admission webhook service가 실행되며 자체 코드/로직 포함
- 요청이 built-in admission controllers를 모두 지난 뒤 webhook으로 전달
- API server가 webhook 서버에 AdmissionReview 객체를 JSON으로 전송
- AdmissionReview에 포함되는 정보
    - 요청을 보낸 사용자 정보
    - 수행하려는 operation 종류
    - 어떤 오브젝트에 대한 요청인지
    - 오브젝트 자체의 상세 내용
- webhook 서버가 AdmissionReview 형태로 응답 반환
    - 응답이 true/false에 따라 허용/거절

## Webhook server 구현 조건

1. admission webhook server 배포
2. Kubernetes에 webhook configuration object 생성
- 어떤 플랫폼으로도 구현 가능
- Kubernetes 문서에 Go로 작성된 예제 코드 존재
- 필요하면 다른 언어로도 구현 가능
- 요구 조건은 1가지
    - mutate/validate API를 받고 webhook이 기대하는 JSON 응답을 반환하는 서버 구현

## JSON Patch 구성 요소

- patch object는 patch operation들의 리스트
- operation 타입 예시
    - add
    - remove
    - replace
    - move
    - copy
    - test
- 변경 대상 path 지정
    - 예시 path는 `/metadata/labels/users`
- add operation이면 value 지정
    - value로 username 사용
- patch는 base64 인코딩되어 응답에 포함되는 형태

## Webhook server 호스팅 방식

- 서버를 외부에서 운영하는 방식 가능
- 서버를 컨테이너로 만들어 클러스터 내부에 Deployment로 배포하는 방식 가능
- 클러스터 내부 Deployment로 배포하면 접근을 위한 Service 필요
- Service 예시

```yaml
apiVersion: v1
kind: Service
metadata:
  name: webhook-service
  namespace: webhook-ns
spec:
  selector:
    app: webhook-server
  ports:
    - port: 443
      targetPort: 8443
```

- Deployment 예시

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: webhook-server
  namespace: webhook-ns
spec:
  replicas: 1
  selector:
    matchLabels:
      app: webhook-server
  template:
    metadata:
      labels:
        app: webhook-server
    spec:
      containers:
        - name: server
          image: example/webhook-server:1.0.0
          ports:
            - containerPort: 8443
```

## Webhook configuration 오브젝트 구성

- validating webhook configuration 오브젝트 생성 단계
- kind는 `ValidatingWebhookConfiguration / MutatingWebhookConfiguration`
- webhooks 섹션에 여러 webhook 정의 가능
- 각 webhook이 가지는 구성 요소
    - name
    - clientConfig
    - rules

## clientConfig: 외부 URL로 연결하는 예시

- webhook 서버가 클러스터 외부에 있고 URL로 접근하는 구성

```yaml
apiVersion: admissionregistration.k8s.io/v1
kind: ValidatingWebhookConfiguration
metadata:
  name: pod-validation-webhook
webhooks:
  - name: example.com
    clientConfig:
      url: https://webhook.example.com/validate
      caBundle: <BASE64_CA_BUNDLE>
    rules:
      - apiGroups: [""]
        apiVersions: ["v1"]
        operations: ["CREATE"]
        resources: ["pods"]
```

## clientConfig: 클러스터 내부 Service로 연결하는 예시

- webhook 서버를 클러스터 내부에 Deployment+Service로 두는 구성

```yaml
apiVersion: admissionregistration.k8s.io/v1
kind: ValidatingWebhookConfiguration
metadata:
  name: pod-validation-webhook
webhooks:
  - name: example.com
    clientConfig:
      service:
        namespace: webhook-ns
        name: webhook-service
        path: /validate
        port: 443
      caBundle: <BASE64_CA_BUNDLE>
    rules:
      - apiGroups: [""]
        apiVersions: ["v1"]
        operations: ["CREATE"]
        resources: ["pods"]
```

## TLS 요구 사항

- API server와 webhook server 통신은 TLS 사용
- webhook server에 인증서 쌍 구성 필요
- CA bundle 생성 후 `clientConfig.caBundle`에 설정 필요

## rules로 호출 조건 제한

- 어떤 요청에서 webhook을 호출할지 rules로 제한 가능
- apiGroups, apiVersions, operations, resources로 조건 지정 가능
- 예시에서는 Pod 생성(CREATE pods) 요청에서만 webhook 호출

```yaml
rules:
  - apiGroups: [""]
    apiVersions: ["v1"]
    operations: ["CREATE"]
    resources: ["pods"]
```

## 결과 동작

- webhook configuration 생성 이후 Pod 생성 요청마다 webhook service 호출
- webhook 응답의 allowed 값에 따라 요청 허용 또는 거절

## 마무리

- labs에서 webhook 실습 진행
- 다음 강의로 이동
