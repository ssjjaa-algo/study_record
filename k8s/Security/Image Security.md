## Docker 이미지 네이밍 규칙

```
library/nginx
```

- `library` → 사용자 또는 계정 이름
- `nginx` → 저장소(repository) 이름
- [`docker.io`](http://docker.io) → 레지스트리
- 사용자 이름을 명시하지 않으면 기본값으로 `library`
    - `library` 는 Docker 공식 이미지가 저장된 기본 계정

---

## 사용자 정의 이미지의 경우

자신의 계정이나 회사 계정으로 이미지를 만들면 다음과 같은 형식이 된다:

```
mycompany/myapp
```

여기서:

- `mycompany` → 사용자 또는 조직 이름
- `myapp` → 이미지 저장소 이름

---

## 레지스트리 예시

- Google Container Registry: `gcr.io`
- AWS ECR
- Azure Container Registry
- 이들 중 많은 이미지는 공개(public) 상태이며 누구나 다운로드할 수 있다.
- 내부 애플리케이션의 경우
    - 사내 애플리케이션처럼 외부에 공개하면 안 되는 경우
- 내부 Private Registry를 운영하는 것이 적절
    - AWS, Azure, GCP 같은 클라우드 제공자는 기본적으로 Private Registry를 제공한다.

---

## Private Repository 접근

- Docker 로그인 후 private repo 이용

```bash
docker login myregistry.example.com
```

```bash
docker run private-registry.io/apps/internal-app
```

---

## Kubernetes에서 Private Registry 사용

- Pod 정의에서 이미지를 private registry 경로로 변경

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: private-app
spec:
  containers:
    - name: app
      image: private-registry.io/apps/internal-app
```

- 그런데 인증은 어떻게 하는가?
- Kubernetes는 어떻게 registry 자격 증명을 아는가?
- 이미지는 워커 노드에서 Docker runtime이 pull한다
- 그렇다면 Docker runtime에 자격 증명을 어떻게 전달하는가?

---

## 해결 방법: Secret 사용

- Docker registry 자격 증명을 포함하는 Secret 객체를 생성
    - Secret 타입은 `docker-registry`

```bash
kubectl create secret docker-registry regcred \
    --docker-server=private-registry.io \
    --docker-username=registry-user \
    --docker-password=registry-password \
    --docker-email=registry-user@org.com
```

---

## Pod에서 Secret 사용

Pod 정의 파일에 `imagePullSecrets` 항목을 추가한다.

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: private-app
spec:
  containers:
    - name: app
      image: myregistry.example.com/myapp:latest
  imagePullSecrets:
    - name: regcred 
```