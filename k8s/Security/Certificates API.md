## 새로운 관리자 합류 또는 인증서 만료되었을 때

- CSR 생성 → CA 서명 → 새 certificate 발급 과정을 반복
- certificate files를 지속적으로 rotation 수행
- 강조하고 싶은 포인트는 **매번 수동으로 일일이 해야한다는 것**

## CA(Certificate Authority)

- CA는 key + certificate 파일 쌍
- 이 파일 쌍을 가진 주체는 Kubernetes 환경에서 어떤 certificate든 서명 가능
    - 원하는 만큼 사용자 생성 가능
    - 원하는 권한으로 사용자 생성 가능
    - 따라서 CA 파일은 엄격하게 보호되어야 함
- CA 파일을 안전한 서버에 저장 → 여기서 안전한 서버가 **`CA 서버`**임
    - CA private key는 해당 서버에만 보관
- certificate 서명은 그 서버에 로그인해서 수행
- 현재는 CA 파일이 Kubernetes master node에 있음
- master node가 CA server 역할도 수행
- kubeadm도 동일
    - CA 파일 쌍을 생성
    - master node에 저장

## 수동 서명의 한계와 자동화 필요

- 사용자 수가 늘고 팀이 커지면 수동 서명 방식은 비효율
- CSR 관리와 만료 시 rotation을 자동화할 필요

## Kubernetes Certificates API

- Kubernetes 내장 certificates API 제공 (Built-in)
- CSR을 Kubernetes에 API로 제출
- certificates API 방식
    - 관리자가 CertificateSigningRequest(CSR 오브젝트)를 생성
    - 클러스터 관리자가 CSR을 조회/검토/승인
    - 승인 후 certificate를 추출해 사용자에게 전달

## CSR 오브젝트 생성 흐름

- 사용자가 key 생성

```bash
openssl genrsa -out jane.key 2048
```

- key로 CSR 생성

```bash
openssl req -new jane.key -subj "/CN=jane" -out jane.csr
```

- CSR을 관리자에게 전달
- 관리자가 CSR 텍스트로 Kubernetes CertificateSigningRequest 오브젝트 생성
- spec.request 필드에 CSR 넣기
- CSR은 plain text를 base64 인코딩 후 넣음

```yaml
apiVersion: certificates.k8s.io/v1betal
kind: CertificateSigningRequest
metadata:
  name: jane
spec:
  groups:
  - system:authenticated
  - usages:
	- client auth
	request: # 여기에 CSR의 base64 인코딩 결과를 넣는다
	
# usages에 들어가는 필드는 공식문서를 참고한다. 임의의 값을 넣으면 Unsupported value 예외
```

## CSR 조회와 승인

- CSR 목록 확인

```bash
kubectl get csr
```

- 특정 CSR 승인

```bash
kubectl certificate approve <csr-name>
```

- 승인 후 Kubernetes가 CA key pair로 certificate 생성
- 거절

```bash
kubectl certificate deny <csr-name>
```

## certificate 추출과 디코딩

- CSR 오브젝트를 YAML로 확인하면 certificate가 포함
- certificate 값도 base64 인코딩 형태

```bash
kubectl get csr <csr-name> -o yaml
```

- base64 decode로 certificate 원문 복원

```bash
echo "<BASE64_CERT>" | base64 --decode
```

- 복원된 certificate를 사용자에게 전달

## 누가 서명 작업을 수행하는지

- certificate 관련 작업은 **`controller manager`**가 수행
- controller manager 내부 컨트롤러에 아래와 같은 컨트롤러가 존재
    - CSR approving
    - CSR signing

## controller manager의 CA 파일 설정

- certificate 서명에는 CA root certificate와 CA private key 필요
- controller manager 설정에 이 파일들을 지정

![Certificates API.png](../img/Certificates%20API.png)