![image.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/9dc8d30d-5946-433d-b625-05408022dc4c/bd90a296-75aa-40fd-bff0-99be114ae732/image.png)

## 1. Role 생성

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: developer
rules:
- apiGroups: [""] 
  resources: ["pods"]
  verbs: ["list", "get", "create", "update", "delete"]
- apiGroups: [""] 
  resources: ["ConfigMap"]
  verbs: ["create"]
```

- 이후 k create -f
- 참고로 apiGroups: [””]의 의미는 Core group (Core group은 별도 이름이 없이 사용할 수 있음)
    - 직접 명시한 그룹으로 제한할 수 있는데 이는 공식문서를 참고하자 (Role examples 부분 확인)
        - https://kubernetes.io/docs/reference/access-authn-authz/rbac/

---

## 2. Role binding

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: devuser-developer-binding
subjects: # subjects는 복수 (권한을 여러 명 또는 여러 그룹이 받을 수 있음)
- kind: User
  name: dev-user
  apiGroup: rbac.authorization.k8s.io
roleRef:
  kind: Role
  name: developer # 위에서 정의한 Role의 이름
  apiGroup: rbac.authorization.k8s.io
```

- 이후 k create -f

---

## 범위

- Role과 RoleBinding은 네임스페이스 범위
- 위 예시에서는 `dev-user` 가 default 네임스페이스 안에서만 pods와 configmaps에 접근할 수 있음
- 네임스페이스 제한
    - Role과 RoleBinding 정의 파일의 metadata에 해당 네임스페이스를 지정하면 된다.

---

## 명령어

```bash
kubectl get roles
kubectl get rolebindings
kubectl describe role developer
kubectl describe rolebinding <이름>
```

---

## auth

- `kubectl auth can-i` 명령을 사용하여 특정 권한을 가지고 있는지를 확인할 수 있다.

```bash
kubectl auth can-i create deployments
kubectl auth can-i delete nodes
kubectl auth can-i create deployments --as dev-user # dev-user의 권한을 확인할 수 있음
kubectl auth can-i create pods --as dev-user -n test # namespace 지정
```

- 있으면 yes, 없으면 no

---

## Resource names

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: limited-pod-access
  namespace: default
rules:
  - apiGroups: [""]
    resources: ["pods"]
    resourceNames: ["blue", "orange"] # blue와 orange 파드에 대해서만 작업을 허용
    verbs: ["get", "delete"]
```

- resourceNames 주목