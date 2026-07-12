# DaemonSets

- On how many nodes are the pods scheduled by the **DaemonSet** `kube-proxy`?
    - 해당 명령어를 통해 node 확인 가능
    
    ```yaml
    kubectl describe daemonset kube-proxy --namespace=kube-system
    ```
    

- Deploy a **DaemonSet** for `FluentD` Logging.
    
    Use the given specifications.
    
    - Name: elasticsearch
    - Namespace: kube-system
    - Image: registry.k8s.io/fluentd-elasticsearch:1.20
