# BOJ5670 - 휴대폰 자판

태그: 입력 NPE

### BufferedReader.readLine()

| 패키지 | 메서드 | 설명 |
| --- | --- | --- |
| BufferedReader | readLine() | - text를 줄 단위로 읽어들인다.- 만약 stream의 끝에 다다르면 (EOF) null 값을 반환한다. |

- 문제의 조건에서 입력은 무한으로 받고 아무것도 입력받지 않을 때 종료해야함
- readLine()을 통해 입력받을 때 EOF에 null을 반환하는 경우를 생각해야함
- 이를 생각하지 않아서 NullPointerException이 발생하였음.
