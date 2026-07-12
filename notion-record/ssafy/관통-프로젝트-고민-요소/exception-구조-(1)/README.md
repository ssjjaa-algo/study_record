# Exception 구조 (1)

- 어떻게 관리해야 좋을까?
- RuntimeException을 각각 상속하는 Exception들을 만들어?
    - RuntimeException
        - GlobalException
            - BusinessException
        - 근데 exception 종류를 나누는것도 또 일이다
            - 너무 세분화 시키면 좋지 않음

- 적절한 수준에서 분할이 필요
    - `GlobalControllerAdvice`
    - `BusinessControllerAdvice`
    - 일단 이 수준으로만 분할
