# `!다시 정리 필요!`

## Kubernetes에서 백업 대상으로 고려할 것

- deployment, pod, service definition file로 여러 애플리케이션을 클러스터에 배포한 상태
- etcd cluster에 클러스터 관련 정보가 저장되는 구성
- 애플리케이션이 persistent storage를 사용하면 그 스토리지도 백업 대상

## 리소스 설정 백업: 선언형 파일 중심

- imperative / declarative 두 가지 방식이 있으나 `관리 관점`에서 `declarative` 선호
- GitHub 같은 managed/public 저장소는 백업 걱정이 상대적으로 적은 편
- 클러스터를 통째로 잃어도 다시 apply해서 애플리케이션 재배포 가능

## 리소스 설정 백업: API 서버에서 덤프

- 팀이 항상 declarative 방식을 지키지 않을 수 있음
- 누군가 imperative로 생성하고 문서화하지 않으면 파일 기반 백업으로는 누락 가능
- 더 나은 방식으로 `Kubernetes API 서버에서 리소스 설정을 조회`해서 전체를 저장
- kubectl로 API 서버를 조회하거나 API 서버를 직접 호출하여 클러스터의 오브젝트 구성을 사본으로 저장
- 모든 namespace의 pods/deployments/services를 YAML로 추출 후 저장

```bash
kubectl get all --all-namespaces -o yaml > cluster-resources.yaml
```

- 위 예시는 일부 리소스 그룹만 포함
- 고려해야 할 리소스 그룹은 더 많음
- 이런 작업을 직접 하지 않아도 되는 도구 존재
    - Heptio의 Ark(현재 Velero) 같은 도구 사용 가능

## etcd 백업

- etcd cluster에는 클러스터 상태 정보 저장
- 리소스별 YAML 백업 대신 `etcd 자체를 백업`하는 선택 가능
- etcd cluster는 master nodes에 호스팅
- etcd 설정 시 데이터 저장 위치`(data directory)` 지정
- 해당 data directory를 백업 도구로 백업 대상으로 잡는 방식 가능
    - etcd.service 파일에
    - —data-dir=/var/lib/etcd 와 같이 설정할 수 있음

## etcd snapshot

- etcd는 built-in snapshot 기능 제공
- etcdctl로 snapshot 저장 가능
- snapshot 저장 예시

```bash
ETCDCTL_API=3 etcdctl snapshot save snapshot.db
```

- 다른 경로에 만들려면 전체 경로 지정 가능
- snapshot 상태 확인 예시

```bash
ETCDCTL_API=3 etcdctl snapshot status snapshot.db
```

## etcd snapshot restore

- restore 시 kube-apiserver를 먼저 `중지`
- restore 명령 예시

```bash
ETCDCTL_API=3 etcdctl snapshot restore snapshot.db --data-dir /var/lib/etcd-from-backup
```

- restore 시 etcd가 새 클러스터 구성을 초기화함
- 기존 클러스터에 실수로 합류하는 상황을 방지하는 목적(??)
- 예시에서 새 data directory가 `/var/lib/etcd-from-backup`로 생성
- etcd 설정 파일에서 data directory를 새 경로로 변경
- daemon reload 후 etcd 서비스 재시작
- 마지막에 kube-apiserver 서비스 시작
- 순서 요약
    - kube-apiserver 중지
    - snapshot restore 수행
    - etcd 설정에서 data-dir 변경
    - daemon-reload
    - etcd 재시작
    - kube-apiserver 시작
- 클러스터가 백업 시점 상태로 복구

```bash
ETCDCTL_API=3 etcdctl \
  --endpoints=https://127.0.0.1:2379 \
  --cacert=/etc/kubernetes/pki/etcd/ca.crt \
  --cert=/etc/kubernetes/pki/etcd/server.crt \
  --key=/etc/kubernetes/pki/etcd/server.key \
  snapshot save snapshot.db
```

## (Working With Etcdctl)ETCD 백업

### etcdctl 사용(스냅샷 기반)

- 실행 중인 etcd 서버에서 스냅샷 생성

```bash
ETCDCTL_API=3 etcdctl \
  --endpoints=https://127.0.0.1:2379 \
  --cacert=/etc/kubernetes/pki/etcd/ca.crt \
  --cert=/etc/kubernetes/pki/etcd/server.crt \
  --key=/etc/kubernetes/pki/etcd/server.key \
  snapshot save /backup/etcd-snapshot.db
```

- 필수 옵션
    - `-endpoints`
        - etcd 서버 주소 지정
        - 기본값은 localhost:2379
    - `-cacert`
        - CA 인증서 경로
    - `-cert`
        - 클라이언트 인증서 경로
    - `-key`
        - 클라이언트 키 경로