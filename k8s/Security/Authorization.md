## Authorization

- 리소스별 권한 제어를 위한 것
    - ex) 관리자 / 개발자 / 서로 다른 팀 / 모니터링 툴 별 권한을 다르게 주는 것이 목적
- Kubernetes에서 지원하는 authorization 메커니즘 목록
    - node authorizer
    - attribute based access control(ABAC)
    - role based access control(RBAC)
    - webhook

---

## Node Authorizer 동작 맥락

- kube-apiserver는 사용자(관리 목적)뿐 아니라 클러스터 내부의 구성요소도 접근하는 대상
    - kubelet 같은 것
- kubelet은 노드 상태 같은 정보를 API server에 보고(report)하는 역할
    - 이러한 요청은 `node authorizer`라는 특수한 authorizer가 처리
    - 사용자 이름이 `system:node:<노드이름>`

---

## 외부 사용자에 대한 Authorization과 ABAC

- ABAC는 사용자 또는 사용자 그룹을 `권한 집합`과 연결하는 방식임
    - 예시로 dev 유저에게 pods에 대해 view/create/delete 권한을 부여

    ```json
    // json 요청으로 보냄
    {"kind": "Policy", "spec": {"user": “dev-user", "namespace": "*", "resource": “pods", "apiGroup": "*"}}
    
    ```

    ```json
    // 파일 생성 관리 = 매번 수기 추가의 부담 존재
    [{"kind":  "Policy", "spec": {"user":  "dev-user", "namespace":  "*", "resource":  "pods", "apiGroup": "*"}},
    {"kind":  "Policy", "spec": {"user":  "prod-user", "namespace":  "*", "resource":  "pods", "apiGroup": "*"}},
    {"kind":  "Policy", "spec": {"user":  "aws-user", "namespace":  "*", "resource":  "pods", "apiGroup": "*"}},
    {"kind":  "Policy", "spec": {"user":  "admin", "namespace":  "*", "resource":  "pods", "apiGroup": "*"}}]
    ```

    - 운영 부담 존재 (수기 변경)

---

## RBAC 개념과 장점

- ABAC처럼 사용자/그룹에 권한을 직접 붙이는 대신 역할(Role)을 정의하는 방식임
    - 개발자용 권한 집합을 `Role`(dev-role)로 정의하고 개발자를 Role에 `Binding`
    - 관리자용 권한 집합을 Role(sec-role)로 정의하고 관리자를 Role에 Binding
- 사용자 접근 변경이 필요할 때 Role만 바꾼다

---

## Webhook

- 서드 파티 (외부 시스템)을 이용하여 권한 관리를 맡기고 싶을 때 사용할 수 있음

---

## AlwaysAllow / AlwaysDeny 모드

- AlwaysAllow는 authorization 체크 없이 모든 요청을 허용
- AlwaysDeny는 모든 요청을 거부

---

## Authorization Mode

- 옵션을 지정하지 않으면 기본값이 AlwaysAllow로 설정되는 흐름임
- 콤마로 구분된 여러 모드를 **`순서대로`**입력 → 여러 개면 순서대로 매칭함
    - 순서대로 매칭이 안되면 다음 것을 확인하고, 어느 순간 매칭이 되면 더이상 확인하지 않음

```bash
kube-apiserver \
  --authorization-mode=Node,RBAC,Webhook
```

---