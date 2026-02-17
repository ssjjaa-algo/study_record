보안 설정은 다음 두 수준에서 구성할 수 있음

- Pod
    - 해당 설정은 Pod 안의 모든 컨테이너에 적용
- Container
- Pod와 Container 양쪽에 모두 설정한 경우 Container 수준의 설정이 Pod 수준 설정을 override

---

## Pod 수준에서 Security Context 설정

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: ubuntu-pod
spec:
  securityContext:
    runAsUser: 1000
  containers:
    - name: ubuntu
      image: ubuntu
      command: ["sleep", "3600"]
```

- `securityContext` 를 `spec` 아래에 추가
- `runAsUser` 옵션을 사용해 Pod 전체를 사용자 ID 1000으로 실행

---

## Container 수준에서 Security Context 설정

- securityContext 를 **`컨테이너 정의 안으로 이동`**

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: ubuntu-pod
spec:
  containers:
    - name: ubuntu
      image: ubuntu
      command: ["sleep", "3600"]
      securityContext:
        runAsUser: 1000
```

이 경우 해당 컨테이너에만 적용된다.

---

## Capabilities 추가하기

- Linux capability를 추가하려면 `capabilities` 옵션을 사용

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: ubuntu-pod
spec:
  containers:
    - name: ubuntu
      image: ubuntu
      command: ["sleep", "3600"]
      securityContext:
        capabilities:
          add: ["MAC_ADMIN"]
```