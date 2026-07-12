# Manual Scheduling

태그: 3-54

- A pod definition file `nginx.yaml` is given. Create a pod using the file.
    
    ```bash
    kubectl create -f nginx.yaml
    ```
    

- Why is the POD in a pending state?
    
    ```bash
    kubectl get pods -n kube-system
    # controle plane 요소들을 찾고 kube-scheduler가 없는 것을 확인한다
    ```
