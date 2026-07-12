# Namespaces

https://kubernetes.io/docs/concepts/overview/working-with-objects/namespaces/

- 단일 클러스터 내에서 리소스 그룹을 격리하는 메커니즘을 제공
- 소스의 이름은 네임스페이스 내에서만 유일해야 하며, 네임스페이스 간에는 유일할 필요가 없음
- 네임스페이스가 있는 객체(예: 배포(Deployments), 서비스(Services) 등)에만 적용되며, 클러스터 전체 객체(예: 스토리지 클래스(StorageClass), 노드(Nodes), 퍼시스턴트 볼륨(PersistentVolumes) 등)에는 적용되지 않음
- 각 쿠버네티스 리소스는 오직 하나의 네임스페이스에만 속할 수 있다

## 네임스페이스 리소스 확인

```yaml
kubectl api-resources --namespaced=true
kubectl api-resources --namespaced=false
```

## Initial namespaces

- (문서 보고 그냥 설명 보면 될 듯)
- default
- kube-node-lease
- kube-public
- kube-system
