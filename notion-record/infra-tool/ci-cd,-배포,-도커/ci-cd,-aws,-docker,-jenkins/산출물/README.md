# 산출물

# 1. Gitlab 소스 클론 이후 빌드 및 배포할 수 있도록 정리한 문서

# 사용 툴 및 버전

- **웹서버**
    - nginX/1.18.0 (Ubuntu)
- **개발환경** **IDE**
    - IntelliJ IDEA 2023.1.3 - 백엔드(SpringBoot) 개발
        - jdk : OpenJDK - 17.0.7
    - Visual Studio Code 1.81.1 - 프론트엔드(React, Typescript) 개발
        - nodeJS : 18.17.0
- **EC2**
    - Ubuntu 20.04.6
- **Docker**
    - Docker Desktop  (로컬 개발시 사용)
    - EC2 내부 Docker 버전 : 24.0.5
- **Jenkins**
    - 도커 컨테이너 Jenkins
    - 이미지 : jenkins/jenkins:jdk17

# 프론트, 백엔드 설정파일

## 백엔드 설정파일

### application.yml

- {DB_URL} 등의 정보들은 Jenkins에 등록해둔 Credential이
- 서버에 배포할 때 **`sed 명령어를 이용해 치환이 된다.`**
- Jenkins에 Credential들을 등록해두고 빌드 시 값이 대입되도록 설정했는데, 아래 빌드 & 배포 과정에서 설정을 어떻게 했는지 설명한다.

```java
server:
  port: 9999
  servlet:
    context-path: /api

spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USER}
    password: ${DB_PW}
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
        default_batch_fetch_size: 1000 # 최적화 옵션
  sql:
    init:
      encoding: UTF-8

    redis:
      host: tagyou.site
      port: ${REDIS_PORT}

  security:
    oauth2:
      client:
        registration:
          kakao:
            client-id: ${KAKAO_CLIENT}
            redirect-uri: https://tagyou.site/api/login/oauth2/code/kakao
            authorization-grant-type: authorization_code
            client-authentication-method: client_secret_post
            client-name: Kakao
            scope: profile_nickname, profile_image, account_email

        provider:
          kakao:
            authorization-uri: https://kauth.kakao.com/oauth/authorize
            token-uri: https://kauth.kakao.com/oauth/token
            user-info-uri: https://kapi.kakao.com/v2/user/me
            user-name-attribute: id

  servlet:
    multipart:
      maxFileSize: 10MB
      maxRequestSize: 20MB

logging:
  level:
    org.hibernate.SQL: debug

cloud:
  aws:
    s3:
      bucket: tag-u #tagyou
    stack.auto: false
    region.static: ap-northeast-2
    credentials:
      accessKey: ${S3_ACCESS}
      secretKey: ${S3_SECRET}

#OPENVIDU_URL: http://localhost:4443/
OPENVIDU_URL: ${OPENVIDU_URL}
OPENVIDU_SECRET: ${OPENVIDU_SECRET}

#
```

### build.gradle

