# Node Affinity

태그: 3-63

- Apply a label `color=blue` to node `node01`
    
    ```bash
    kubectl label node node01 color=blue
    ```
    

- Set Node Affinity to the deployment to place the pods on `node01` only.
    - spec.template.spec.affinity.nodeAffinity 부분에 추가해야 함( pod에 대한 설정 )
    
    ```bash
    ---
    apiVersion: apps/v1
    kind: Deployment
    metadata:
      name: blue
    spec:
      replicas: 3
      selector:
        matchLabels:
          run: nginx
      template:
        metadata:
          labels:
            run: nginx
        spec:
          containers:
          - image: nginx
            imagePullPolicy: Always
            name: nginx
          affinity:
            nodeAffinity:
              requiredDuringSchedulingIgnoredDuringExecution:
                nodeSelectorTerms:
                - matchExpressions:
                  - key: color
                    operator: In
                    values:
                    - blue
    ```
    

- Create a new deployment named `red` with the `nginx` image and `2` replicas, and ensure it gets placed on the `controlplane` node only.
    
    Use the label key - `node-role.kubernetes.io/control-plane` - which is already set on the `controlplane` node.
    
    - yaml 추출
    
    ```bash
    kubectl create deployment red --image=nginx --replicas=2 --dry-run=client -o yaml > test.yaml
    ```
    
    - 내용 추가
        - operator Exists, In .. 등등의 차이는 공식문서에서 참고
    
    ```yaml
          affinity:
            nodeAffinity:
              requiredDuringSchedulingIgnoredDuringExecution:
                nodeSelectorTerms:
                -  matchExpressions:
                   - key: node-role.kubernetes.io/control-plane
                     operator: Exists 
                    
    ```
