## Kubernetes 리소스

- 크게 2가지
    - namespaced 리소스
        - namespace를 별도로 지정하지 않으면 default naemsapce
    - cluster scoped 리소스
        - 클러스터 범위이므로 네임스페이스 지정 x
        - ex) nodes, persistent volumes
- namespace 리소스와 non-namespace 리소스 확인

```bash
kubectl api-resources --namespaced=true
kubectl api-resources --namespaced=false
```

---

## Cluster roles / bindings

- cluster 수준이라는 것에만 차이가 있음
- 참고
    - namespace를 위한 클러스터 역할도 만들 수 있다
        - 이 경우 사용자는 모든 namespace의 리소스에 접근할 수 있는 것

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: cluster-administrator
rules:
- apiGroups: [""]
  resources: ["nodes"]
  verbs: ["get", "create", "delete", "list"]
```

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: cluster-admin-role-binding
subjects:
  - kind: User
    name: cluster-admin
    apiGroup: rbac.authorization.k8s.io
roleRef:
  kind: ClusterRole
  name: cluster-administrator
  apiGroup: rbac.authorization.k8s.io
```