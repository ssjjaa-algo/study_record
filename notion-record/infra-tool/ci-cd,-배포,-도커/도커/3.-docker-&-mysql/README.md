# 3. Docker & MySQL

```powershell
## Windows
$ mkdir $HOME\docker\mysql\
$ cd $HOME\docker\mysql
$ docker run --name mysql -p 3306:3306 -v ${pwd}:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=mysql-password -d mysql:8.0

## macOS / Linux
```

- 현재 디렉터리를 컨테이너의 /var/lib/mysql 디렉터리에 마운트
    - MySQL 서버가 데이터를 저장하는 디렉터리.
- 원래는 데이터베이스에 데이터를 기록하더라도 컨테이너를 삭제하면 데이터가 사라짐.
- 호스트의 디렉터리와 컨테이너의 디렉터리를 연결
- 호스트 디렉터리에 데이터베이스의 변경 사항을 저장
    - 컨테이너를 삭제하더라도 다시 디렉터리를 마운트하여 데이터 접근 가능
