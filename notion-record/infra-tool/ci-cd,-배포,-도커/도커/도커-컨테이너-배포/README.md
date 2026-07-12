# 도커 컨테이너 배포

태그: 명령어

## FROM 인스트럭션

- 도커 이미지의 바탕이 될 베이스 이미지를 지정
- Dockerfile로 이미지를 빌드할 때 먼저 FROM 인스트럭션에 지정된 이미지를 내려받음
- FROM에서 받아오는 도커 이미지는 도커 허브 레지스트리에 공개된 것

## RUN 인스트럭션

- 도커 이미지를 실행할 때 컨테이너 안에서 실행할 명령을 정의

## COPY 인스트럭션

- 도커가 동작 중인 호스트 머신의 파일이나 디렉터리를 도커 컨테이너 안으로 복사

## CMD 인스트럭션

- 도커 컨테이너를 실행할 때 컨테이너 안에서 실행할 프로세스를 지정

## 도커 이미지 빌드하기

- docker image build -t 이미지명[:태그명] Dockerfile의 경로

## 도커 컨테이너 실행

- docker container run

## 포트 포워딩

- HTTP 요청을 받는 애플리케이션을 사용하려면
    - 컨테이너 밖에서 온 요청을
    - 컨테이너 안에 있는 애플리케이션에 전달해줘야 한다
        - **`포트 포워딩`**
- 호스트 머신의 포트를 컨테이너 포트와 연결해 컨테이너 밖에서 온 통신을 컨테이너 포트로 전달
- docker container run **`-p`**
    - 호스트_포트:컨테이너_포트
        - -p 9000:8080
    - 호스트가 9000 포트로 연결하면 8080이 받게 설정할 수 있다

## 도커 이미지

- 도커 컨테이너를 다루기 위한 템플릿
    - 파일 시스템, 애플리케이션, 의존 라이브러리, 실행 환경 설정까지 포함하는 아카이브
- 컨테이너의 템플릿 역할을 하는 이미지를 만드는 과정을
    - **`도커 이미지를 빌드`**한다고 표현

<aside>
💡 **`docker help`**를 통해 명령어 확인 가능

</aside>

- docker image —help 등

### docker image build

- Dockerfile에 기술된 구성을 따라 도커 이미지를 생성하는 명령어

<aside>
💡 docker image build -t 이미지명[:태그명] Dockerfile의 경로

</aside>

- -t 옵션은 실제 사용에서 거의 필수적으로 쓰인다.
    - 이미지와 태그명을 붙이는 것
    - docker image build -t example/echo:latest .

- -f 옵션
    - docker image build는 기본적으로 Dockerfile이라는 이름으로 된 Dockerfile을 찾음
    - 다른 파일명의 Dockerfile을 사용하려면 -f 사용해야함
        - docker image build -f Dockerfile-test -t example/echo:latest .
- —pull 옵션
    - 레지스트리에서 받아온 도커 이미지는 삭제하지 않는 한 호스트 운영체제에 저장
    - 위 옵션을 사용하면 매번 베이스 이미지를 강제로 새로 받아온다
    - 이미지를 빌드할 때 확실하게 최신 베이스 이미지를 사용하고 싶다면
        - docker image build —pull=true -t ~

## docker search - 이미지 검색

- 도커 허브에 등록된 이미지 검색

<aside>
💡 docker search [options] 검색키워드

</aside>

## docker image pull - 이미지 내려받기

- 도커 레지스트리에서 도커 이미지를 내려받기

<aside>
💡 docker image pull [options] 리포지토리명[:태그명]

</aside>

## docker image push - 이미지를 외부에 공개하기

- 도커 허브 등의 레지스트리에 등록하기

<aside>
💡 docker image push [options] 리포지토리명[:태그]

</aside>

# 도커 컨테이너 다루기

- 이미지를 바탕으로 만든다.
- 파일 시스템과 애플리케이션이 함께 담겨있는 박스

## docker container run

- 도커 이미지로부터 컨테이너를 생성하고 실행하는 명령어

## 컨테이너에 이름 붙이기

<aside>
💡 docker container run —name [컨테이너명] [이미지명]:[태그]

</aside>

- 운영 환경에서는 같은 이름의 컨테이너를 새로 실행하려면 같은 이름을 갖는 기존의 컨테이너를 먼저 삭제해야 하기 때문에 적합하지 않다고 함
    - 잘 이해는 안가는데 그렇다고는 하네..

이 이상의 명령은 책 보면서 그 때 그 때.
