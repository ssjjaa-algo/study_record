# 3.(부가).yml 보안 위한 Credentials 추가

태그: 적용 기록

## Jenkins의 Credentials 등록 방법

- Credentials로 들어간다.

![Untitled](../../../../assets/untitled-143.png)

- Add Credentials를 누른다.

![Untitled](../../../../assets/untitled-144.png)

- script에서 text를 secret할 것이기 때문에 kind는 secret text로 설정하고
- secret에 본인이 실제로 넣을 값을 넣어주고 이름 설정하면 된다.
- Scope는 별도로 건드리지 않음.

![Untitled](../../../../assets/untitled-145.png)

## 추가한 크레덴셜을 빌드환경에서 설정

- 생성해둔 Freestyle project에서 구성을 들어간다.
- 빌드 환경에서 아래 버튼을 클릭 (Use secret …)
- Bindings가 나오면 Add를 누르며 하나하나 추가해주면 된다.

![Untitled](../../../../assets/untitled-146.png)

## 추가한 크레덴셜들을 Build steps의 ‘첫’ 번째로 시작.

![Untitled](../../../../assets/untitled-147.png)

- sed 명령어를 통해 치환할 문자열 → 등록한 문자열(Credential)로 바꾸는 과정.
- application.yml 파일이 위치하는 곳으로 이동 후
- sed 명령어를 통해 변경하고 싶은 문자열이 있는 곳에 크레덴셜 값을 주입
- Gitlab에는 {DB_URL} 이런 식으로 올라가고
- 서버가 실제로 EC2에서 작동될 때는 정상 값들이 들어간 채로 되게 하는 것

```jsx
echo ">application.yml 파일 위치로 이동"
cd /var/jenkins_home/workspace/test/Sub-PJT2/backend/src/main/resources

echo ">application.yml 정보 삽입"
sed -i "s#\${DB_URL}#$DB_URL#" application.yml
sed -i "s#\${DB_USER}#$DB_USER#" application.yml
sed -i "s#\${DB_PW}#$DB_PW#" application.yml
sed -i "s#\${S3_ACCESS}#$S3_ACCESS#" application.yml
sed -i "s#\${S3_SECRET}#$S3_SECRET#" application.yml
sed -i "s#\${KAKAO_CLIENT}#$KAKAO_CLIENT#" application.yml
sed -i "s#\${OPENVIDU_URL}#$OPENVIDU_URL#" application.yml
sed -i "s#\${OPENVIDU_SECRET}#$OPENVIDU_SECRET#" application.yml

cat application.yml
```