```java
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.1.1'
    id 'io.spring.dependency-management' version '1.1.0'
}

group = 'com.ssafy'
version = '0.0.1-SNAPSHOT'

java {
    sourceCompatibility = '17'
}

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-jdbc'
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-websocket'
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.2'
    compileOnly 'org.projectlombok:lombok'
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
    runtimeOnly 'com.mysql:mysql-connector-j'
    annotationProcessor 'org.projectlombok:lombok'
    // querydsl 패키지
    implementation 'com.querydsl:querydsl-jpa:5.0.0:jakarta'
    annotationProcessor "com.querydsl:querydsl-apt:${dependencyManagement.importedProperties['querydsl.version']}:jakarta"
    annotationProcessor "jakarta.annotation:jakarta.annotation-api"
    annotationProcessor "jakarta.persistence:jakarta.persistence-api"

    testImplementation 'org.springframework.boot:spring-boot-starter-test'

    implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'io.jsonwebtoken:jjwt-api:0.11.2'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.2'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.11.2'
    testImplementation 'org.springframework.security:spring-security-test'

    implementation 'org.springframework.cloud:spring-cloud-starter-aws:2.2.6.RELEASE'
    implementation 'commons-io:commons-io:2.6'

    implementation 'io.openvidu:openvidu-java-client:2.28.0'

    implementation 'org.webjars:sockjs-client:1.5.1'
    implementation 'org.webjars:stomp-websocket:2.3.4'
    implementation 'org.springframework:spring-messaging:6.0.3'
    implementation 'org.springframework.security:spring-security-messaging:6.0.2'

}

tasks.named('test') {
    useJUnitPlatform()
}

// Querydsl 설정
def generated = 'src/main/generated'

// querydsl QClass 파일 생성 위치 지정
tasks.withType(JavaCompile) {
    options.getGeneratedSourceOutputDirectory().set(file(generated))
}

// java source set 에 querydsl QClass 위치 추가
sourceSets {
    main.java.srcDirs += [ generated ]
}

// gradle clean 시에 QClass 디렉토리 삭제
clean {
    delete file(generated)
}
```

## 프론트 설정파일

### package.json

```tsx
{
  "name": "frontend",
  "version": "0.1.0",
  "private": true,
  "dependencies": {
    "@emotion/react": "^11.11.1",
    "@emotion/styled": "^11.11.0",
    "@mui/icons-material": "^5.14.0",
    "@mui/material": "^5.14.2",
    "@mui/styled-engine-sc": "^5.12.0",
    "@react-spring/web": "^9.7.3",
    "@stomp/stompjs": "^7.0.0",
    "@testing-library/jest-dom": "^5.16.5",
    "@testing-library/react": "^13.4.0",
    "@testing-library/user-event": "^13.5.0",
    "@types/antd": "^1.0.0",
    "@types/express": "^4.17.17",
    "@types/jest": "^27.5.2",
    "@types/kakao-js-sdk": "^1.39.1",
    "@types/node": "^16.18.38",
    "@types/react": "^18.2.15",
    "@types/react-cookies": "^0.1.0",
    "@types/react-dom": "^18.2.7",
    "@types/react-modal": "^3.16.0",
    "@types/react-motion": "^0.0.34",
    "@types/react-router-dom": "^5.3.3",
    "@types/stompjs": "^2.3.5",
    "@types/yaireo__tagify": "^4.17.0",
    "@yaireo/tagify": "^4.17.8",
    "axios": "^1.4.0",
    "emotion-normalize": "^11.0.1",
    "express": "^4.18.2",
    "framer-motion": "^10.15.1",
    "history": "^5.3.0",
    "normalize.css": "^8.0.1",
    "openvidu-browser": "^2.28.0",
    "openvidu-react": "^2.27.0",
    "react": "^18.2.0",
    "react-cookie": "^4.1.1",
    "react-cookies": "^0.1.1",
    "react-dom": "^18.2.0",
    "react-hook-form": "^7.45.2",
    "react-modal": "^3.16.1",
    "react-query": "^3.39.3",
    "react-router-dom": "^6.14.2",
    "react-scripts": "5.0.1",
    "react-slick": "^0.29.0",
    "react-transition-group": "^4.4.5",
    "react-use-websocket": "^3.0.0",
    "recoil": "^0.7.7",
    "recoil-persist": "^5.1.0",
    "slick-carousel": "^1.8.1",
    "socket.io-client": "^4.7.2",
    "sockjs-client": "^1.6.1",
    "stompjs": "^2.3.3",
    "styled-components": "^5.3.11",
    "v6": "^0.0.0",
    "web-vitals": "^2.1.4",
    "ws": "^8.13.0"
  },
  "scripts": {
    "start": "react-scripts start",
    "build": "react-scripts build",
    "test": "react-scripts test",
    "eject": "react-scripts eject",
    "storybook": "storybook dev -p 6006",
    "build-storybook": "storybook build"
  },
  "eslintConfig": {
    "extends": [
      "react-app",
      "react-app/jest",
      "plugin:storybook/recommended"
    ]
  },
  "browserslist": {
    "production": [
      ">0.2%",
      "not dead",
      "not op_mini all"
    ],
    "development": [
      "last 1 chrome version",
      "last 1 firefox version",
      "last 1 safari version"
    ]
  },
  "devDependencies": {
    "@storybook/addon-essentials": "^7.0.26",
    "@storybook/addon-interactions": "^7.0.26",
    "@storybook/addon-links": "^7.0.26",
    "@storybook/blocks": "^7.0.26",
    "@storybook/preset-create-react-app": "^7.0.26",
    "@storybook/react": "^7.0.26",
    "@storybook/react-webpack5": "^7.0.26",
    "@storybook/testing-library": "^0.0.14-next.2",
    "@types/react-slick": "^0.23.10",
    "@types/sockjs-client": "^1.5.1",
    "babel-plugin-named-exports-order": "^0.0.2",
    "eslint-plugin-storybook": "^0.6.12",
    "http-proxy-middleware": "^2.0.6",
    "msw": "^1.2.3",
    "prop-types": "^15.8.1",
    "storybook": "^7.0.26",
    "typescript": "^4.9.5",
    "typescript-styled-plugin": "^0.18.3",
    "webpack": "^5.88.1"
  },
  "msw": {
    "workerDirectory": "public"
  }
}
```

