## Docker에서 ubuntu 컨테이너 실행 예시

- ubuntu 이미지를 가지는 Docker 컨테이너 실행 시 ubuntu 컨테이너가 바로 종료되는 현상
    - 이미지를 실행 후 바로 종료
- 실행 중인 컨테이너 목록(docker ps의 결과)에는 보이지 않음 → 바로 종료되어서
- -a 옵션으로 방금 실행한 컨테이너가 exited 상태는 확인 가능

```bash
docker run ubuntu
docker ps
docker ps -a
```

## 컨테이너가 바로 종료되는 이유

- 컨테이너는 VM처럼 OS를 “호스팅”하는 목적이 아님
- 컨테이너는 특정 작업/프로세스를 실행하는 목적
    - 웹 서버 실행
    - 애플리케이션 서버 실행
    - 데이터베이스 실행
    - 계산/분석 작업 수행
- 작업이 끝나면 `컨테이너도 종료`
- 컨테이너 수명은 내부 프로세스 수명과 동일
- 컨테이너 내부 웹 서비스가 멈추거나 크래시하면 컨테이너도 종료

## 컨테이너에서 실행할 프로세스를 누가 정하는지

- Dockerfile의 `CMD` 지시어가 컨테이너 시작 시 실행할 프로그램을 정의
- nginx 이미지의 CMD는 nginx 실행
- mysql 이미지의 CMD는 mysqld 실행

```docker
RUN
add-apt-repository -y ppa:nginx/stable && \
apt-get update && \
apt-get install -y nginx && \
rm -rf /var/lib/apt/lists/* &&
echo "\ndaemon off;" >> /etc/nginx/nginx.conf &&
chown -R www-data:www-data /var/lib/nginx
# Define mountable directories.
VOLUME ["/etc/nginx/sites-enabled", "/etc/nginx/certs", "/etc/nginx/cor
# Define working directory.
WORKDIR /etc/nginx
# Define default command.
# 지시어
CMD ["nginx"] 
```

## ubuntu 이미지의 Dockerfile과 bash

- ubuntu 이미지 Dockerfile에서 기본 command가 bash로 설정된 구성 ( **`CMD [”bash”]`** )
- bash는 웹 서버/DB 서버 같은 지속 실행 프로세스가 아니라 “터미널 입력을 기다리는 쉘”
    - 터미널을 못 찾으면 bash가 종료
- 앞에서 ubuntu 컨테이너 실행 시 Docker가 bash를 실행
- Docker는 기본으로 **컨테이너에 터미널을 붙이지 않음**
    - bash가 터미널을 못 찾아 종료
    - 시작된 프로세스가 끝났기 때문에 컨테이너도 종료

## 다른 command로 컨테이너 시작

- `docker run` 뒤에 command를 붙이면 이미지의 기본 command(CMD)를 덮어쓰기 가능
- 예시로 `sleep 5`를 붙이면 컨테이너 시작 시 sleep 실행
- 5초 대기 후 종료

```bash
docker run ubuntusleep 5
```

## 변경을 이미지에 영구 반영하기

- ubuntu를 베이스로 새 이미지를 만들고 CMD를 sleep로 지정
- CMD 지정 방법 2가지
    - shell form
    - exec form(JSON array)
- JSON array에서 첫 번째 원소는 **`executable`**이어야 함
- command와 파라미터를 한 덩어리로 쓰지 않고 분리해야 함
    - 잘못된 예: `["sleep 5"]`
    - 올바른 예: `["sleep", "5"]`
- Dockerfile 예시

```docker
FROM ubuntu
CMD ["sleep", "5"]
```

- 빌드와 실행 예시

```bash
docker build -t ubuntu-sleeper .
docker run ubuntu-sleeper
```

- ubuntu-sleeper는 항상 5초 sleep 후 종료

## ENTRYPOINT와 CMD의 차이

- ENTRYPOINT는 실행 파일(executable)을 정의
- CMD는 기본 args를 제공
- ENTRYPOINT를 `sleep`으로 두고, 실행 시 `10`을 주면 entrypoint 뒤에 붙는 형태
- 결과적으로 `sleep 10` 실행
    - `docker run ubuntu-sleeper`처럼 숫자를 안 주면 `sleep`만 실행
    - sleep은 operand(초)가 필요하므로 `missing operand` 에러 발생
- Dockerfile 예시(ENTRYPOINT만 있는 형태)

```docker
FROM ubuntu
ENTRYPOINT ["sleep"]
```

- 실행 예시

```bash
docker run ubuntu-sleeper 10 # 최종 실행: sleep 10
```

## 기본값까지 갖추려면 ENTRYPOINT + CMD 조합

- ENTRYPOINT로 실행 파일을 고정
- CMD로 기본 args 제공
- args를 안 주면 기본값이 사용
- args를 주면 CMD 기본값이 덮어쓰기

```docker
FROM ubuntu
ENTRYPOINT ["sleep"]
CMD ["5"]
```

- 실행 예시

```bash
docker run ubuntu-sleeper# 최종 실행: sleep 5

docker run ubuntu-sleeper 10# 최종 실행: sleep 10
```

- 위 조합은 ENTRYPOINT와 CMD를 JSON(exec) 형태로 써야 기대한 결합 동작이 일어나는 전제

## Pod 파일 (CMD=args, ENTRYPOINT=command)

```yaml
...
spec:
  containers:
	  - name: ubuntu-sleeper
		image: ubuntu-sleeper
		args: ["10"]
			

spec:
  containers:
	  - name: ubuntu-sleeper
	    image: ubuntu-sleeper
	    command: ["sleep", "10"]
	    # 혹은
	    command:
	    - "sleep"
	    - "10"
			
						
```

## 런타임에 ENTRYPOINT 자체를 바꾸고 싶은 경우

- `docker run`에서 `-entrypoint` 옵션으로 ENTRYPOINT 덮어쓰기 가능
- 예시로 entrypoint를 `sleep2.0`(가상의 명령)으로 바꾸고, 인자로 `10`을 주는 형태
- 최종 실행이 `sleep2.0 10` 형태

```bash
docker run --entrypoint sleep2.0 ubuntu-sleeper 10
```