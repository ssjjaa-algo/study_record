# 3. 3-way handshake

## 3-way handshake (Connection Setup의 과정)

- TCP/IP 프로토콜로 통신하기 전 정확한 정보 전송을 위해 상대방 컴퓨터와 세션을 수립하는 과정

- 순서
    - 클라이언트가 서버에 접속을 요청**`(SYN)`**
    - 서버는 요청을 수락하는 ACK를 포함하여 클라이언트에 전송**`(SYN+ACK)`**
    - 클라이언트가 수신한 후 다시 **`ACK`**를 서버에 보냄

## 3단계

- Connection Setup(3-way handshaking)
- Data transfer
- Connection termination(tcp 연결 종료 → 4-way handshaking)

## 4-way handshake

- Connection termination
- 양방향으로 2개의 연결이 독립적으로 닫히기 때문에 4-way 단계
    - 클라이언트가 서버에 FIN 패킷을 보냄
    - 서버는 ACK를 보냄
    - 서버가 프로세스가 끝났다면 클라이언트에  FIN을 보냄
    - 클라이언트가 서버에 ACK를 보냄
