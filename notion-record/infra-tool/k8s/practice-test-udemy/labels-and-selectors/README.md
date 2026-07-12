# Labels and Selectors

태그: 3-57

- We have deployed a number of PODs. They are labelled with `tier`, `env` and `bu`. How many PODs exist in the `dev` environment (`env`)?
    
    ```bash
    kubectl get pod --selector env=dev
    ```
    

- How many objects are in the `prod` environment including PODs, ReplicaSets and any other objects?
    
    ```bash
    kubectl get all --selector env=prod
    ```
    

- Identify the POD which is part of the `prod` environment, the `finance` BU and of `frontend` tier?
    
    ```bash
    kubectl get all --selector env=prod,bu=finance,tier=frontend
    
    # kubectl get all: 현재 네임스페이스에서 모든 리소스(파드, 서비스, 디플로이먼트 등)를 조회
    # --selector: 특정 라벨 셀렉터를 기반으로 객체를 필터링
    ```
    
    - 라벨 내용을 출력해보면 다음과 같다.
    - 라벨이 key:value 쌍으로 이루어진 것을 확인
        
        ```bash
        kubectl get pod app-1-zzxdf -o yaml > pod_definition.yaml
        ```
        
        ```yaml
        apiVersion: v1
        kind: Pod
        metadata:
          creationTimestamp: "2024-09-16T08:51:27Z"
          labels:
            bu: finance
            env: prod
            tier: frontend
          name: app-1-zzxdf
          namespace: default
          resourceVersion: "908"
          uid: c287cca2-6e25-4b74-89d2-7fbe4a0cefba
        ```
