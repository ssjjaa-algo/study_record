# Taints and Tolerations

태그: 3-60

- How many `nodes` exist on the system?
    
    ```yaml
    kubectl get nodes
    ```
    

- Create a taint on `node01` with key of `spray`, value of `mortein` and effect of `NoSchedule`
    
    ```yaml
    kubectl taint node node01 spray=mortein:NoSchedule
    ```
    

- Create another pod named `bee` with the `nginx` image, which has a toleration set to the taint `mortein`.
    - —dry-run=client 명령어로 bee에 대한 default 세팅 가져오기
        
        ```yaml
        kubectl run bee --image=nginx --dry-run=client -o yaml > pod_definition.yaml
        ```
        
    - 내용 추가
        
        ```yaml
        apiVersion: v1
        kind: Pod
        metadata:
          creationTimestamp: null
          labels:
            run: bee
          name: bee
        spec:
          containers:
          - image: nginx
            name: bee
            resources: {}
          dnsPolicy: ClusterFirst
          restartPolicy: Always
          tolerations:
          - key: spray
            value: mortein
            effect: NoSchedule
        status: {}
        ```
        

- Remove the taint on `controlplane`, which currently has the taint effect of `NoSchedule`.
    - -가 해당  taint를 지운다는 뜻.
    
    ```bash
    kubectl taint node controlplane node-role.kubernetes.io/contr
    ol-plane:NoSchedule-
    ```