# 사용 기술 스택

## 백엔드

![Untitled](../../../../assets/untitled-194.png)

- SpringBoot 3.1.1
- Spring security 6.1.1
- JPA

## 프론트

![Untitled](../../../../assets/untitled-195.png)

- "react": "^18.2.0",
- "recoil": "^0.7.7",
- "storybook": "^7.0.26",
- "typescript": "^4.9.5",
- "@emotion/react": "^11.11.1"
- sourcetree 3.4.14

### 데이터베이스

![Untitled](../../../../assets/untitled-196.png)

- MySQL 8.0.34
- redis 7.0.12

### 인프라, CICD

![Untitled](../../../../assets/untitled-197.png)

- EC2 (Ubuntu 20.04.6)
- S3
- jenkins (jenkins/jenkins:jdk17)
- nginx(1.18.0 Ubuntu)
- docker (24.0.5)
- Gitlab (ssafy gitlab )

# 인프라 구조

![Untitled](../../../../assets/untitled-198.png)

### 목적 : 빌드 & 배포 **`자동화`**

- Gitlab에서 push가 일어나면 Jenkins가 감지하여 빌드한다.
- Jenkins에서 빌드한 파일 배포 : nginX, (Docker)SpringBoot
- SpringBoot는 mysql, redis, S3와 연동상태
    - SpringBoot와 mysql, redis는 **`같은 도커 네트워크`**를 공유

# CI/CD 과정

<aside>
💡 아래의 내용은 처음부터 CI/CD를 구축하는 과정이다.

</aside>

# 환경

- Gitlab
- Jenkins : openjdk 17버전
    - Jenkins의 jdk 경우 Build할 SpringBoot 프로젝트에서 사용하는 jdk와 동일하게 설정해주었다.
- EC2
    - Ubuntu 환경에서 실행하였음.
- Docker
    - Jenkins를 Docker Container로 구동시킬 것이기 때문에 Docker 필요하다.
    - Jenkins를 이용하여 Build한 결과물(SpringBoot, React) 역시 Docker Container로 구동시켜야 한다.

### Webhook이 필요한 이유.

- Jenkins가 Gitlab에서 **`변경이 일어난 것을 감지해야하기 때문`**
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

- 공식 문서의 내용과 동일한 블로그 참고.

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

- Build → Deploy
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

### Gitlab과 Webhook이 통신하기 위해 Gitlab에서 Personal Access Token 발급

- 우측 상단 본인 아이디 → Edit Profile → Access Token 들어간다.

![Untitled](../../../../assets/untitled-152.png)

- scopes를 선택하는데, 설명을 읽어보고 본인에게 맞는 api를 선택
- 필자의 경우 api를 select 하였음
- Create personal access token 클릭

