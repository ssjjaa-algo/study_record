## 멀티 컨테이너 pod

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

## **내부 접근용 진입점 만들기(Service)**

`default` Namespace에서 `web-deploy` 앞에 안정적인 내부 접근 지점을 구성하세요.

**조건:**

- 이름: `web-svc`
- Namespace: `default`
- 접근 포트: `80`
- `app=web` 레이블을 가진 Pod들로 트래픽이 전달되어야 함
- 생성 후 내부 Cluster IP가 할당되어 있어야 함

- 일단 무슨 명령어로 시작할지 모르겠으면 무조건 -h
    - expose : Take a replication controller, service, deployment or pod and expose it as a new Kubernetes service
- k expose -h에서 예시 확인
    - Create a service for an nginx deployment, which serves on port 80 and connects to the containers on port 8000
    - kubectl expose deployment nginx --port=80 --target-port=8000

```bash
k expose deployment web-deploy --name=web-svc --port=80 --target-port=80 
```

```bash
k expose deployment web-deploy --name=web-svc --port=80 --target-port=80 \
--dry-run=client -o yaml > web-svc.yaml

k apply -f web-svc.yaml
```