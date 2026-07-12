# Solution - Secrets

```
kubectl get secrets
```

```
kubectl describe default-token-cr4sr
```

```
k create secret generic db-secret --from-literal=DB_HOst sql01 --from-literal=DB_user=root
 --from-literal DB_Password=password123
```
