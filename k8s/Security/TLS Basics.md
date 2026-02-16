## 대칭키 암호화(Symmetric encryption)

- 전송되는 데이터를 **`동일한`** 암호화 키로 암호화하는 방식
- **같은 네트워크로 키도 전송되므로 해커의 스니핑 가능**

## 비대칭키 암호화(Asymmetric encryption)

- 하나의 키가 아니라 키 쌍을 사용
    - private key
    - public key
- public lock으로 잠근 데이터는 대응되는 private key로만 열 수 있는 성질
    - **`private key는 공유하면 안 됨`**
- public key(=public lock)는 **공유 가능하지만** “잠그는 것(암호화)”만 가능
    - **`public key로 잠근 것은 private key로만 풀 수 있음`**

## SSH 보안 예시(키 페어)

- `ssh-keygen`으로 public/private 키 생성
- 생성 파일
    - `id_rsa` = private key
    - `id_rsa.pub` = public key(뒤에 pub 붙어있고 Public Lock)
- 서버는 public key로 잠긴 “문”만 열리도록 구성
- 서버의 `~/.ssh/authorized_keys`에 public key 추가로 구성
- 공격자가 public key를 알아도 private key가 없으면 접속 불가
- SSH 접속 시 private key 경로를 지정

```bash
ssh -i ~/.ssh/id_rsa user@server
```

- 서버가 여러 대이면 public key를 여러 서버에 복사 가능
- 같은 private key로 여러 서버에 SSH 가능
- 다른 사용자도 접근해야 하면 각자 키 페어 생성
- 서버에 그 사용자의 public key를 authorized_keys에 추가
- 각 사용자는 자신의 private key로 접속

## HTTPS에서 키 전달 과정

- 대칭키 방식의 문제는 “대칭키를 네트워크로 전달해야 한다는 점”
- 대칭키를 서버에 안전하게 전달할 수 있으면 이후 통신은 대칭키로 빠르고 안전하게 가능
- 안전한 키 전달에 **`비대칭키`** 사용
- 서버에서 public/private 키 페어 생성
- ssh 명령어하고는 약간 다름

    ```yaml
    openssl genrsa -out my-bank.key 1024
    
    openssl rsa -in my-bank.key -pubout > mybank.pem
    ```

- 사용자가 HTTPS로 접속하면 서버가 public key를 제공
- 해커도 트래픽을 스니핑하면 public key를 얻을 수 있음
- 브라우저가 “대칭키”를 생성
- 브라우저가 서버의 public key로 대칭키를 암호화
- 암호화된 대칭키를 서버로 전송
- 서버는 private key로 복호화해 대칭키를 획득
    - 해커는 public key만 있으므로 대칭키를 복호화할 수 없음
- 대칭키는 사용자와 서버만 공유하게 됨
- 이후 실제 데이터 전송은 대칭키로 암호화/복호화
- 해커는 암호문과 public key만 가지게 되어 데이터 해독 불가
- 비대칭키로 대칭키를 안전하게 전달
- 대칭키로 이후 통신 전체를 보호

## 피싱 사이트 문제(서버 신원 검증)

- 해커가 은행 웹사이트와 똑같이 생긴 사이트를 만듦
- 해커 서버에 HTTPS를 구성하기 위해 자기 키 페어 생성
- 네트워크/환경을 조작해 사용자의 은행 접속을 해커 서버로 라우팅
- 사용자는 같은 로그인 화면을 보고 사이트 접속
- URL에 https가 보이고 통신도 암호화되지만 해커서버인 것

## 피싱 사이트 문제 - 서버가 진짜인지 확인하는 방법: 인증서(Certificate)

- 서버는 public key만 보내지 않고 **`certificate`**를 함께 전송
- certificate는 디지털 형태의 신원 증명서
- 포함 정보 예시
    - 누구에게 발급되었는지(issued to, subject)
    - 서버의 public key
    - 서버의 위치 등

## self-signed 문제

- 누구나 “나는 Google이다” 같은 certificate를 만들 수 있음
- 해커도 은행 도메인인 것처럼 certificate를 만들어 사용 가능
- 이런 경우 보통 self-signed certificate
    - 본인이 생성하고 본인이 서명한 certificate
- 브라우저는 certificate 검증 메커니즘을 내장
- self-signed이거나 신뢰되지 않는 서명이라면 경고 표시

## 신뢰되는 인증서 발급: CA(Certificate Authority)

- 권위 있는 기관이 certificate를 서명해 주면 브라우저가 신뢰
- CA 예시
    - Symantec
    - DigiCert
    - Comodo
    - GlobalSign
- 절차
    - 서버에서 키를 만들고 CSR(Certificate Signing Request) 생성
    - CSR에는 도메인 정보와 공개키 정보 포함
    - CSR을 CA에 제출
    - CA가 검증 후 서명된 certificate 발급
- 해커는 도메인 소유자 검증 단계에서 통과하지 못해 서명 발급 실패
- CA는 실제 도메인 소유자임을 검증하는 절차를 수행

## 브라우저가 CA를 신뢰하는 이유

- CA도 public/private 키 페어 보유
- CA는 private key로 certificate를 서명
- 브라우저에는 주요 CA들의 public key가 내장
- 브라우저는 CA public key로 서명을 검증해 진짜 CA 서명인지 확인
- 브라우저 설정의 trusted CAs 탭에서 확인 가능

## Private CA

- 조직 내부에 private CA 운영 가능
- 많은 CA 업체가 사내용 CA 서버 제공
- 사내 CA의 public key를 직원 브라우저에 설치

## 클라이언트 인증서

- 브라우저가 서버를 검증할 수 있어도 서버는 클라이언트가 진짜인지 확실히 모를 수 있음
- 해커가 자격 증명을 훔쳐 사용자로 가장할 수 있음
- TLS로 전송 중 탈취는 막았지만 다른 경로로 자격 증명이 유출될 수 있음
- 그래서 서버가 클라이언트 인증서를 요구할 수 있음
- 클라이언트도 키 페어와 CA 서명 인증서를 발급받아 서버에 제시하고 검증한다

## PKI

- CA, 서버, 사용자, 인증서 생성/배포/유지 프로세스 전체를 PKI(Public Key Infrastructure)라고 부름

## 파일 이름 관례

- public key가 포함된 certificate 파일 확장자 예시
    - `.crt`
    - `.pem`
    - 예: `server.crt`, `server.pem`, `client.crt`, `client.pem`
- private key 파일 확장자/이름 예시
    - `.key`
    - `key` 포함
    - 예: `server.key`, `server-key.pem`
- 이름에 key가 들어 있으면 보통 private key
- key가 들어 있지 않으면 보통 public key 또는 certificate