<aside>
💡 **`발급 받은 Access token의 값을 기억해두어야 한다. 다시 보지 못한다.`**

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
    - Generate는 계속 가능. **그러나 선택한 Secret Token과 Gitlab에 등록되는 것은 동일해야함**
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

# 빌드를 위한 준비물

- 본인이 빌드할 환경에 맞는 **Tool을 설정해야한다**.
- SpringBoot를 gradle 형식으로 했다면 gradle이 필요하고,
- Maven으로 했다면 Maven이 필요하다.
- React라면 node 설정해주기
- 현재 환경
    - SpringBoot gradle 8.1.1
    - nodeJs 18.17.0
- 두 가지 모두 필요하기 때문에 모두 설치한다.

![Untitled](../../../../assets/untitled-167.png)

## 위 화면에서 Tools로 들어가서 설치한다.

### JDK

![Untitled](../../../../assets/untitled-168.png)

- jenkins에서 이미 jdk-17 버전을 가지고 있는 컨테이너를 만들었기 때문에 생략해도 무방하다.
- 그러나 jdk를 미리 깔지 않았다면 Jenkins에서 별도의 설치 작업을 해야한다.
- Jenkins에서 jdk가 필요한 이유는 Jenkins는 Java 기반이기 때문.
- 참고로 자동 설치의 경우 (install automatically)
    - Oracle JDK만 제공, 11이상 버전은 라이센스가 있어야 받을 수 있음.
- docker 환경의 jenkins에서 java— version을 쳐서 확인한다
    
    ![Untitled](../../../../assets/untitled-199.png)
    

### Gradle, NodeJS

- 본인이 Build할 프로젝트와 맞게 설정한다.
- 내가 진행한 환경은 Springboot Gradle 8.1.1과 Nodejs 18.7.0

![Untitled](../../../../assets/untitled-169.png)

![Untitled](../../../../assets/untitled-170.png)

## 생성해둔 Freestyle 프로젝트 → 구성으로 들어간다.

### Build steps

- Gradle 프로젝트를 빌드할 것이기 때문에 설정해준다.
- Tasks에 clean build -x test를 입력한다.
    - 본인이 원하는 방식으로 바꿔도 무방하다.
    
    <aside>
    💡 **`FROM ChatGPT`**
    
    1. **`clean`**: 이는 Gradle 빌드 프로세스에서 이전 빌드에서 생성된 모든 빌드 출력을 제거하고 "클린(clean)"한 상태로 시작하라는 것을 의미합니다. 이는 불필요한 파일이나 디렉토리가 남지 않도록 하기 위해 사용됩니다.
    2. **`build`**: 이는 실제 빌드 작업을 수행하라는 것을 나타냅니다. 프로젝트 소스 코드를 컴파일하고 라이브러리를 생성하며 실행 가능한 파일을 생성하는 등의 작업이 이 단계에서 수행됩니다.
    3. **`x test`**: 이는 빌드 작업을 수행할 때 "test" 태스크를 제외하라는 것을 의미합니다. "test" 태스크는 유닛 테스트를 실행하는데 사용되므로, 이 옵션을 사용하면 빌드 중에 테스트가 실행되지 않습니다. 이를 통해 빌드 시간을 단축할 수 있습니다. 만약에 빌드 이후에 테스트를 별도로 실행하려면 이 옵션을 사용할 수 있습니다.
    
    따라서, "clean build -x test"는 Gradle을 사용하여 프로젝트를 클린(clean)하게 빌드(build)하되, 테스트는 실행하지 않는다는 것을 나타냅니다.
    
    </aside>
    

![Untitled](../../../../assets/untitled-171.png)

- 바로 아래 고급을 누르면 아래와 같은 화면이 보인다.

![Untitled](../../../../assets/untitled-172.png)

- 위의 화면에서 Root Build script의 위치는,
    - Jenkins 내부에서 빌드할 프로젝트가 위치하는 곳.
    - 기본적으로 /var/jenkins_home/workspace가 잡힌다.

- React

