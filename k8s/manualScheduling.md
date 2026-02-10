### 1. 스케줄러의 내부 동작 원리

- 모든 포드 정의 파일에는 `nodeName`이라는 필드가 존재함
- 기본적으로 사용자가 포드 매니페스트를 작성할 때는 이 필드를 설정하지 않으며 쿠버네티스가 자동으로 추가함
- 스케줄러는 클러스터 내의 모든 포드를 탐색하며 `nodeName` 속성이 설정되지 않은 포드를 스케줄링 후보로 식별함
- 스케줄링 알고리즘을 실행하여 최적의 노드를 찾은 뒤 **바인딩(Binding) 객체**를 생성하여 해당 포드의 `nodeName`에 노드 이름을 설정함

---

### 2. 스케줄러 부재 시의 현상

- 포드를 모니터링하고 노드에 배치할 스케줄러가 없는 경우 포드는 계속해서 **보류(Pending)** 상태에 머무름

---

### 3. 수동 스케줄링 방법

### **방법 A: 포드 생성 시 노드 이름 지정**

- 스케줄러가 없을 때 가장 간단한 방법으로 포드 생성 시 정의 파일에 `nodeName` 필드를 직접 설정함
- 이렇게 생성된 포드는 즉시 지정된 노드에 할당됨

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: nginx
  labels:
    name: nginx
spec:
  containers:
  - name: nginx
    image: nginx
  nodeName: node01    # 수동으로 노드 지정
```

### **방법 B: 이미 생성된 포드에 할당 (바인딩 API 활용)**

- 쿠버네티스는 이미 생성된 포드의 `nodeName` 속성을 수정하는 것을 허용하지 않음
- 따라서 기존 포드를 노드에 할당하려면 실제 스케줄러의 동작을 모방하여 **바인딩(Binding) 객체**를 생성해야 함
- 바인딩 객체에 대상 노드 이름을 지정하고 포드의 **바인딩 API**로 POST 요청을 전송함
- 이때 YAML 파일을 이에 상응하는 **JSON 형식**으로 변환하여 데이터를 보내야 함

## 쿠버네티스 바인딩 객체 변환 및 전송 예시

### 1. YAML 형식 정의 (원본)

```yaml
apiVersion: v1
kind: Binding
metadata:
  name: nginx         # 노드에 할당할 포드의 이름
target:
  apiVersion: v1
  kind: Node
  name: node01        # 포드를 배치할 대상 노드의 이름
```

---

### 2. JSON 형식 변환 (전송용)

API 서버가 처리하기 위해 위 YAML을 JSON으로 변환한 형태

```yaml
{
    "apiVersion": "v1",
    "kind": "Binding",
    "metadata": {
        "name": "nginx"
    },
    "target": {
        "apiVersion": "v1",
        "kind": "Node",
        "name": "node01"
    }
}
```

---

### 3. API 전송 예시 (curl 사용)

포드의 바인딩 엔드포인트에 POST 요청을 보내 실제 스케줄링을 완료하는 과정

- **엔드포인트 주소:** `http://$SERVER/api/v1/namespaces/default/pods/nginx/binding`
- **요청 명령어:**

```bash
curl -X POST \
  --data '{"apiVersion":"v1","kind":"Binding","metadata":{"name":"nginx"},"target":{"apiVersion":"v1","kind":"Node","name":"node01"}}' \
  -H "Content-Type: application/json" \
  http://$SERVER/api/v1/namespaces/default/pods/nginx/binding
```

---

### 4. 핵심 요약 및 주의사항

- **바인딩 객체 활용:** 이미 생성된 포드의 `nodeName`은 직접 수정할 수 없으므로 반드시 바인딩 API를 호출해야 함
- **데이터 형식:** 쿠버네티스 API 서버는 통신 시 JSON 형식을 사용하므로 YAML을 적절히 변환하여 본문(body)에 담아 전송함
- **대상 경로:** API 경로는 반드시 해당 포드가 속한 네임스페이스와 포드 이름을 포함해야 함