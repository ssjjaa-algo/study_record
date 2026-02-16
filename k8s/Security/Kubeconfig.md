## curl 또는 kubectl에서 매번 옵션을 쓰는 방식

- 아래 예시 참고
- kubectl에서도 같은 정보를 옵션으로 지정 가능
    - server
    - client key
    - client certificate
    - certificate authority
- 매번 입력하기 번거로움
- 이 정보를 kubeconfig 파일로 옮기는 방식

```bash
curl https://my-kube-playground:6443/api/v1/pods \
    --key admin.key
    --cert admin.crt
    --cacert ca.crt

{
    "kind": "PodList",
    "apiVersion": "v1",
    "metadata": {
        "selfLink": "/api/v1/pods"
    },
    "items": []
}

kubectl get pods
    --server my-kube-playground:6443
    --client-key admin.key
    --client-certificate admin.crt
    --certificate-authority ca.crt
```

## kubeconfig 기본 위치

- kubectl은 기본적으로 사용자 홈 디렉터리 아래 `.kube/config` 파일을 찾음
- 여기에 kubeconfig를 두면 kubectl 명령에 경로를 따로 지정하지 않아도 됨
- 지금까지 kubectl 명령에서 옵션을 안 줬던 이유

## kubeconfig 구성 요소 3개

- clusters
    - 접근할 Kubernetes 클러스터 목록
    - dev/test/prod 환경
    - 조직별 클러스터
    - 클라우드 제공자별 클러스터
- users
    - 클러스터에 접근할 사용자 계정 목록
    - admin user
    - dev user
    - prod user
    - 사용자마다 권한이 다를 수 있음
- contexts
    - **cluster와 user를 묶는 항목**
    - 어떤 user로 어떤 cluster에 접근할지 지정
    - 예시 context
        - `admin@production`
            - production cluster에 admin user로 접근
        - `dev@google`
            - Google에 있는 cluster에 dev user로 접근

## kubeconfig YAML

- clusters/contexts/users는 배열 형태
- 한 파일에 여러 클러스터/유저/컨텍스트를 넣는 구조

```yaml
apiVersion: v1
kind: Config

clusters:
  - name: mykube-playground
    cluster:
      server: https://<apiserver-endpoint>:6443
      certificate-authority: /etc/kubernetes/pki/ca.crt

users:
  - name: mykube-admin
    user:
      client-certificate: /etc/kubernetes/pki/users/admin.crt
      client-key: /etc/kubernetes/pki/users/admin.key

contexts:
  - name: mykube-admin@mykube-playground
    context:
      cluster: mykube-playground
      user: mykube-admin

current-context: mykube-admin@mykube-playground # 시작 컨텍스트
```

- 파일을 그대로 두고 kubectl이 읽어서 사용

## current-context

- 컨텍스트가 여러 개면 어느 것을 기본으로 쓸지 지정 필요
- kubeconfig에 `current-context` 필드로 기본 컨텍스트 지정
- 예시로 `dev-user@google`을 current-context로 두면
    - kubectl은 기본으로 Google cluster에 dev user로 접근

## kubeconfig 확인/변경 명령

### 현재 kubeconfig 보기

```bash
kubectl config view
```

- clusters/contexts/users/current-context 출력

### 다른 kubeconfig 파일 사용

```bash
kubectl config view --kubeconfig=my-custom-config
```

- `-kubeconfig`로 파일 경로 지정 가능
- 홈 디렉터리 `.kube/config`에 두면 기본 파일로 사용

### current-context 변경

- 예시로 minikube admin에서 production user로 전환

```bash
kubectl config use-context prod-user@production
```

- 변경 사항이 kubeconfig 파일의 current-context에 반영 → **`실제 파일에 데이터가 수정됨`**

## context에 namespace 지정

- context에 namespace 필드 추가 가능
- 해당 context로 전환하면 자동으로 그 namespace를 기본으로 사용

```yaml
contexts:
  - name: dev@google
    context:
      cluster: google
      user: dev-user
      namespace: dev
```

## certificates 경로 vs data

- kubeconfig에서 certificate 파일 경로를 지정하는 방식 존재
    - `certificate-authority`
    - `client-certificate`
    - `client-key`

```yaml
apiVersion: v1
kind: Config

clusters:
  - name: production
    cluster:
      certificate-authority: /etc/kubernetes/pki/ca.crt
      server: https://172.17.0.51:6443

contexts:
  - name: admin@production
    context:
      cluster: production
      user: admin
      namespace: finance

current-context: admin@production

users:
  - name: admin
    user:
      client-certificate: /etc/kubernetes/pki/users/admin.crt
      client-key: /etc/kubernetes/pki/users/admin.key

```

- 경로는 full path 사용이 더 안전
- 파일 경로 대신 data 필드로 certificate 내용을 넣는 방식 존재
    - certificate 내용을 base64로 인코딩해서 넣는 방식
    - 예시로 `certificate-authority-data` 사용

```yaml
clusters:
  - name: mykube-playground
    cluster:
      server: https://<apiserver-endpoint>:6443
      certificate-authority-data: <BASE64_ENCODED_CA_CERT>
```

- encoded certificate가 있으면 base64 decode로 원문 복원 가능