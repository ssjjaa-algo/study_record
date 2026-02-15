## Kubernetes Secrets

- 코드에 db 관련 정보 host name, username, password가 하드코딩 → 위험하다

## Secret 개념

- Secret은 password나 key 같은 민감 정보 저장에 사용
- Secret은 encoded 또는 hashed 형태로 저장되는 점이 차이
    - **Secret 생성은 base64 인코딩을 하는 것이라 `절대 암호화가 아님`**
    - 서드 파티 솔루션을 사용하던가 해야함
        - AWS Provider, Azure 등

## Imperative 방식: --from-literal

- `kubectl create secret generic` 사용
- secret 이름 지정
- `-from-literal`로 key=value 지정
- 예시로 app-secret 생성
- key-value 예시로 `DB_HOST=mysql` 사용

```bash
kubectl create secret generic app-secret --from-literal=DB_HOST=mysql
```

- key-value 추가는 `-from-literal`을 여러 번 지정

```bash
kubectl create secret generic app-secret \
  --from-literal=DB_HOST=mysql \
  --from-literal=DB_USER=root \
  --from-literal=DB_PASSWORD=passw0rd
```

## Imperative 방식: --from-file

- 파일로 secret data 입력 가능
- `-from-file`로 파일 경로 지정
- 파일 내용이 읽혀서 “파일 이름”을 key로 저장

```bash
kubectl create secret generic app-secret --from-file=db_password.txt
```

## Declarative 방식: YAML

- declarative 방식에서는 data 값을 plain text로 두지 않고 **`base64 encoded 값으로`** 둬야 하는 전제

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: app-secret
data:
  DB_HOST: bXlzcWw=
  DB_USER: cm9vdA==
  DB_PASSWORD: cGFzc3cwcmQ=
```

## base64 인코딩 방법

- Linux에서 평문을 base64로 변환
- `echo -n`으로 개행 없이 출력 후 base64로 파이프

```bash
echo -n mysql | base64
echo -n root | base64
echo -n passw0rd | base64
```

## Secret 조회

- Secret 목록 조회

```bash
kubectl get secrets
```

- Kubernetes 내부 목적의 Secret도 함께 보일 수 있음
- Secret 상세 조회
- attribute는 보이지만 value는 숨김

```bash
kubectl describe secret app-secret
```

- value까지 포함해서 YAML로 조회

```bash
kubectl get secret app-secret -o yaml
```

## base64 디코딩 방법

- base64 decode 옵션 사용

```bash
echo -n bXlzcWw= | base64 --decode
```

## Pod에 Secret 주입: envFrom

- Secret 생성 후 Pod에 주입
- 컨테이너에 `envFrom` 추가
- `envFrom`은 리스트
- 리스트 항목으로 `secretRef`지정
- secret 이름 지정

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: app-pod
spec:
  containers:
    - name: app
      image: my-python-app:1.0
      envFrom:
        - secretRef:
            name: app-secret
```

- 위 방식으로 secret 데이터가 컨테이너의 환경 변수로 제공

## Secret 주입 방식

- 단일 환경 변수 / volume

### 단일 환경 변수로 주입

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: app-pod
spec:
  containers:
    - name: app
      image: my-python-app:1.0
      env:
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: app-secret
              key: DB_PASSWORD
```

### volume에 파일로 주입

- Secret을 volume으로 마운트하면 key마다 파일이 생성
- 파일 내용이 value가 됨
- 예시로 secret에 3개 attribute가 있으면 3개 파일 생성
- dbpassword 파일을 보면 password가 들어 있음

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: app-pod
spec:
  containers:
    - name: app
      image: my-python-app:1.0
      volumeMounts:
        - name: secret-vol
          mountPath: /etc/secret
  volumes:
    - name: secret-vol
      secret:
        secretName: app-secret
```