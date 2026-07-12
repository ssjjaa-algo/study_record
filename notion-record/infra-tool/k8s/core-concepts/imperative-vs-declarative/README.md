# Imperative vs Declarative

## Imperative

- 무엇을 어떻게 할 것인지 지시하는 것
    - B 스트리트에서 우회전 → C스트리트에서 좌회전 → …
- 인프라를 명령형 방식으로 프로비저닝
    - web-server라는 이름의 VM 생성
    - 해당 VM에 nginx 설치
    - 설정 파일을 수정하여 포트를 8080으로 수정
    - ….
- k8s 명령형 방식의 예시
    - kubectl run
    - kubectl create deployment
    - kubectl expose
    - kubectl edit
    - …
    - 포인트 = 객체를 어떻게 생성/수정/삭제할지를 직접 지시
- 순수 명령형 커맨드 (위의 run, create 등)
    - yaml 없이 빠르게 객체 생성/수정 가능
    - 복잡한 객체(멀티 컨테이너, init container 등)는 한계가 있다
    - 실행 후 기록이 안남는다
- 설정 파일 + create / replace
    - yaml 파일로 객체 상태를 정의
    - git 저장 가능
    - 변경 이력 관리 및 리뷰 가능
- kubectl edit는 클러스터 내부에 저장된 객체 정의를 직접 수정하는 방식
    - 로컬 yaml 파일에 반영되지 않음 → 변경사항이 사라질 우려가 있음
    - 장기적으로는 replace 또는 apply를 사용하는 것이 좋음

## Declarative

- 톰의 집으로 가주세요
    - 최종 목적지만 지정
    - **단계별 지시를 하지 않음**
- 인프라를 선언형 접근 방식으로 프로비저닝
    - nginx가 설치된 web-server라는 VM이 필요하다
    - 포트는 8080이어야 한다
    - …
- k8s 선언형 접근 방식의 예시
    - 애플리케이션과 서비스의 기대 상태(expected state)를 정의한 설정 파일들의 집합을 만듬
        - 아마 yaml이 될 것임.
    - 이후 kubectl apply 명령
        - apply를 통해 스스로 현재 상태와 비교하여 필요한 작업을 수행하도록 한다.
- **`kubectl apply만 실행하면 k8s가 알아서 처리하는게 핵심`**

# 문제 풀다가 틀린 것

- **kubectl describe pod**:
    
    실제로 **이미 생성된 Pod의 현재 상태**를 상세히 보여줌 (이벤트, 상태, IP, 컨테이너 상태 등)
    
- **kubectl explain pod**:
    
    Pod **리소스 스펙(YAML 구조와 필드 설명)** 을 보여줌 (설계/작성용, 실행 상태와 무관)
