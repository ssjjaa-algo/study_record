## Kubernetes 계정

- User Account
    - 사람이 사용하는 계정
        - 클러스터를 관리하기 위해 접근하는 관리자
        - 애플리케이션을 배포하기 위해 접근하는 개발자
- Service Account
    - 머신(애플리케이션)이 사용하는 계정
        - Prometheus
        - Jenkins

---

## Pod와 Service Account의 연결

- Pod가 생성되면 기본적으로 `default` Service Account가 자동으로 연결

```bash
kubectl describe pod <pod-name>
...
...
Service Account: default
...
```

- Service Account는 Pod 내부에 projected volume 형태로 마운트된다
- 이 디렉터리는 Kubernetes가 자동으로 생성
    - 마운트 위치 : /var/run/secrets/kubernetes.io/serviceaccount
- crt, namespace, token 등을 확인할 수 있음
- default Service Account는 권한이 매우 제한적
- 특정 권한이 필요하다면 사용자 정의 Service Account를 생성해야함

---
## Service Account 생성 방법

```bash
kubectl create serviceaccount dashboard-sa
kubectl get serviceaccount
kubectl describe serviceaccount dashboard-sa
```

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: dashboard-sa
  namespace: default
```

---

## Pod에 Service Account 연결

- Pod 정의에서 `serviceAccountName` 필드를 사용한다

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: my-kubernetes-dashboard
spec:
  serviceAccountName: dashboard-sa
  containers:
    - name: app
      image: nginx
```

---

## Token 동작 방식

- 짧은 수명의(short-lived) Token 생성
- Pod 내부 projected volume에 자동 마운트
- Kubelet이 자동으로 Token을 rotation
- Pod 삭제 시 Token 자동 만료
- 즉 Token은 Pod 생명주기와 연결되어 있다

---

## 자동 마운트 비활성화

- Token이 자동으로 마운트되는 것을 원하지 않는 경우:

### Service Account 수준에서 설정

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: dashboard-sa
automountServiceAccountToken: false
```

- 이 경우 해당 Service Account를 사용하는 모든 Pod에 Token이 자동 마운트되지 않는다

### Pod 수준에서 설정

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: dashboard-pod
spec:
  serviceAccountName: dashboard-sa
  automountServiceAccountToken: false
  containers:
    - name: app
      image: nginx
```

- 이 경우 Service Account 설정과 관계없이 해당 Pod에는 마운트되지 않는다

---

## 클러스터 외부에서 사용할 Token 생성

- Pod 내부가 아닌 외부 애플리케이션에서 사용할 Token을 생성할 수도 있음

```bash
kubectl create token dashboard-sa
```

- 기본적으로 Token은 1시간 동안 유효
- 유효 기간 연장

```bash
kubectl create token dashboard-sa --duration=24h
```

- 이 Token은 Secret에 저장되지 않음

Token을 디코딩하면 다음 정보를 확인할 수 있다:

- 만료 시간(exp)
- Service Account 이름
- 기타 클레임 정보

```bash
echo <TOKEN> | cut -d "." -f2 | base64 -d | jq
```

---

## Token을 이용한 API 호출 예시

```bash
curl https://<API-SERVER>:6443/api/v1/pods \
  -H "Authorization: Bearer <TOKEN>" \
  --insecure
```