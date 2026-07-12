# phase1

## 1. nginx Pod 생성

```bash
k run my-nginx --image=nginx:1.25 --dry-run=client -o yaml > nginx.yaml

k apply -f nginx.yaml
```

## 2. Pod 생성 이벤트와 배치 노드 확인

```bash
k get pod my-nginx
k describe pod my-nginx
```

## 3. 컨테이너 내부에서 nginx 응답 확인

```bash
k exec my-nginx -- curl -s localhost
```

## 4. Pod 로그 확인

```bash
k logs my-nginx
```

## 5. 레이블 포함된 Pod 선언적 생성

- Pod 이름: `my-app`
- Namespace: `default`
- 컨테이너 이름: `app-container`
- 이미지: `nginx:1.25`
- 컨테이너 포트: `80`
- 레이블: `app=web`
- 최종 상태: `Running`

```bash
k run my-app --image=nginx:1.25 --port=80 --labels=app=web --dry-run=client -o yaml > my-app.yaml

yaml에서 containers[0].name을 app-container로 변경

k apply -f my-app.yaml

```

## **6. 이미지 오류 Pod 진단**

- describe를 통해 파악

```java
 Warning  Failed     7s    kubelet            spec.containers{broken-pod}: Error: ErrImagePull
  Normal   BackOff    6s    kubelet            spec.containers{broken-pod}: Back-off pulling image "nginx:this-tag-does-not-exist-99999"
```

## 7. 멀티 컨테이너 pod

https://kubernetes.io/docs/concepts/workloads/pods/init-containers/

- 위 페이지에서 init containers yaml 가져온 후 수정해서 작성
- **멀티 컨테이너 Pod는 yaml로만 가능** **(몰랐던 점)**

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: multi-container
spec:
  containers:
  - name: web
    image: nginx:1.25
  - name: logger
    image: busybox:1.36
    command: ["sh", "-c", "while true; do echo hello; sleep 5; done"]
```

- k logs -h를 통해 특정 컨테이너의 로그를 보는 방법을 확인
    
    ```bash
    -c, --container='':
        Print the logs of this container
        
    k logs multi-container -c logger
    ```
