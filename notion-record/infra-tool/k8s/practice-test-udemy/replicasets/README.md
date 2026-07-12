# ReplicaSets

태그: 2-30

- How many ReplicaSets exist on the system?
    
    ```bash
    kubectl get replicasets
    ```
    

- ReplicaSet 복원
    - 4개 짜리 pod를 담당하는 replicaSet에서 하나 지우면 바로 다시 감지하고 생성함.
    
    ```bash
    kubectl delete pod new-replica-set-xxvht
    ```
    

- check for apiVersion of replicaset by command
    
    ```bash
    kubectl api-resources | grep replicaset
    ```
    

- metaLabels의 tier와 template tier의 일치

```bash
apiVersion: apps/v1        # 이 리소스는 apps/v1 API 버전을 사용
kind: ReplicaSet           # 리소스 종류
metadata:
  name: replicaset-2       # ReplicaSet의 이름은 replicaset-2
spec:
  replicas: 2             # 파드 2개를 항상 유지
  selector:
    matchLabels:
      tier: nginx          # 'tier: nginx' 레이블을 가진 파드를 선택
  template:
    metadata:
      labels:
        tier: nginx        # 파드에 'tier: nginx' 레이블을 추가
    spec:
      containers:
      - name: nginx        # 파드에 nginx 컨테이너를 포함
        image: nginx       # nginx 이미지를 사용
                      
```

- scale out
    
    ```bash
    kubectl scale <replicaset> --replica=<num>
    ```
