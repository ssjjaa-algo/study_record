# Taints and Tolerations

- Node affinity는 파드가 특정 노드 집합에 끌리게 만드는 속성(선호 또는 강제 조건으로 작용).
- Taints는 그 반대로, 노드가 특정 파드를 거부하도록 만드는 것
- Tolerations는 파드에 적용
    - 스케줄러가 테인트에 맞는 파드를 스케줄링할 수 있게 허용
    - 스케줄링을 허용할 뿐, 스케줄링을 보장하지는 않음
- 테인트와 톨러레이션은 파드가 부적절한 노드에 스케줄링되지 않도록 함께 작동
- 하나 이상의 테인트가 노드에 적용되면, 해당 테인트를 허용하지 않는 파드는 그 노드에 스케줄링되지 않도록 표시된다

## Concept

```bash
kubectl taint nodes node1 key1=value1:NoSchedule

# node1에 taint를 추가
# taint는 key1:value1을 가지며 NoSchedule이라는 taint를 가진다.
# 해당 taint에 맞는 toleration이 없는 한 스케줄링 불가
# 지우기는 NoSchedule 뒤에 -를 붙여줌
```

## taint를 우회

### 1. **일반적인 상황: 스케줄러가 테인트를 고려함**

- 보통 Kubernetes 스케줄러는 파드가 노드에 스케줄링될 때 **테인트**와 **톨러레이션**을 고려
- 예를 들어, 어떤 노드 `node1`에 `NoSchedule` 테인트가 설정되어 있다면, 해당 테인트를 무시할 수 있는 **톨러레이션**이 없는 한 그 노드에는 파드가 스케줄링되지 않음

### 예시

- `node1`에 다음과 같은 테인트 설정
    
    ```bash
    kubectl taint nodes node1 key=value:NoSchedule
    ```
    
- 파드에 톨러레이션이 없다면, 기본적으로 이 파드는 `node1`에 스케줄링되지 않음

### **스케줄러 우회: .spec.nodeName을**

- 파드의 `.spec.nodeName`을 수동으로 지정스
- 노드에 `NoSchedule` 테인트가 있어도 **무시**하고 파드를 강제로 그 노드에 할당

### 예시:

- 파드의 정의에서 `node1`을 수동으로 지정합니다:
    
    ```yaml
    yaml
    코드 복사
    apiVersion: v1
    kind: Pod
    metadata:
      name: example-pod
    spec:
      containers:
      - name: nginx
        image: nginx
      nodeName: node1  # 수동으로 노드를 지정
    ```
    
- 이 설정으로 인해 `NoSchedule` 테인트가 있어도 파드는 **무시**하고 `node1`에 배치

## **NoExecute**

- 노드에 `NoExecute` 테인트가 추가로 설정
- kubelet이 파드를 축출할 수 있다.
- 파드가 그 노드에서 실행된 후에도 적절한 **톨러레이션**이 없으면 **kubelet**이 파드를 노드에서 **강제 종료**할 수 있다

### 예시:

- `node1`에 다음과 같은 `NoExecute` 테인트도 설정되어 있다고 가정
    
    ```bash
    kubectl taint nodes node1 key=value:NoExecute
    ```
    
- 만약 파드에 이 `NoExecute` 테인트에 대한 톨러레이션이 없으면, 파드는 `node1`에 배치된 후 곧바로 kubelet에 의해 **강제종료**
- 이를 방지하려면 파드에 톨러레이션 추가
    
    ```yaml
    yaml
    코드 복사
    apiVersion: v1
    kind: Pod
    metadata:
      name: example-pod
    spec:
      containers:
      - name: nginx
        image: nginx
      tolerations:
      - key: "key"
        operator: "Equal"
        value: "value"
        effect: "NoExecute"
      nodeName: node1
    
    ```
    

### **요약**

1. **스케줄러가 자동으로 선택하는 경우**, 테인트와 톨러레이션을 고려해서 파드를 배치
2. **수동으로 `.spec.nodeName`을 지정**하면 스케줄러를 우회해 강제로 그 노드에 배치하지만, 노드에 `NoExecute` 테인트가 있으면 적절한 톨러레이션이 없을 경우 파드는 kubelet에 의해 **강제 축출**

### PreferNoSchedule

- 얘 언제 씀?
