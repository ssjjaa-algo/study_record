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

## 공식문서 참고

https://v1-34.docs.kubernetes.io/ko/docs/tasks/administer-cluster/kubeadm/kubeadm-upgrade/

- 강의 내용이 잘 이해되지 않아 공식문서를 기반으로 정리함
- 실습문제 환경은 1.33 → 1.34 업그레이드
    - 공식문서를 들어가보면 1.29 → 1.30 등 버전별로 1단계씩 업그레이드 문서가 다 있다
- 대략적 절차
    1. 기본 컨트롤 플레인 노드를 업그레이드한다.
    2. 추가 컨트롤 플레인 노드를 업그레이드한다.
    3. 워커(worker) 노드를 업그레이드한다.

## **1. Changing the package repository**

If you're using the community-owned package repositories (`pkgs.k8s.io`), you need to enable the package repository for the desired Kubernetes minor release. This is explained in [Changing the Kubernetes package repository](https://v1-34.docs.kubernetes.io/docs/tasks/administer-cluster/kubeadm/change-package-repository/) document.

- 실습 문제를 풀다 보면 아래의 설정을 반드시 해주어야함
- **The legacy package repositories (`apt.kubernetes.io` and `yum.kubernetes.io`) have been `deprecated` (지원 종료)**
    - 따라서 [pkgs.k8s.io](http://pkgs.k8s.io)를 사용해야 하며
    - 아래 명령의 deb에서 `1.33을 1.34로 바꿔서` 설정을 인식시켜줘야 한다
    - minor 버전 별로 관리하기 때문에 아래의 값이 1.34가 아니면 1.34 관련 정보를 아예 다운로드 받을 수 없는 듯 하다
    - 바꿔주어야 `apt-cache madison` 명령어의 결과로 1.34버전을 확인할 수 있다

```bash
# On your system, this configuration file could have a different name
pager /etc/apt/sources.list.d/kubernetes.list

deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.33/deb/ /
```

## **2. Upgrading control plane nodes**

### kubeadm upgrade

- 클러스터를 업그레이드 하기 전에 kubeadm이 먼저 최신 버전이어야 함
- 현재 1.33에서 1.34으로 가려는 상황 (실습 환경에서의 문제)
    - Upgrade the `controlplane` components to exact version `v1.34.0`
    - 위에서 패키지를 1.33 -> 1.34로 변경해주고 나면 결과를 찾을 수 있음

```bash
apt update
apt-cache madison kubeadm # kubeadm의 최신 버전을 찾을 수 있음
 # 1.34.x-00에서 x를 최신 패치 버전으로 바꾼다.
```

- apt-cache madison kubeadm 결과

```bash
controlplane ~ ➜  apt-cache madison kubeadm
   kubeadm | 1.34.4-1.1 | https://pkgs.k8s.io/core:/stable:/v1.34/deb  Packages
   kubeadm | 1.34.3-1.1 | https://pkgs.k8s.io/core:/stable:/v1.34/deb  Packages
   kubeadm | 1.34.2-1.1 | https://pkgs.k8s.io/core:/stable:/v1.34/deb  Packages
   kubeadm | 1.34.1-1.1 | https://pkgs.k8s.io/core:/stable:/v1.34/deb  Packages
   kubeadm | 1.34.0-1.1 | https://pkgs.k8s.io/core:/stable:/v1.34/deb  Packages
```

- 위에서 확인한 버전 중 업그레이드할 버전을 선택해서 아래 명령어 진행

```bash
# replace x in 1.34.x-* with the latest patch version
sudo apt-mark unhold kubeadm && \
sudo apt-get update && sudo apt-get install -y kubeadm='1.34.x-*' && \ # 버전 변경
sudo apt-mark hold kubeadm

kubeadm version # 버전 확인
kubeadm upgrade plan # upgrade plan verify
kubeadm upgrade apply v1.34.x # 버전 업그레이드
```

- Once the command is complete, you should see the following:

```
[upgrade/successful] SUCCESS! Your cluster was upgraded to "v1.34.x". Enjoy!

[upgrade/kubelet] Now that your control plane is upgraded, please proceed with upgrading your kubelets if you haven't already done so
```

- 이후 다른 노드는 `kubeadm upgrade node`로 실행

## 3. Drain the node
- drain은 노드를 `업그레이드 하기 전에` 미리 해줄 것

```bash
# replace <node-to-drain> with the name of your node you are draining
kubectl drain <node-to-drain> --ignore-daemonsets
```

## 4. Upgrade kubelet and kubectl

1. Upgrade the kubelet and kubectl:

```bash
# replace x in 1.34.x-* with the latest patch version
sudo apt-mark unhold kubelet kubectl && \
sudo apt-get update && sudo apt-get install -y kubelet='1.34.x-*' kubectl='1.34.x-*' && \
sudo apt-mark hold kubelet kubectl
```

2. Restart the kubelet:

```bash
sudo systemctl daemon-reload
sudo systemctl restart kubelet
```

## 5. Uncordon the node

- schedulable 상태로 노드 변경

```bash
# replace <node-to-uncordon> with the name of your node
kubectl uncordon <node-to-uncordon>
```

## worker 노드가 여러 개인 경우 반복

- 첫 번째 worker 완료 후 다음 worker에 동일 절차 반복
    - worker에서 kubeadm 업그레이드
    - worker에서 `kubeadm upgrade node`
    - control plane에서 drain
    - worker에서 kubelet(및 kubectl) 업그레이드
    - worker에서 kubelet restart
    - control plane에서 uncordon
- worker 노드 전체가 업그레이드될 때까지 반복

## kubectl get nodes 출력의 의미

- 업그레이드 직후 `kubectl get nodes`에서 master가 여전히 이전 버전처럼 보일 수 있음
- `kubectl get nodes`는 API server 버전이 아니라 **`각 노드의 kubelet 버전을 보여주는 출력`**
    - 따라서 다음 단계로 kubelet을 업그레이드해야 함