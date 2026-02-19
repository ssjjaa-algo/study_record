## Operator

- CRD와 Custom Controller는 별개의 엔티티
    - CRD를 수동으로 생성해야 하고
    - CRD를 사용해 리소스를 생성해야 하며
    - 컨트롤러를 Pod 또는 Deployment로 따로 배포해야 함
- 이 두 가지를 하나의 단위로 패키징해 배포할 수 있도록 해주는 것이 **Operator Framework**

---

## 예시: etcd Operator

- 대표적인 예시 중 하나가 **etcd Operator**
    - etcdCluster CRD
    - etcd를 관리하는 Custom Controller
- etcd Operator는 다음과 같은 추가 기능도 수행
    - etcd 클러스터 백업
    - 백업 복원
    - 장애 발생 시 복구
- 이러한 작업을 수행하기 위해 Operator 내부에는 Backup Operator, Restore Operator 같은 추가 로직이 포함

---

## Operator의 개념적 의미

- Operator는 사람이 애플리케이션을 운영할 때 수행하는 작업을 자동화한다.
    - 애플리케이션 설치
    - 설정 구성
    - 백업 수행
    - 복구 작업
    - 장애 처리
    - 상태 유지

즉,

> Operator는 “애플리케이션을 운영하는 사람”의 역할을 코드로 구현한 것
>

---

# Operator Hub

- 여러 도구들에 대한 Operator를 모아둔 사이트인 것 같음 (like docker hub)
- https://operatorhub.io/
    - etcd
    - MySQL
    - Prometheus
    - Grafana
    - Argo CD
    - Istio 등등.. 종류는 많다.

---

# Operator 배포 단계

- https://operatorhub.io/operator/etcd
    - 예시는 etcd로 하였으나 검색에서 설치하고 싶은 툴을 누르고 install 누르면 다 확인 가능
- install 버튼을 누르면 예시가 나옴
    - 해당 내용은 별도로 정리하지 않고 웹페이지로 들어가서 확인 요망