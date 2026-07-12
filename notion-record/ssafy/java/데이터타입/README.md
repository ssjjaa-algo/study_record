# 데이터타입

태그: 자료형, 형변환

[Variable]

기본형 : 미리 정해진 크기의 데이터 표현, 변수 자체에 값 저장

참조형 : 크기가 미리 정해질 수 없는 데이터의 표현, 변수에는 실제 값을 참조할 수 있는 주소만 저장

| 구분 | Type | bit 수 | 값 |
| --- | --- | --- | --- |
| 논리형 | boolean |  | true/false |
| 정수형 | bte | 8 | -2^(비트수-1)~2^(비트수-1) -1 |
| short | 16 |  |  |
| int (기본형) | 32 |  |  |
| long | 64 |  |  |
| 실수형 | float | 32 |  |
| double (기본형) | 64 |  |  |
| 문자형 | char | 16 |  |
- 맨 앞 비트는 부호비트로 bit수 - 1 제곱이 크기임.
- 오버플로우(Overflow)는 '오류'는 아님.
- 실수의 연산은 부정확하다.

- 유효 자리수를 이용한 반올림 처리가 필요하다.

2.0f - 1.1f = 0.9

1) 실수를 정수로 바꾸고 정수를 실수로 바꾸는 과정을 거침.

2) BigDecimal을 이용한 연산.

## 형 변환

- 변수의 타입을 다른 타입으로 변환.
- 기본형은 기본형끼리, 참조형은 참조형끼리 형 변환 가능
    - 기본 타입과 참조형의 형 변환을 위해서는 Wrapper 클래스 사용
    - (Integer, Double, Float... 앞에 기본형 8개에 대해서 8개의 Wrapper클래스 존재)
- 작은집 -> 큰집 (데이터 손실 x), 큰집 -> 작은집 (데이터 손실 o)
- 묵시적 형 변환 / 명시적 형 변환
- 값의 크기, 타입의 크기가 아닌 타입의 '표현 범위'가 커지는 방향으로 할당할 경우는 묵시적 형변환 발생
- 비트수만으로 변환하는 것은 아님.
- short와 char는 호환이 안되는 이유? : char는 시작이 0부터이기 때문.
    - char는 음수를 표현할 수가 없음
- 명시적 형변환은 값손실을 프로그래머 책임하에 진행 (값에 대한 손실이 발생할 때는 반드시 필요)
- 묵시적 형변환은 JVM이 서비스해줌.

```bash
package com.ssafy.a_basic.basic;

public class BP_09 {
    public static void main(String[] args) {
        int i1 = Integer.MAX_VALUE;
        int i2 = i1 + 1;
        System.out.println(i2); // Overflow

        long l1 = i1 + 1; // 깨진 값을 long에 할당한 것임
        System.out.println(l1);

        long l2 = (long) (i1 + 1); // i1 +1이 연산과정에서 이미 깨진 값임
        System.out.println(l2);

        long l3 = (long) i1 + 1; // i1이 long으로 형변환 후 +1이기 때문에 가능
        System.out.println(l3);

        // 컴퓨터는 수 연산을 왼쪽 -> 오른쪽으로.
        int i3 = 1000000 * 1000000 / 100000;
        int i4 = 1000000 / 100000 * 100000;
        System.out.println(i3 + " : " + i4);
    }
}
```

```bash
package com.ssafy.a_basic.basic;

public class BP_11 {

    @SuppressWarnings("unused")
    public static void main(String[] args) {
        byte b1 = 10;
        byte b2 = 20;
        // TODO:
         byte b3 = b1 + b2; // 실제로 더하기를 하는 과정에서 int가 된다.

        int i1 = 10;
        long l1 = 20;
        // TODO: 연산을 할 때는 두 타입을 맞추어야 함.
         int i2 = i1 + l1;

        // TODO:
         float f1 = 10.0; // 뒤에 f를 붙여줘야 float, 안붙여주면 double임
         float f2 = f1 + 20.0; // 20.0이 double이기 때문에 두 type을 맞춰주어야 함
    }

}
```

```bash
package com.ssafy.a_basic.basic;

public class BP_15 {
    public static void main(String[] args) {

        int a = 10;
        int b = 20;
        System.out.println((a > b) & (b > 0));

        System.out.println((a += 10) > 15 | (b -= 10) > 15); // 결과는 뻔한데 굳이 뒤에거까지 연산을 하겠다는 것임 얘는 b가 20임.
        System.out.println("a = " + a + ", b = " + b);

        a = 10;
        b = 20;
        System.out.println((a += 10) > 15 || (b -= 10) > 15); // 얘는 b가 10
        System.out.println("a = " + a + ", b = " + b);
    }
}
```

[배열]

- 동일한 타입의 데이터 '0'개 이상을 연속된 메모리 공간에서 관리하는 것.

* int[] arr --> arr의 타입 = 참조형, primitive 타입에는 []타입이 없다.

참조형 변수의 기본값 : null

문자열

- toCharArray() : 문자열을 Char 배열로 변환.
    - for-each with Array
    
    - index 대신 직접 요소(element)에 접근하는 변수를 제공
    

```bash
for (int i=0; i< intArray.length; i++) // index가 필요한 경우
{
	int x = intArray[i];
    System.out.println(x);
}

for (int x : intArray) // index를 사용할 수 없다.
{
	System.out.println(x);
}
```

- api 제공하는 배열 복사 method

- System.arrayCopy

- Arrays.Copyof
