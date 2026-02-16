- static file / token 위주의 방식을 설명하는 문서
- **다만 권장되지 않은 방식이고 1.19 버전 이후 deprecated** 

## Kubernetes 클러스터 관리 접근 사용자 2종류

- humans
    - administrators
    - developers
    - end users (애플리케이션 사용자 = 실제 애플리케이션 접근자)
        - who access the applications deployed on the cluster
        - end user는 관리자 측면보다는 그냥 사용자 형태로 설명
- robots
    - 프로세스/서비스/애플리케이션 등 클러스터 접근이 필요한 주체

## Kubernetes의 사용자 계정 관리 방식

- Kubernetes는 user accounts를 관리하지 않음
- 외부 소스에 의존
    - 사용자 정보 파일
    - certificates
    - LDAP 같은 third party identity service
- Kubernetes 클러스터에서 “사용자 생성” 또는 “사용자 목록 조회”를 직접 할 수 없음
- service accounts는 예외
- Kubernetes가 **`service accounts`**는 관리 가능

    ```bash
    kubectl create serviceaccount sa1
    ```


## 모든 사용자 요청의 진입점

- 사용자 접근은 kube-apiserver가 처리
- kubectl로 접근하든 curl로 API를 직접 호출하든 요청은 kube-apiserver를 통과
- kube-apiserver가 요청을 처리하기 전에 인증 수행

## kube-apiserver 인증 방식

- 설정 가능한 인증 메커니즘 종류
    - static token file에 usernames/tokens 저장
    - certificates 기반 인증
    - LDAP, Kerberos 같은 third party 인증 프로토콜 연동

## Static password/token 파일

- 사용자 정보 CSV 파일을 사용하는 방식
- 쉽게 말해 사용자 정보를 파일에 그냥 저장하는 방식임
    - **`보안 상 권장하지 않음`**
- 프로덕션에서 권장되지 않음
- 학습 목적 전용
- Kubernetes 1.19에서 deprecated 되었고 이후 버전에서는 사용 불가

### 1) 사용자 파일 생성

- `/tmp/users/user-details.csv` 파일 생성

```bash
# /tmp/users/user-details.csv
password123,user1,u0001
password123,user2,u0002
password123,user3,u0003
password123,user4,u0004
password123,user5,u0005
```

### 2) kube-apiserver static pod에 사용자 파일 마운트

- kubeadm이 관리하는 kube-apiserver manifest 수정
- 파일 경로: `/etc/kubernetes/manifests/kube-apiserver.yaml`
- hostPath로 `/tmp/users`를 pod 안의 `/tmp/users`에 mount

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: kube-apiserver
  namespace: kube-system
spec:
  containers:
  - command:
    - kube-apiserver
      <content-hidden>
    image: k8s.gcr.io/kube-apiserver-amd64:v1.11.3
    name: kube-apiserver
    volumeMounts:
    - mountPath: /tmp/users
      name: usr-details
      readOnly: true
  volumes:
  - hostPath:
      path: /tmp/users
      type: DirectoryOrCreate
    name: usr-details
```

## 3) kube-apiserver 옵션에 basic-auth-file 추가

- kube-apiserver command 옵션에 아래 라인 추가

```yaml
apiVersion: v1
kind: Pod
metadata:
  creationTimestamp: null
  name: kube-apiserver
  namespace: kube-system
spec:
  containers:
  - command:
    - kube-apiserver
    - --authorization-mode=Node,RBAC
      <content-hidden>
    - --basic-auth-file=/tmp/users/user-details.csv
```

## 4) RBAC Role/RoleBinding 생성

- user1이 default namespace의 pods를 get/watch/list 할 수 있도록 설정

```yaml
---
kind: Role
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  namespace: default
  name: pod-reader
rules:
- apiGroups: [""] # "" indicates the core API group
  resources: ["pods"]
  verbs: ["get", "watch", "list"]

---
kind: RoleBinding
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: read-pods
  namespace: default
subjects:
- kind: User
  name: user1 # Name is case sensitive
  apiGroup: rbac.authorization.k8s.io
roleRef:
  kind: Role #this must be Role or ClusterRole
  name: pod-reader # this must match the name of the Role or ClusterRole you wish to bind to
  apiGroup: rbac.authorization.k8s.io
```

- 적용

```bash
kubectl apply -f rbac.yaml
```

## 5) basic auth로 kube-apiserver 호출

```bash
curl -v -k https://localhost:6443/api/v1/pods -u "user1:password123"
```

## Static token file 적용 방식

- kube-apiserver에 `-token-auth-file` 옵션으로 token file 전달
- 요청 인증 시 token을 Authorization Bearer token으로 전달
- kube-apiserver 실행 옵션 예시

```yaml
# kube-apiserver 옵션 예시
- --token-auth-file=/etc/kubernetes/auth/tokens.csv
```

- token 파일 예시(CSV)

```
token1,user1,uid1,group1
token2,user2,uid2,group2
```

- API 요청에서 bearer token 전달 예시

```bash
curl -k https://<apiserver-host>:6443/api/v1/pods \
  -H "Authorization: Bearer token1"
```