# Deployment

태그: 2-34

- Create a new Deployment with the below attributes using your own deployment definition file.
    
    ```bash
    kubectl create deployment httpd-frontend --image=httpd:2.4-alpine --replicas=3
    ```
    
    - 이렇게 만들고 정보 확인하면 될 듯? vi로 만들어도 되긴 하지만.
    - kubectl get deployment httpd-frontend -o yaml로 확인해보면..
        
        ```bash
        apiVersion: apps/v1           # API 버전. 이 리소스는 apps/v1 API를 사용.
        kind: Deployment              # 리소스의 종류는 Deployment.
        metadata:
          annotations:
            deployment.kubernetes.io/revision: "1"   # 현재 배포 버전. 롤아웃 시 이 값이 증가.
          creationTimestamp: "2024-09-11T14:12:39Z"  # Deployment 생성 시간.
          generation: 1              # Deployment가 몇 번 업데이트되었는지 나타냄.
          labels:
            app: httpd-frontend      # 이 리소스에 부여된 레이블. 파드와 일치시키기 위한 필드.
          name: httpd-frontend       # Deployment의 이름.
          namespace: default         # Deployment가 배포된 네임스페이스.
          resourceVersion: "1870"    # 이 리소스의 현재 버전을 나타냄.
          uid: 29ccabd4-5570-4aaa-af99-b6b999e90415  # Kubernetes에서 이 리소스를 고유하게 식별하는 UID.
        
        spec:
          progressDeadlineSeconds: 600   # 배포가 실패로 간주되기 전까지 기다리는 시간 (초 단위).
          replicas: 3                    # 이 Deployment가 유지할 파드 수. 3개의 파드를 항상 유지.
          revisionHistoryLimit: 10        # 이전 버전의 ReplicaSet을 최대 10개까지 저장.
          selector:
            matchLabels:
              app: httpd-frontend         # app: httpd-frontend 레이블을 가진 파드만 관리.
          strategy:
            rollingUpdate:
              maxSurge: 25%               # 롤링 업데이트 중 최대 추가 파드 수. 총 파드 수의 25%만큼 더 생성 가능.
              maxUnavailable: 25%         # 업데이트 중 사용 불가능할 수 있는 최대 파드 비율. 25%까지 허용.
            type: RollingUpdate           # 배포 전략으로 롤링 업데이트 사용. 새로운 파드를 단계적으로 배포하고 기존 파드를 점진적으로 삭제.
        
          template:
            metadata:
              creationTimestamp: null     # 파드 템플릿에서 필요하지 않은 필드로 null 처리.
              labels:
                app: httpd-frontend       # 파드에 적용되는 레이블. Deployment의 matchLabels와 일치해야 함.
            spec:
              containers:
              - image: httpd:2.4-alpine   # 컨테이너에서 사용하는 Docker 이미지.
                imagePullPolicy: IfNotPresent  # 이미지가 로컬에 없을 때만 풀(Pull) 받음.
                name: httpd               # 컨테이너의 이름.
                resources: {}             # 자원 제한/요청이 설정되지 않음.
                terminationMessagePath: /dev/termination-log  # 종료 메시지를 저장할 경로.
                terminationMessagePolicy: File  # 종료 메시지를 파일로 기록.
              dnsPolicy: ClusterFirst      # 파드가 DNS 요청을 처리하는 방법. 기본적으로 클러스터 DNS 사용.
              restartPolicy: Always        # 컨테이너가 실패하면 항상 다시 시작.
              schedulerName: default-scheduler  # 기본 스케줄러 사용.
              securityContext: {}          # 보안 설정이 비어 있음.
              terminationGracePeriodSeconds: 30  # 파드가 종료될 때 30초의 유예 시간을 부여.
        
        status:
          availableReplicas: 3            # 사용 가능한 파드 수. 현재 3개의 파드가 정상적으로 동작 중.
          conditions:
          - lastTransitionTime: "2024-09-11T14:12:50Z"  # 상태 전환이 발생한 마지막 시간.
            lastUpdateTime: "2024-09-11T14:12:50Z"      # 상태가 마지막으로 업데이트된 시간.
            message: Deployment has minimum availability.  # 최소 복제본 수가 사용 가능함을 나타냄.
            reason: MinimumReplicasAvailable            # 최소 복제본이 사용 가능할 때의 상태 이유.
            status: "True"                              # 이 조건이 현재 만족되고 있음을 나타냄.
            type: Available                             # 파드가 사용할 수 있는 상태.
          - lastTransitionTime: "2024-09-11T14:12:39Z"
            lastUpdateTime: "2024-09-11T14:12:50Z"
            message: ReplicaSet "httpd-frontend-5bf76dfdf9" has successfully progressed.  # 새롭게 배포된 ReplicaSet이 정상적으로 진행됨.
            reason: NewReplicaSetAvailable              # 새 ReplicaSet이 사용 가능할 때의 상태 이유.
            status: "True"                              # 이 조건이 현재 만족됨.
            type: Progressing                           # 배포 진행 중인 상태.
          observedGeneration: 1                         # 이 상태가 마지막으로 관찰된 세대 번호.
          readyReplicas: 3                              # 준비된 상태인 파드 수. 총 3개의 파드가 준비됨.
          replicas: 3                                   # 총 파드 수. 3개의 파드가 생성됨.
          updatedReplicas: 3                            # 새로운 버전으로 업데이트된 파드 수. 3개의 파드가 업데이트됨.
        
        ```
