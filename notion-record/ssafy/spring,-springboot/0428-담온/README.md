# 0428 담온

- DOM XSS
    - text로 보낸 객체가 js단에서 받을 때 다시 부활해서 발생
    - innerHTML 대신 innerText를 하는 것
    
- 게시판은 항상 취약하다.
    - 사용자의 입력을 그대로 줘야하기 때문에
        - **`무조건 필터링 해야한다`**
        

- FileUpload는 절대 S3에 하면 안된다.
    - Upload path를 어디로 잡아야겠다가를 고민해야한다.
