# Object

## 쿠버네티스 오브젝트

- 쿠버네티스 시스템에서 영속성을 가지는 오브젝트.
- 클러스터의 상태를 나타내는 entity들.
    - 어떤 컨테이너화된 application이 실행중인지
    - 해당 애플리케이션에서 사용 가능한지
    - 재시작 정책, 업그레이드 .. 등.
- 객체를 생성 → 쿠버네티스 시스템이 객체가 존재하는지 **`지속적 작동`**
    - 쿠버네티스 시스템에 나의 작업 부하를 알리는 것.
- 객체를 만들기 위해 Kubernetes API 사용
    - CLI → **`kubectl`**

- 객체 사양 예시
    
    ```yaml
    apiVersion: apps/v1 # 객체 생성시 사용하는 kubernetes API version
    kind: Deployment # 객체 졸유
    metadata: # 객체 고유 식별 데이터들.
      name: nginx-deployment
    spec: # 원하는 객체 상태
      selector:
        matchLabels:
          app: nginx
      replicas: 2 # tells deployment to run 2 pods matching the template
      template:
        metadata:
          labels:
            app: nginx
        spec:
          containers:
          - name: nginx
            image: nginx:1.14.2
            ports:
            - containerPort: 80
    ```
    
    - kubectl apply -f를 이용해 적용가능