![Untitled](../../../../assets/untitled-173.png)

- 여기까지 설정하면, Push가 일어났을 시 정상적으로 빌드가 되도록 설정한 것.

<aside>
💡 아래의 설정파일은 application.yml에 들어갈 정보들을 숨김 처리하기 위한 설정이다.
Credential을 Jenkins에 설정하여 Jenkins가 빌드 시 application.yml 내용 안에 {DB_URL}, {DB_PW} 등에 대해서 읽고 **`치환`**하도록 하는 방식을 사용한다.

</aside>

## Jenkins의 Credentials 등록 방법

- Credentials로 들어간다.

![Untitled](../../../../assets/untitled-143.png)

- Add Credentials를 누른다.

![Untitled](../../../../assets/untitled-144.png)

- script에서 text를 secret할 것이기 때문에 kind는 secret text로 설정하고
- secret에 본인이 실제로 넣을 값을 넣어주고 이름 설정하면 된다.
- Scope는 별도로 건드리지 않음.
- 예를 들어, Secret 값이 DB_PW라고 하자.
    - DB_PW는 실제로 1234라고 하면,
    - Secret에 1234가 들어가고
    - ID에 DB_PW를 넣어준다.

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

## SED 명령어를 사용할 때 주의할 점

- sed 명령 실행 시 &를 만나는 경우
- &는 일치하는 문자열 자체를 의미하므로 ${DB_URL} 이런 식으로 값이 들어가버림.
- 그래서 \& 이렇게 &이 출현하는 곳 바로 뒤에 \을 붙여서 무시할 수 있도록 한다.
- 아래는 Chatgpt
    
    ![Untitled](../../../../assets/untitled-200.png)
    
    # 배포
    
    ## 생성해둔 Freestyle Project에서 빌드 후 조치
    
    - SSH 방식을 이용할 것이기 때문에
    - Publish over SSH 플러그인을 설치해야한다.
    - tools에서 install한다.
        - **Gitlab 플러그인을 설치한 과정을 그대로 따라가서** Publish over SSH 플러그인을 설치해주면 된다.
        
    
    ## 설치 후 Jenkins 관리로 들어간다.
    
    ![Untitled](../../../../assets/untitled-201.png)
    
    - 내리다 보면 Publish over SSH라는 화면이 보이게 된다.
    - Key에는 EC2에 접속하는데 사용하는 .pem 키를 복사 붙여넣기
    
    ![Untitled](../../../../assets/untitled-202.png)
    
    - 추가 버튼을 누르면, SSH Servers가 나온다.
    - Name은 마음대로 설정해도 된다.
    - Hostname은 domain 이름을 설정해준다.
        - ssh hostname
    - Username같은 경우, 필자는 root로 접속하기 때문에 root
        - ssh username
    - Remote Directory는, 전송이 되었을 때 EC2의 시작 경로이다.
    
    ## 2023. 09. 12 publish over SSH 관련 설정 정리
    
    ![Untitled](../../../../assets/untitled-203.png)
    
    - Sourcefiles는 기본적으로 앞에 경로가 생략되있음.
        - /var/jenkins-home/{freestyle 프로젝트명}
        - 그래서 위와 같이 설정해줘도 된다.
    - Remote directory는 기본 위치임.
        - ubuntu 계정으로 접속했다고 가정할 때 /home/ubuntu
        - 그러므로 /home/ubuntu/BackDeploy임.
    
    <aside>
    💡 Exec command할 때의 위치는 /home/ubuntu임.
    그래서 전송한 파일이 있는 경로로 이동하고 싶다면,
    **Remot directory 위치로 이동해주어야 한다!**
    
    </aside>
    
    ## 백엔드 관련 배포
    
    - 구성한 Freestyle Project로 들어간다.
    
    ![Untitled](../../../../assets/untitled-190.png)
    
    - 위와 같이 설정할 것이다. 따라하기 전에 설명부터 봐야 한다.
    - SSH Server : 실제로 전송이 될 EC2 이름을 설정한다.
        - Publish over SSH에서 지정한 Server name을 적어준다.
    - Source files는 jar파일만 서버에 보낼것이다.
    - Remove prefix에서 jar파일 이전 경로를 다 지워준다.
    - Remote directory는 실제로 서버에 전송했을 때 저장이 되는 경로이다.
    - 필자같은 경우는 /root에 jar파일이 저장된다.
        
        ![Untitled](../../../../assets/untitled-191.png)
        
    
    ![Untitled](../../../../assets/untitled-192.png)
    
    - Exec command는 전송 후 실행하는 명령어라고 생각하면 된다.
    
    ```bash
    docker rm -f BS;
    docker build -t project:latest -f Dockerfile .;
    docker run -d --rm -p 9999:9999 --name BS --network springboot-mysql-net project:latest
    
    # 존재하는 BS (백엔드 서버)를 지우고
    # root 경로에 있는 Dockerfile에서 project 이미지를 최신화하고
    # BS 서버를 다시 로드한다.
    
    # 계속해서 BS 서버를 지워주는 것은 최신으로 바꾸고 다시 구동시키기 위함.
    
    ```
    
    ## 프론트 관련 배포
    
    - 빌드 후 조치 추가를 눌러 아래와 같이 작성
    
    ![Untitled](../../../../assets/untitled-193.png)
    
    - 이 경우는 별도로 Exec command 하지 않았다.
    - 프론트 파일 역시 별도의 컨테이너화 해서 관리하고 싶다면
        - Exec command가 백엔드처럼 진행이 되어야하고
        - Dockerfile을 React 파일에 넣어주던, React 파일이 저장되는 위치에 넣어두던 설정해주어야 한다.
    - 현재 본인 서버에는 nginX가 컨테이너화 되어있지 않고 바로 존재함.
        - 이 nginX가 바라보고 있는 정적 파일의 경로가
        - /usr/share/nginx임.
    - 그래서 react 파일을 바로 /usr/share/nginx에 전송시켜준다.
    - 이후의 프록시 설정은 nginX에서 한다.
    

