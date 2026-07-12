# Casting

# 캐스팅

- 타입을 변환하는 것
- 상속 관계에 있는 부모와 자식 간에 형변환 가능

## 종류

- 업 캐스팅 (묵시적 형변환)
    - 자식 클래스가 부모 클래스 타입으로 캐스팅
    - 업 캐스팅을 하면 객체 내의 모든 멤버, 메서드에 접근할 수 없다.
    
    ```jsx
    
    Child child = new child();
    Parent parent = child; // Upcasting
    // Parent parent = (Parent) child; 와 의미가 같다.
    
    // 위의 상황에서 child의 멤버변수, 메서드에 접근 불가.
    // Parent에서 오버라이딩된 메서드를 호출할 경우
    // 자식 메서드 호출.
    ```
    
- 다운 캐스팅 (명시적 형변환)
    - 부모 클래스가 자식 클래스 타입으로 캐스팅
    - **업캐스팅이 선행된 후** 진행되어야 함.
    
    ```jsx
    Child child = new Parent();
    Parent parent = (Parent) child;
    
    ```
    

- 다운 캐스팅 런타임 에러
    
    ```jsx
    Child child = (Child) new Parent();
    ```
    
    - 컴파일 과정에서는 데이터형의 일치만 따짐
    - ClassCastException 발생
    - 왜?
        - **Child 클래스에 Parent를 넣을 수 없다.**
