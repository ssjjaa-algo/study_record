## 업그레이드 대상 범위

- etcd와 DNS 같은 외부 컴포넌트 의존성은 잠시 제외
- core control plane components에 집중

## 컴포넌트 버전 일치 필요 여부

- control plane 컴포넌트가 모두 같은 버전일 필요 없음
- kube-apiserver가 control plane의 핵심 컴포넌트
- 다른 모든 컴포넌트가 kube-apiserver와 통신
    - 핵심 : 다른 컴포넌트는 **`kube-apiserver보다 높은 버전이면 안 됨`**
- controller manager와 scheduler는 1 minor 버전 낮아도 됨
- kubelet과 kube-proxy는 2 minor 버전 낮아도 됨
- 예시로 kube-apiserver가 X 버전이면
    - controller manager / scheduler는 X 또는 X-1 가능
    - kubelet / kube-proxy는 X 또는 X-1 또는 X-2 가능
    - **`X보다 높은 버전은 불가`**
- **`kubectl`**은 kube-apiserver보다 한 단계 높은 버전도 가능

## 버전 skew가 주는 효과

- 컴포넌트별로 **`순차 업그레이드`** 가능
- **`live upgrade`** 수행 가능
- 예시로 현재 1.10을 사용 중이고 1.11, 1.12가 릴리스된 상황 가정
- Kubernetes는 최신 3개의 minor 버전만 지원
    - 1.12가 최신이면 지원 버전은 1.12, 1.11, 1.10
    - 1.13이 릴리스되면 지원 버전은 1.13, 1.12, 1.11
- 1.10에서 1.13으로 한 번에 업그레이드하지 않음
    - 1 minor씩 순차 업그레이드 권장

## 클러스터 구성 방식에 따른 업그레이드 방식

- managed Kubernetes(GKE 등)
    - 몇 번 클릭으로 업그레이드 제공
- kubeadm 기반
    - kubeadm이 업그레이드 계획과 업그레이드 수행 지원
- scratch 설치
    - 각 컴포넌트를 수동 업그레이드
- 이 강의는 kubeadm 기준으로 설명

## 마스터 업그레이드와 워커 업그레이드 2단계

- 업그레이드는 크게 2단계
    - master nodes 업그레이드
    - worker nodes 업그레이드

## 마스터 업그레이드 중 클러스터 영향

- master 업그레이드 동안 control plane 컴포넌트가 잠시 내려감
    - kube-apiserver
    - scheduler
    - controller manager
- master가 내려가도 worker node의 워크로드는 계속 동작
- 사용자 트래픽은 worker node의 Pod가 계속 처리
- master가 내려간 동안에는 본래의 master가 제공하는 기능이 잠시 down
- 업그레이드 완료 후 정상 동작 복귀

## 워커 노드 업그레이드 전략 3가지

### 1) 워커 노드 전부 한 번에 업그레이드

- 모든 워커 노드를 동시에 업그레이드
- 업그레이드 동안 Pod가 내려감
- 사용자 접근 불가
- 업그레이드 완료 후 노드가 올라오고 Pod가 새로 스케줄링된 뒤 접근 재개
- 마치 Recreated 전략같이 작동하는 듯

### 2) 워커 노드를 1개씩 순차 업그레이드

- 마스터는 업그레이드된 상태에서 워커 노드 업그레이드 진행
- 첫 번째 노드를 업그레이드할 때 워크로드를 2, 3번 노드가 처리
- 첫 번째 노드 업그레이드 완료 후 두 번째 노드 업그레이드
    - 워크로드를 1, 3번 노드가 처리
- 세 번째 노드 업그레이드
    - 워크로드를 1, 2번 노드가 처리
- 모든 노드가 새 버전이 될 때까지 반복
- 이후 1.11 → 1.12 → 1.13에서도 동일 방식 반복

### 3) 새 버전 노드 추가 후 기존 노드 제거

- 새 버전 소프트웨어가 깔린 노드를 새로 추가
- 워크로드를 새 노드로 옮김
- 기존 노드를 제거
- 클라우드 환경에서 특히 편리

## kubeadm으로 업그레이드 수행

- kubeadm upgrade 명령으로 업그레이드 지원

### kubeadm upgrade plan

- `kubeadm upgrade plan` 실행
    - kubeadm 자체를 먼저 업그레이드해야 함
- 출력 정보
    - 현재 클러스터 버전
    - kubeadm 도구 버전
    - 최신 stable Kubernetes 버전
    - control plane 컴포넌트 버전
    - 업그레이드 가능한 목표 버전
    - control plane 업그레이드 후 각 노드의 kubelet을 수동 업그레이드해야 한다는 안내
    - kubeadm은 kubelet을 설치/업그레이드하지 않는 점
    - 클러스터 업그레이드에 사용할 명령 안내

### 1 minor씩 업그레이드

- 현재 1.11에서 1.13으로 가려는 상황
- 1 minor씩 진행 필요
- 먼저 kubeadm을 1.12로 업그레이드
- 이후 `kubeadm upgrade apply`로 control plane 업그레이드 수행
- 필요한 이미지 pull
- control plane 컴포넌트 업그레이드

## kubectl get nodes 출력의 의미

- 업그레이드 직후 `kubectl get nodes`에서 master가 여전히 이전 버전처럼 보일 수 있음
- `kubectl get nodes`는 API server 버전이 아니라 **`각 노드의 kubelet 버전을 보여주는 출력`**
    - 따라서 다음 단계로 kubelet을 업그레이드해야 함

## 마스터 노드 kubelet 업그레이드

- 다음 단계로 master 노드의 kubelet 업그레이드
- kubeadm 클러스터에서는 master에 kubelet이 존재
    - control plane 컴포넌트를 master에서 Pod로 실행하기 때문
- 나중에 scratch로 구성하는 경우 master에 kubelet을 설치하지 않는 케이스도 존재
- 그 경우 `kubectl get nodes`에 master가 보이지 않을 수 있음
- master에 kubelet이 있는 경우
    - apt-get으로 kubelet 패키지 업그레이드
    - kubelet 서비스 재시작

## 워커 노드 업그레이드 절차(노드별)

- 워커 노드를 1개씩 진행

### 1) drain

- 첫 번째 워커 노드에서 워크로드를 다른 노드로 보내기
- `kubectl drain` 사용

### 2) 패키지 업그레이드

- 워커 노드에서 kubectl과 kubelet 패키지 업그레이드
- 마스터에서 했던 것과 동일 방식

### 3) kubeadm으로 노드 설정 업그레이드

- kubeadm 업그레이드 명령으로 노드 설정을 새 kubelet 버전에 맞게 업데이트

### 4) kubelet 재시작

- kubelet 서비스 재시작
- 노드가 새 버전으로 올라옴

### 5) uncordon

- drain으로 노드가 unschedulable 상태가 되었으므로 해제 필요
- `kubectl uncordon node1` 실행