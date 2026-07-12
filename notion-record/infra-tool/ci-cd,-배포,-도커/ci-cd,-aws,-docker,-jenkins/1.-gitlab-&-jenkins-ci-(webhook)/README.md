# 1. Gitlab & Jenkins CI (Webhook)

태그: 적용 기록

# 환경

- Gitlab
- Jenkins : openjdk 17버전
    - Jenkins의 jdk 경우 Build할 SpringBoot 프로젝트에서 사용하는 jdk와 동일하게 설정해주었다.
- EC2
    - Ubuntu 환경에서 실행하였음.
- Docker
    - Jenkins를 Docker Container로 구동시킬 것이기 때문에 Docker 필요하다.
    - Jenkins를 이용하여 Build한 결과물(SpringBoot, React) 역시 Docker Container로 구동시켜야 한다.

# CI

### 개념 설명

- Jenkins는 Gitlab에서 **변경이 일어난 것을 감지**해야 한다. 이것을 감지하게 해주기 위해서 Gitlab에서 **`Webhook` 을 걸어줘야 한다.**
    - 그래서, Gitlab이 Jenkins에게 **나 변동이 일어났다**! 라고 알려줘야함.
    - 언제 알려줄건가?
        - 여기서 세팅할 환경은 **Push Events**, 즉 **`Push가 일어났을 때.`**
        
        <aside>
        💡 Gitlab에서 Push를 하면 Jenkins가 감지하게 한다.
        
        </aside>
        
    - 굳이 Push Events가 아니어도, 목적에 따라 다를 수 있음.
    - **결론적으로**
        
        <aside>
        💡 Gitlab → Jenkins **`(Webhook 필요)`**
        
        </aside>
        

## Ubuntu에서 도커 설치하기

- 공식 문서의 내용과 동일한 블로그

