# 처리율 제한

- Throttling
    - Hard
    - Soft
    - Elastic or Dynamic
    - 처리율 제한
        - prevent brute-force,
        - virtual cap
            - 유저별 API 요청 수 제한 → userId 이용?
                - **`429 Too many Request`**
                - 허용되는 요청 최대 수
                - 남은 요청 수
                - 요청 최댓값이 재설정될때까지의 시간
                    - header에 담아 위 3가지 정보 보내주기?
    
- IP주소
    - Header

# 토큰버킷 선택 이유

- 버스트 요청 처리가 가능하다
    - 누출 버킷은 불가능.

# 구현 방식

- guava의 Cache를 쓰면 됨
    - 몇분 지속되게 할지는 고민해봐야 함
- 토큰 채우고 빼는 것이 중요
    - 최초 담은시간 저장하고
    - 빼는순간에 뺀시간과 빼서 증분만큼 다시 추가
        - 오차범위 내에 들어와있으면 ㅇㅋ
            - 처리율 제한 1분이라 했을 때
            - 10번의 요청 허용
        - 오차범위 벗어나면 최대

# 참고

[[Rate Limit - step 1] Rate Limit이란? (소개, Throttling, 구현시 주의사항 등)](https://etloveguitar.tistory.com/126)
