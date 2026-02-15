## Pod 정의 파일에 env를 직접 넣는 방식의 문제

- Pod 정의 파일이 많아지면 각 파일 안에 들어 있는 environment data 관리가 어려워지는 문제
- environment data를 Pod 정의 파일에서 분리하여 중앙에서 관리하는 방식 필요

## ConfigMap 개념

- Pod 정의 파일에서 configuration 정보를 분리하여 ConfigMap으로 중앙 관리 가능
- ConfigMap은 key-value 쌍 형태로 configuration data를 전달하는 오브젝트
- Pod 생성 시 **`ConfigMap을 Pod에 주입하여`** key-value 쌍이 컨테이너 내부 애플리케이션에서 environment variables로 사용 가능
    - 즉 ConfigMap을 생성하고, Pod에 주입한다

## Imperative 방식으로 생성: --from-literal

- `kubectl create configmap` 명령 사용
- configmap 이름 지정
- `-from-literal`로 key=value를 명령에 직접 입력
    - key: APP_COLOR
    - value: blue

```bash
kubectl create configmap appconfig --from-literal=APP_COLOR=blue
```

- key-value를 더 추가하려면 `-from-literal`을 여러 번 지정

```bash
kubectl create configmap appconfig \
  --from-literal=APP_COLOR=blue \
  --from-literal=APP_MODE=prod
```

## Imperative 방식 2: --from-file

- 파일을 통해 configuration data 입력 가능
- `-from-file` 옵션으로 파일 경로 지정
- 파일 내용이 읽혀서 ConfigMap에 저장

```bash
kubectl create configmap appconfig --from-file=app.properties
```

## Declarative 방식: YAML

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: appconfig
data:
  APP_COLOR: blue
  APP_MODE: prod
```

```bash
kubectl create -f appconfig.yaml
```

## ConfigMap 여러 개 생성

- 목적별로 여러 ConfigMap 생성 가능
- 예시로 애플리케이션용, MySQL용, Redis용 ConfigMap 분리 가능
- 나중에 Pod에 연결할 때 **`name으로 참조`**하므로 이름을 명확하게 짓는 필요

## ConfigMap 조회

- ConfigMap 목록 조회

```bash
kubectl get configmaps
```

- ConfigMap 상세 조회(데이터 포함)

```bash
kubectl describe configmap appconfig
```

## Pod에 ConfigMap 주입

- 2단계로 Pod에 ConfigMap 연결
- Pod 정의 파일에서 컨테이너에 `envFrom` 추가
    - `envFrom`은 리스트
- 리스트 각 항목이 ConfigMap 참조
    - `configMapRef.name`에 ConfigMap 이름 지정

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: webapp-pod
spec:
  containers:
    - name: webapp
      image: my-webapp:1.0
      envFrom:
        - configMapRef:
            name: appconfig
```

- 위 구성으로 web application이 APP_COLOR=blue 환경 변수를 받아 blue background로 동작하는 예시

## ConfigMap 단일환경변수 / volume 주입

- envFrom으로 “전체 key-value를 환경변수로 주입” 외에도 다른 방식 존재
- 단일 환경 변수로 주입

```yaml
      env:
        - name: APP_COLOR
          valueFrom:
            configMapKeyRef:
              name: appconfig
              key: APP_COLOR
```

- volume

```yaml
	
      volumeMounts:
        - name: appconfig-vol
          mountPath: /etc/config
  volumes:
    - name: appconfig-vol
      configMap:
        name: appconfig
```