[[Linux] 우분투(Ubuntu)에서 도커(Docker) 설치 하기](https://jkim83.tistory.com/166)

```bash
// 순서대로 입력한다.

sudo apt-get update

sudo apt-get install \
    apt-transport-https \
    ca-certificates \
    curl \
    gnupg \
    lsb-release

curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt-get update

sudo apt-get install docker-ce docker-ce-cli containerd.io
```

## Jenkins

### 목적

- Gitlab Build → Deploy
- Gitlab에서 받은 파일을 Build하여 **이상이 없음을 체크**하고 (build가 되지 않으면 Jenkins에서 막힌다.)
- 이상이 없다면 만들어진 결과물을 **서버**에 전송한다
    - **EC2**에 전송하는 것.

### Jenkins 이미지를 가진 도커 컨테이너

- jdk 버전은 **본인의 프로젝트에 맞게** 설정.
- ec2에서 아래 명령어를 사용

```bash
sudo docker run -d \
-u root \
-p 9090:8080 \
--name=jenkins \
-v /home/ubuntu/docker/jenkins-data:/var/jenkins_home \
-v /var/run/docker.sock:/var/run/docker.sock \
jenkins/jenkins:jdk17

#docker run : Docker 컨테이너 실행
# -d : 백그라운드 모드로 실행
# -u root : 컨테이너 안에서 실행되는 프로세스 사용자 root로 설정
# -p 9090:8080 : 호스트가 9090으로 접속할 경우 8080포트로 포워딩.
# --name=jenkins : 실행 중인 컨테이너의 이름

# -v /home/ubuntu/docker/jenkins-data:/var/jenkins_home
# 호스트 디렉터리 /home/ubuntu/docker/jenkins-data를
# /var/jenkins_home 디렉터리와 마운트

# -v /var/run/docker.sock:/var/run/docker.sock
# 호스트의 Docker 소켓을 컨테이너 내부의 Docker 소켓으로 마운트 
# 이를 통해 컨테이너 내부에서 호스트의 Docker 데몬을 사용하여 
# 다른 컨테이너를 관리

#jenkins/jenkins:jdk17 : jdk17 버전의 jenkins 이미지
```

### Jenkins 컨테이너 접속 및 설정

<aside>
💡 순서대로 따라가면 된다.

</aside>

- 참고한 블로그
    
    [[CI/CD] Jenkins 설치하기 (Docker)](https://seosh817.tistory.com/287)
    
- ec2에서 아래 명령어 실행.

```bash
docker exec -it jenkins /bin/bash
# jenkins 컨테이너에 접속하는 명령어.

cat /var/jenkins_home/secrets/initialAdminPassword

# cat 명령어의 결과물로 무슨 password가 나타남. 
```

- [http://도메인:9090](http://도메인:9090) 을 이용하여 jenkins에 접속한다.

![Untitled](../../../../assets/untitled-148.png)

- cat 명령어의 결과를 복사해서 위 화면에 입력 후 Continue

![Untitled](../../../../assets/untitled-149.png)

- 비밀번호 입력 후 다음 화면에서 Install suggested plugins 선택

![Untitled](../../../../assets/untitled-150.png)

- 설치가 되면 계정을 생성한다.
    - 이 계정은 Jenkins에 로그인 할 때 필요한 계정임

![Untitled](../../../../assets/untitled-151.png)

- Jenkins 접속 URL을 입력한다.
- 현재 젠킨스 도커 포트는 9090으로 접속하게 해둔 상태이다.
- 그러므로, [**`http://도메인이름:](http://도메인이름:8080)9090`** 으로 설정한다.

## 위의 과정을 마치면 jenkins의 기본 홈페이지가 뜰 것임.

## Webhook

### Gitlab과 Webhook이 통신하기 위해Gitlab에서 Personal Access Token 발급

- 우측 상단 본인 아이디 → Edit Profile → Acces Token 들어간다.

![Untitled](../../../../assets/untitled-152.png)

- scopes를 선택하는데, 설명을 읽어보고 본인에게 맞는 api를 선택
- 필자의 경우 api를 select 하였음
- Create personal access token 클릭

<aside>
💡 **`발급 받은 Access token의 값을 기억해두어야 한다.`**

</aside>

### Jenkins → Jenkins 관리 → plugins에 들어간다.

![Untitled](../../../../assets/untitled-153.png)

![Untitled](../../../../assets/untitled-154.png)

- plugins로 들어가서 **Gitlab을 검색**하여 Install 받는다.

## 이후 다시 위의 사진에서 System을 들어간다.

- Gitlab이라 들어와 있는 부분에서 본인에 맞게 작성한다.
- GItlab host URL은 gitlab 서버임.

![Untitled](../../../../assets/untitled-155.png)

- Credential은 Add해줘야 한다. 아래 참고

![Untitled](../../../../assets/untitled-156.png)

- GitLab API token을 설정 후,
- API token에 발급받은 Gitlab Access token을 입력 후
- ID는 본인이 관리하고 싶은 ID를 설정한다.
    - 예시 : Gitlab-Jenkins Personal Access token

## 위의 토큰은 Personal Access Token이다.

## Freestyle Project를 생성한다.

![Untitled](../../../../assets/untitled-157.png)

- Git이라 나와있는 부분에서 작성해야 한다.
- Repository URL 형식 (Gitlab 페이지 참고)
    - https://[token-id]:[token-value]@lab.ssafy.com/프로젝트/프로젝트세부명
    - **`s09-webmobile1-sub2/S09P12A705`**
    
    ![Untitled](../../../../assets/untitled-158.png)
    
    - 위의 URL은 http 방식의 git을 clone해도 상관없지 않을까..? 테스트 해봐야 알 듯.

- 아래에 Build할 Branch를 설정한다.
- 아래 예시로 보면,
    - feature/nginX라는 branch에서 push를 하면 Jenkins가 감지하여 빌드하는 개념이라고 생각하면 된다.

![Untitled](../../../../assets/untitled-159.png)

## 빌드 유발 설정

![Untitled](../../../../assets/untitled-160.png)

- Gitlab에 push가 일어날 시 WebHook을 걸기 때문에 위와 같이 설정한다.
- Comment 바로 아래에 고급이라는 부분을 눌러주자.
    
    ![Untitled](../../../../assets/untitled-161.png)
    

![Untitled](../../../../assets/untitled-162.png)

- 맨 아래에 보이는 Secret Token은 Generate하여 Gitlab에서 Webhook할 때 넣어줘야 한다.
    - Generate는 계속 가능. 그러나 선택한 Secret Token과 Gitlab에 등록되는 것은 동일해야함
- Secret Token 값을 복사하여 Gitlab으로 간다.
- Gitlab → 프로젝트 선택 → 왼쪽 화면에서 Settings → Webhooks

![Untitled](../../../../assets/untitled-163.png)

- 위와 같은 화면이 나오는데 URL에는 jenkins의 경로를 적어준다.
    - 이 URL의 경로는 어디있나?
    - **빌드 유발 부분에 보이는 `GitLab webhook URL`**이다.
    
    ![Untitled](../../../../assets/untitled-164.png)
    
- 복사해서 가져온 Secret Token도 위에 붙여준다.
- Trigger에서 Push events를 설정한다.
    - 모든 브랜치에서 푸쉬가 일어날 때마다 선택할 수도 있으나, 이러면 Push가 일어나기만 해도 빌드가 된다.
        - jenkins에서 감지하는 것이 모든 브랜치가 되는 것임.
    - 그래서
        - **`Regular expression`** 선택하고 감지할 브랜치 설정
        
        ![Untitled](../../../../assets/untitled-165.png)
        

![Untitled](../../../../assets/untitled-166.png)

- Add Webhook을 누르면 위와 같은 Project Hooks가 생긴다.
- Test를 눌러 200을 반환받으면 Webhook이 성공적으로 이루어진다.

## 일단 여기까지 하고 Jenkins Freestyle을 저장해두자.

- 이후 내용은 Jenkins(빌드)에 있다.
