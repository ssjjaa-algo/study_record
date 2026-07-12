# 4. Http

## HTTP(Hyper Text Transfer Protocol)

- 웹 상에서 정보를 전송하기 위한 통신 프로토콜
- **`서버 - 클라이언트`** 구조를 따르며
- **`request와 response`**의 구조로 웹상에서 정보를 주고받는 구조
- TCP/IP 기반
- **`Stateless & Connectionless`**
    - 동시 접속 최소화.
    - Cookie, Session, jwt등의 도입 이유.
- text 형식으로 데이터 유출 위험
    - Https

## request & response

- 서버와 클라이언트간에 데이터가 교환되는 방식 → **`message`**
- request
    - startline(method, path, Http Version), headers, body

- response
    - statusline(Http Version, status code, status message), headers, body
