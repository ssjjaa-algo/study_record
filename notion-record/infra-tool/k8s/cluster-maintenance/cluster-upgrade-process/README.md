# Cluster Upgrade Process

- kube-apiserver보다 높은 버전을 가질 수 없다.
    - Controller Manager, kube-scheduler은 -1버전까지
    - kublet, kube-proxy는 -2 버전까지만.
        - 예외로 kubectl은 더 높은 버전 가능

- 버전 업그레이드는 1씩 권장

## 순서

- 마스터 노드 → 워커 노드
    - 마스터 노드 업그레이드되는 동안 kube-apiserver, manager는 잠시 down

## kubeadm

- kubeadm upgrade plan을 통해 정보 확인 가능
- kubeadm은 kublet 설치, 업그레이드 지원하지 않음
- 쿠버네티스와 같은 버전 사용
- 순서
    - apt-get upgrade -y kubeadm=버전
    - kubeadm upgrade apply v1.12.0
    - apt-get upgrade -y kublet=버전
    - systemctl restart kublet —> 다시 시작 필요
    - kubeadm upgrade node config —kubelet-version 버전
