- kubelet → kube-apiserver → create Pod
    - kube-apiserver에 도착했을 때의 처리

## 요청 처리 순서

- 요청이 API server에 도달하면 authentication 수행
    - authentication은 보통 certificates 기반
- kubectl 요청은 kubeconfig에 certificates가 설정
- authentication이 요청 보낸 사용자를 식별하고 유효성 확인
- 다음으로 authorization 수행
    - authorization은 작업 수행 권한 확인
  - `RBAC`로 권한 확인

      ```yaml
      apiVersion: rbac.authorization.k8s.io/v1
      kind: Role
      metadata:
          name: developer
      rules:
      - apiGroups: [""]
        resources: ["pods"]
        verbs: ["list", "get", "create", "update", "delete"]
        resourceNames: ["blue", "orange"]
      ```
- 예시로 developer 역할이 있으면 pods에 대해 list/get/create/update/delete 가능
- create pod 요청은 허용
- 조건에 해당하지 않으면 거절
- 특정 resource name만 허용 (blue, oragne)

## RBAC로 할 수 없는 제약

- RBAC는 API 접근 권한까지 제어
    - 예시로 Pod에 대해 create는 허용, delete는 금지 같은 제어 가능
    - namespace 범위 제한, 특정 resourceName만 허용 같은 제어 가능
    - RBAC로는 `image가 docker.io면 거절`, `tag가 latest면 거절`, `runAsUser=0이면 거절`, `labels 없으면 거절` 같은 “스펙 내용 조건”을 표현할 수 없음
- 오브젝트 스펙 내용 자체에 대한 정책 강제는 어려움
    - 이미지가 어디서 왔는지, 태그가 latest인지, root로 실행하는지, 라벨이 있는지 같은 내용은 API 권한만으로는 강제 불가

## Admission Controllers 역할

- kubelet → authentication → authorization → `admission controllers` → create pod
- RBAC로 못하는 정책 강제를 담당
- 클러스터 사용 방식에 대한 보안 통제에 사용
- 단순 검증뿐 아니라 다른 동작도 가능
    - 요청 자체 변경 가능
    - Pod 생성 전에 추가 작업 수행 가능

## Admission Controller 예시

- AlwaysPullImages
    - Pod 생성 시 매번 이미지를 pull
- DefaultStorageClass
    - PVC 생성 시 storageClassName이 없으면 기본 storage class 자동 추가
- EventRateLimit
    - API server가 동시에 처리하는 요청 수 제한
- NamespaceExists
    - 존재하지 않는 namespace로의 요청 거절

## NamespaceExists 예시

- 존재하지 않는 namespace blue에 Pod 생성 시도
    - “namespace blue not found” 오류 발생
- 처리 순서
    - authentication 통과
    - authorization 통과
    - admission controllers 단계로 이동
    - NamespaceExists가 namespace 존재 여부 확인
    - 없으면 요청 거절
- NamespaceExists는 기본 활성화

## NamespaceAutoProvision

- 기본 비활성화
- namespace가 없으면 자동 생성

## 기본 활성화 목록 확인

- kube-apiserver에서 enable-admission-plugins 확인
- kubeadm 기반이면 kube-apiserver control plane pod 안에서 kubectl exec로 확인

```bash
#kubeadm에서는 아래
kubectl -n kube-systemexec -it <kube-apiserver-pod-name> -- kube-apiserver -h | grep enable-admission-plugins

#실습환경에서
kube-apiserver -h | grep enable-admission-plugins
```

## Admission Controller 추가

- kube-apiserver의 enable-admission-plugins 플래그에 추가
    - 실습환경 기준 /etc/kubernetes/manifests 아래에서 command에 추가해준다
- kubeadm 기반이면 kube-apiserver manifest에서 플래그 수정

```yaml
---enable-admission-plugins=NamespaceLifecycle,NamespaceAutoProvision
```

## Admission Controller 비활성화

- disable-admission-plugins 플래그 사용

```yaml
---disable-admission-plugins=AlwaysPullImages
```

## NamespaceAutoProvision 활성화 후 동작

- 존재하지 않는 namespace에 Pod 생성 요청
- 처리 순서
    - authentication
    - authorization
    - NamespaceAutoProvision이 namespace 생성
    - Pod 생성 성공
- namespace 목록에서 자동 생성 확인

```bash
kubectl get ns
```

## Admission Controllers가 하는 일

- 요청 검증 후 거절 가능
- 추가 작업 수행 가능
- 요청 변경 가능

## NamespaceExists, NamespaceAutoProvision 변경점

- NamespaceExists, NamespaceAutoProvision은 deprecated
- NamespaceLifecycle로 대체

## NamespaceLifecycle 동작

- 존재하지 않는 namespace 요청 거절
- default namespaces → kube-system, kube-public 등의 삭제 방지
