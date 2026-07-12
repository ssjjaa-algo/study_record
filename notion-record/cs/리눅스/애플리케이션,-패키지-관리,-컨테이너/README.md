# 애플리케이션, 패키지 관리, 컨테이너

# 1. 커널을 부트스트랩한다(?)의 의미가 뭔가요 p152

- boot loader = bootstrap loader이다(boot 용어의 유래가 bootstrap)
- **boot loader는 원하는 커널을 메모리에 로드하고, 커널이 필요로 하는 모든 드라이버와 모듈, 파일시스템등이 담긴 RAM 디스크 파일을 메모리에 로드하는 역할**

**GRUB 2 예시**

- **GRUB Stage 1.** MBR 또는 VBR에 저장되어 있는 부트 이미지가 메모리에 로드되고 실행(core.img의 첫 번쩨 섹터 로드)
- **GRUB Stage 1.5**MBR과 첫번째 파티션 사이에 있는 블록(a.k.a MBR gap)에 저장된 core.img가 메모리에 로드되고 실행. core.img의 configuration 파일과 파일시스템을 위한 드라이버를 로드한다.
- **GRUB Stage 2**/boot/grub 파일 시스템에 직접 접근하여 **커널(vmlinuz)의 압축을 풀어 메모리에 로드하고, 커널이 필요로 하는 모든 드라이버와 모듈, 파일시스템(ext2, ext3, ext4...)등이 담긴 RAM 디스크 파일(initrd.img)를 메모리에 로드**한다.

참고

https://www.wikiwand.com/ko/articles/%EB%B6%80%ED%8C%85

[https://yonlog.tistory.com/59](https://yonlog.tistory.com/59)

# 2. 패키지 관리자는 순환 종속성에 대해 관리를 하나?(미완)

- A→ B → C → A
    - 문제가 안되나?
    1. 다운로드 된 패키지들을 모두 인식하고 있는가.
        1. A→ B → C → A 
        2. 무한 다운로드 안되나?
    2. 패키지를 패키지 저장소에 푸시할 때 의존성을 체크하고 충돌 여부 판단하는지
    3. 버전 충돌이란..
- 의존성 충돌 일어나는 경우가 뭔지. 어떻게 해결하는지.
- 고수준 패키지 관리자들은 순환 종속성(cyclic dependency) 관리를 지원한다고 합니다.
    - 어떻게해? 이건 모르겠다

참고

- [https://stackoverflow.com/questions/41663844/how-to-solve-cyclic-dependencies-while-installing-software-on-linux](https://stackoverflow.com/questions/41663844/how-to-solve-cyclic-dependencies-while-installing-software-on-linux)
- [https://minhan2.tistory.com/entry/리눅스-패키지-비교aptdpkgyumrpm](https://minhan2.tistory.com/entry/%EB%A6%AC%EB%88%85%EC%8A%A4-%ED%8C%A8%ED%82%A4%EC%A7%80-%EB%B9%84%EA%B5%90aptdpkgyumrpm)

# 3. Debian은 패키지 관리자가 아니네요? 얘 패키지네요?(미완. 애매한 용어 다 정리 필요..)

- Debian
    - 리눅스 운영체제 배포판(Distro) 중 하나
- **.deb**
    - Debian 운영체제에서 사용되는 **패키지 포맷의 확장자**
- 이러니까 패키지 개념이 이해가 안된다.
    - 패키지랑 프로그램 차이가 불명확하네요
- 프로그램 : 실행될 수 있는 바이너리 파일, 셸 스크립트
- 패키지 :  프로그램 + 구성인 파일로 애플리케이션을 배포하는데 사용된다(p151). 패키지 자체는 압축된 파일 하나이고, 메타데이터가 포함되어있다(p161).
    - 가장 큰 차이는 프로그램은 실행 목적
    - 패키지는 배포 목적으로 메타데이터에 구성요소를 포함한다

애매한 용어 잡고가기

- 라이브러리
    - 패키지보다 더 작은 단위
- 모듈
    - ????

> Both *apt* and *apt-get* are command line tools. You can use them to manage software packages like applications and libraries on Debian-based Linux servers and server instances. - AWS
> 
> - [https://aws.amazon.com/ko/compare/the-difference-between-apt-and-apt-get/](https://aws.amazon.com/ko/compare/the-difference-between-apt-and-apt-get/)

# 도커 데몬이란?(