# nginX 설정 및 SSL 적용

- nginX 관련 SSL을 적용하는 방식은 ubuntu 버전에 따라 다를 수 있다.
- 아래 블로그를 보고 참고했다.
    
    [Let’s Encrypt 인증서로 NGINX SSL 설정하기](https://nginxstore.com/blog/nginx/lets-encrypt-인증서로-nginx-ssl-설정하기/)
    

- SSL 적용이 된 nginX 구성파일임

```bash
server {
        listen 80 default_server;
        listen [::]:80 default_server;

        if ($host = www.tagyou.site) {
                return 301 https://$host$request_uri;
         } # managed by Certbot

         if ($host = tagyou.site) {
                 return 301 https://$host$request_uri;
         } # managed by Certbot

        server_name tagyou.site www.tagyou.site;
         return 404; # managed by Certbot

}

server {

         location / {
                root /usr/share/nginx/build;
                index index.html index.htm;
                proxy_set_header Host $host;
                proxy_set_header X-Real-IP $remote_addr;
                proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
                proxy_set_header X-Forwarded-Proto $scheme;
                try_files $uri $uri/ /index.html;
        }

        location /api {
                proxy_pass http://localhost:9999;
                proxy_set_header Host $host;
                proxy_set_header X-Real-IP $remote_addr;
                proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
                proxy_set_header X-Forwarded-Proto $scheme;
        }

        listen [::]:443 ssl ipv6only=on; # managed by Certbot
        listen 443 ssl; # managed by Certbot
        ssl_certificate /etc/letsencrypt/live/tagyou.site/fullchain.pem; # managed by Certbot
        ssl_certificate_key /etc/letsencrypt/live/tagyou.site/privkey.pem; # managed by Certbot
        include /etc/letsencrypt/options-ssl-nginx.conf; # managed by Certbot
        ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem; # managed by Certbot
        server_name tagyou.site www.tagyou.site; #managed by Certboit
}
```

## location /

- root가 /usr/share/nginx/build로 되어있다.
    - nginX가 맨 처음 바라보는 곳이 root → /usr/share/nginx/build이다.
    - 아무런 설정을 하지 않은 경우, 홈페이지 화면에서 **`Welcome to nginX!`** 등의 모습이 보인다.
    - root가 바라보는 곳이 nginX가 기본적으로 설정한 index.html로 되어있기 때문.
    - **우리는 root를 react파일을 배포한 위치로 설정해주어야 한다.**
- 이전에 react파일의 빌드 결과물을 /usr/share/nginx로 전송했다.
- 그러므로, /usr/share/nginx/build를 바라보게 하고
    - **`그 안에 있는 index.html을 바라보게 설정한 것` →** index index.html index.htm;
- try_files $uri $uri/ /index.html;
    - **`이거 빼면 api 통신 못한다.`**

## location /api

- proxy_pass http://localhost:9999;
    - /api로 오는 요청에 대해 [localhost:9999](http://localhost:9999), 즉 BS 컨테이너로 요청을 보내주는 것임.

# 2. 프로젝트에서 사용하는 외부 서비스 정보를 정리한 문서

### KAKAO 로그인

[Kakao Developers](https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api)

- 로그인 로직
    
    ![Untitled](../../../../assets/untitled-204.png)
    
- application.yml
    
    ```yaml
    spring:
    	security:
    	    oauth2:
    	      client:
    	        registration:
    	          kakao:
    	            client-id: ${KAKAO_CLIENT} # 카카오 REST_API KEY가 들어가야 하는 곳
    	            redirect-uri: https://tagyou.site/api/login/oauth2/code/kakao
    							# redirect-uri는 카카오 홈페이지에서 등록해두어야함
    	            authorization-grant-type: authorization_code
    	            client-authentication-method: client_secret_post
    	            client-name: Kakao
    	            scope: profile_nickname, profile_image, account_email
    
    	        provider:
    	          kakao:
    	            authorization-uri: https://kauth.kakao.com/oauth/authorize
    	            token-uri: https://kauth.kakao.com/oauth/token
    	            user-info-uri: https://kapi.kakao.com/v2/user/me
    	            user-name-attribute: id
    ```
    
    - 카카오 REST API Key
        
        ![Untitled](../../../../assets/untitled-205.png)
        
    - 위의 REST API 키를 application.yml에 등록해줘야한다.
    - redirect-uri : 우리 서버를 통해서 kakao server로 uri를 redirect 시켜야 한다.
        - 해당 redirect uri는 카카오 홈페이지에서 등록해놓아야 한다.
            
            ![Untitled](../../../../assets/untitled-206.png)
            
    - 서비스에서 필수적으로 사용해야 하는 것은 닉네임, 프로필 사진이고 이메일은 default
        
        ![Untitled](../../../../assets/untitled-207.png)
        

# 3. DB 덤프 파일 최신본

- 덤프 파일 없음.

# 4. 시연 시나리오

- tagyou.site에 접속하여 카카오 로그인 진행
- 로그인 진행 후 처음 맞이하는 화면 : 추가 정보 입력
- 입력 이후 메인화면
    - 현재 내 정보를 볼 수 있는 UI가 떠있다.
    - 좌측 상단의 달 아이콘을 클릭하면 UI의 색깔이 변경된다.
        - 수정이 가능하다.
    - 좌측 하단 미팅
        - 일대일 매칭
        - 다대다 매칭
    - 우측 바에 친구 목록
        - 친구 검색
            - 친구 검색 결과 사용자가 존재하는 경우 친구추가 가능
        - 친구 추가

## 매칭

- 매칭은 내가 들어가 있는 어떤 랜덤방에 사람이 가득 찰 때까지 대기한다. (대기중 이란 화면이 계속 떠있는 상태)
- 매칭완료 시
    - 게임 방 이동 후 게임에 참여한 사용자의 모습이 보인다
    - 게임 방 내에서 채팅이 가능하다.
    - 미니게임을 눌러 실행하면 게임이 된다.
        - 아직 미니게임이 될지 안될지 모르겠다.
