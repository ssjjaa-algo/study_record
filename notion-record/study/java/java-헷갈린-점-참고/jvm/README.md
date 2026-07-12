# JVM

# JVM(Java Virtual Machine)

- 자바 프로그램의 실행 환경을 만들어주는 소프트웨어
- 자바 코드를 컴파일 → .class 바이트 코드
    - 이 코드가 JVM에서 실행된다.
- .class로 모든 플랫폼에서 동작하도록 할 수 있다.
    
    <aside>
    💡 class 파일은 바이트코드이며 컴퓨터가 읽는 기계어로의 중간 단계 정도임.
    
    </aside>
    

### Independent Platform

- Java는 플랫폼에 영향 x

<aside>
💡

**`JVM은 플랫폼에 종속적`
리눅스의 JVM과 윈도우의 JVM은 다르다.**

</aside>

[](https://github.com/devFancy/2023-CS-Study/blob/main/java/java_jvm_architecture.md)

## JVM 작동 방식

- 자바 프로그램 실행 → JVM이 OS로부터 메모리 할당
- javac(컴파일러)가 .java → .class로 컴파일
- Class Loader를 통해 JVM Runtime Data Area 로딩
- Runtime Data Area로 로딩된 .class들이 Exectuion Engine을 통해 해석
- 해석된 바이트 코드는 Runtime Data Area의 영역에 배치, Execution Engine에 의해 GC의 작동과 스레드 동기화가 이루어짐
