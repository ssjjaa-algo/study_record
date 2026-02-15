## 노드가 내려가면 발생하는 일

- 여러 노드와 애플리케이션을 서빙하는 Pod들이 있는 클러스터 가정
- 노드 하나가 내려가면 해당 노드의 Pod에 접근 불가
- Pod 배포 방식에 따라 사용자 영향이 달라짐
- blue pod를 내린다고 했을 때, 다른 노드에 blue Pod가 있다면 서비스에 지장 x
    - 다른 노드의 blue Pod가 계속 서빙
- 하나 뿐인 pod(green이라 가정)가 포함된 노드가 다운 → 서비스에 지장

## Kubernetes의 기본 처리

- 노드가 바로 온라인으로 복구되면 kubelet 프로세스가 시작되고 Pod가 다시 온라인이 됨
    - Delpoyment, ReplicaSet으로 관리될 것이기 때문에 바로 생성할 것이라 가정
    - 노드 다운이 **`5분을 넘으면`** (5분은 pod-eviction-timeout으로 대기)해당 노드의 Pod를 종료 처리
        - 다른 노드에 배치될 것임
        - 만약 ReplicaSet의 하위가 아니라면 사라진다
- Pod eviction timeout 이후 노드가 복구되면 노드는 Pod가 하나도 없는 상태로 올라옴

## 상황 가정

- 어떠한 이유로 노드가 잠깐 다운되어도 괜찮고 (다른 워크로드에서의 실행을 보장할 수 있음)
- 5분 안(pod-eviction-timeout)에 노드가 다시 살아날 보장이 있다면 잠깐의 지연을 감내할 수도 있다
- 하지만 pod-eviction-timeout 안에 노드가 다시 복구되는 것을 **`항상 확신하지는 못한다`**
- 안전한 방법을 사용해보자

## 더 안전한 방법: drain

```bash
kubectl drain node-1
```

- drain은 노드의 워크로드를 의도적으로 비워 **`다른 노드로 가게`** 만드는 방식
    - 참고 : **`이동이 아님`**
        - 해당 노드의 Pod를 graceful하게 종료
        - 다른 노드에 Pod를 재생성
- drain 수행 시 노드는 **`cordon`** 처리
    - cordon은 노드를 **`unschedulable`**로 마킹
    - unschedulable이면 해당 노드에는 새 Pod 스케줄링 불가
- 제한을**`직접 해제하기 전까지`** 유지
    - Pod가 다른 노드에 안전하게 떠 있는 상태에서 원래 노드를 재부팅 가능
- 노드가 다시 올라와도 여전히 **`unschedulable 상태 유지`**
- 다시 스케줄링되게 하려면 **`uncordon`**필요

```bash
kubectl uncordon node-1
```

## uncordon 이후 주의점

- drain으로 다른 노드에 만들어진 Pod가 자동으로 원래 노드로 되돌아오지 않음
- 이후 Pod가 삭제되거나 새 Pod가 생성되면 그때는 이 노드에도 스케줄링될 수 있음

## cordon

```bash
kubectl cordon node-1
```

- drain과 uncordon 외에 cordon 명령 존재
- cordon은 노드를 unschedulable로만 마킹
- drain과 달리 기존 Pod를 종료하거나 재생성하지 않음
- 새 Pod가 해당 노드에 스케줄링되지 않도록만 함