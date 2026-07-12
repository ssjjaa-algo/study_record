# TCP 작동방식

## Send - Receive의 반복

1. 보내는 쪽의 디스크(HDD, SDD 등)에서 정보를 가져와서 Server(Process)의  Buffer에 올려둔다.
2. Socket을 통해서 보내야하기 때문에, Server에서 Socket의 Buffer 에 보낼 수 있는 만큼 보낸다.
3. Encapsulation의 과정을 거치며 다른 Server(Receive의 대상)에 보내게 된다. (세그먼트, 패킷, 프레임 …)
4. Receive한 서버에서는 Decapsulation을 통해서 해당 정보를 확인하기 시작한다.
5. 보내는 것은 Process - > Socket이었으니, 받는 것은 Socket → Process가 된다. 즉, Socket의 Buffer에 받은 데이터를 채워넣는다. Decapsulation을 통해 최종적으로 Segment 모양
6. Socket의 Buffer에서 Process의 Buffer에 내용물을 채우고 Socket의 여유공간을 확보한다.
7. 이 때 ACK 사인을 Send쪽에 보내주게 된다. 
    - TCP에서 ACK 사인을 보낼 때 보내는 정보는 ACK#3 + 여유공간 등의 정보다.
    - 위의 의미로는 1,2번까지의 데이터를 받았단 의미이며, 얼마만큼의 여유공간이 있음을 알려주는 것

## Network 장애

1. LOSS(손실)
    - 어디서 장애가 발생한지는 모르나, 데이터 자체의 손실
2. Re-treansmission, ACK-Duplicate
    - 받는 쪽에서는 해당 데이터가 오지 않은 경우 재전송을 하여 데이터가 오지 않았음을 알려주는데, 하필 그 사이에 받지 못한 데이터가 도착해버린 경우. 그러면 Send한 서버에서는 똑같은 정보를 다시 보내게 되고 Receive에서는 2번 받은 경우이므로 똑같은 ACK 사인을 보내게 된다.
3. Out of order
    - 받는 순서가 잘못된 경우다. 가령 1,2,3,4 순서로 받아야 하는데 1,2,4,3 순서로 받는 것.
4.  Zero window
    - 여유공간이 없다는 표시를 해준 것. 이 경우는 end-point의 문제가 명확하다.
