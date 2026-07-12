# 5. nginX 설정

태그: 적용 기록

# nginx 삭제 후 재설치

[꽃지닷컴 - 우분투 Nginx 지우고 다시 설치하기](https://ggotji.com/index.php?mid=study&document_srl=5253)

# SSL 적용

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
