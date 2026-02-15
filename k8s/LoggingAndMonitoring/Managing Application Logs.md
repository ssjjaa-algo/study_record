## Docker 로깅

- 애플리케이션이 표준 출력(stdout)으로 이벤트 로그 스트리밍
- `f` 옵션으로 실시간 로그 스트리밍

```bash
docker run -d <image>
docker logs <container-id> # 로그를 보려면 docker logs <container-id> 사용
docker logs -f <container-id>
```

## Kubernetes 로깅

- 동일한 Docker 이미지를 Pod 정의 파일로 Pod 생성
- Pod 실행 후 `kubectl logs <pod-name>`으로 로그 조회
- `f` 옵션으로 실시간 로그 스트리밍

```bash
kubectl logs <pod-name>
kubectl logs -f <pod-name>
```

## Pod에 여러 컨테이너가 있는 경우

- Kubernetes Pod는 여러 Docker 컨테이너 포함 가능
- 이 경우 `kubectl logs <pod-name> -c <container-name>`으로 컨테이너 이름 지정 필요
- 예시로 event-simulator 컨테이너 로그 조회

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: event-simulator-pod
spec:
  containers:
  - name: event-simulator
    image: kodekloud/event-simulator
  - name: image-processor
    image: some-image-processor
```

```bash
kubectl logs -f event-simulator-pod event-simulator
```