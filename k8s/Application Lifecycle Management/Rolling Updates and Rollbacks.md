## Deployment rollout과 revision

- Deployment를 처음 생성하면 rollout 발생
- rollout이 새로운 ReplicaSet 생성
- deployment revision기록
- 그런데 예를 들어 애플리케이션 업그레이드를 위해 새로운 deployment를 추가로 생성한다면?
    - 업그레이드로 `revision 2`가 생성, 즉 새로운 revision 생성
- revision으로 Deployment 변경 이력 추적 가능
- 필요 시 이전 revision으로 rollback 가능

```bash
kubectl rollout status deployment/<deployment-name>
```

- rollout revision/history 확인 명령

```bash
kubectl rollout history deployment/<deployment-name>
```

- 참고
    - `얼마나 오래 남아 있느냐`는 `spec.revisionHistoryLimit` 값에 따라 달라질 수 있음
    - 기본값은 보통 10이라서 이전 ReplicaSet이 여러 개까지 유지될 수 있음

## Deployment 전략 2가지

- 예시로 웹 애플리케이션 replicas 5개 운영

### Recreate strategy

- 기존 5개 인스턴스를 모두 종료
- 새 버전 5개 인스턴스를 새로 생성
- 구버전이 내려간 뒤 신버전이 올라오기 전까지 **`서비스 중단 발생`**

### RollingUpdate strategy

- 기존 버전을 한 번에 다 내리지 않음
- 구버전을 하나씩 내리면서 신버전을 하나씩 올림
- **`업그레이드 중에도 애플리케이션이 내려가지 않음`**
- deployment 생성 시 strategy를 지정하지 않으면 rolling update로 동작
- rolling update가 기본 전략

## Deployment 업데이트 방법

### 방법 1: 정의 파일 수정 후 apply

- deployment 정의 파일을 수정
- 변경 적용에 `kubectl apply` 사용
- 새로운 rollout 수행
- 새로운 revision 생성

```bash
kubectl apply -f deployment.yaml
```

### 방법 2: set image로 이미지 변경

- `kubectl set image`로 이미지 변경 가능

```bash
kubectl set image deployment/<deployment-name> <container-name>=<image>:<tag>
```

- 이 방식은 **`정의 파일과 실제 설정이 달라질 수 있음`**
    - 기존에 myapp.yaml이라는 설정 파일이 있었다면 myapp.yaml과 내용이 달라질 수 있다는 것
- 이후 같은 정의 파일로 다시 수정할 때 주의 필요

## recreate와 rolling update 차이 확인

- Deployment 상세 정보 확인 명령

```bash
kubectl describe deployment <deployment-name>
```

- recreate 사용 시 이벤트 특징
    - old ReplicaSet이 먼저 0으로 scale down
    - new ReplicaSet이 5로 scale up
- rolling update 사용 시 이벤트 특징
    - old ReplicaSet을 1씩 scale down
    - 동시에 new ReplicaSet을 1씩 scale up

![image.png](attachment:4272c718-f08c-40d7-a0be-3efe36360f48:image.png)

## 업그레이드 동작 내부 구조

- Deployment 생성 시 내부에서 ReplicaSet 자동 생성
    - ReplicaSet이 replicas 수에 맞게 Pod 생성
    - 업그레이드 시 내부에서 새 ReplicaSet 생성
    - 새 ReplicaSet에서 새 버전 컨테이너 배포
    - 동시에 old ReplicaSet의 Pod를 rolling update 방식으로 축소
- ReplicaSet 목록 확인 명령

```bash
kubectl get replicaset
```

- 업그레이드 후 예시 상태
    - old ReplicaSet: pods 0
    - new ReplicaSet: pods 5
    - 업그레이드가 끝나면 보통 `기존 ReplicaSet = replicas 0` `새 ReplicaSet = replicas N` 형태

## Rollback

- 업그레이드 후 새 버전에 문제가 있는 경우 rollback 필요
- Deployment는 이전 revision으로 rollback 지원
- rollback 명령

```bash
kubectl rollout undo deployment/<deployment-name>
```

- rollback 수행 시 동작
    - new ReplicaSet의 Pod 제거
    - old ReplicaSet의 Pod 재생성
    - 애플리케이션이 이전 버전으로 복귀

## 명령어 요약

- Deployment 생성

```bash
kubectl create -f deployment.yaml
```

- Deployment 목록

```bash
kubectl get deployments
```

- Deployment 업데이트

```bash
kubectl apply -f deployment.yaml
kubectl set image deployment/<deployment-name> <container-name>=<image>:<tag>
```

- Rollout 상태 확인

```bash
kubectl rollout status deployment/<deployment-name>
```

- Rollback

```bash
kubectl rollout undo deployment/<deployment-name>
```