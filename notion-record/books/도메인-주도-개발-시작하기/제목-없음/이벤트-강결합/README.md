# 이벤트 - 강결합

외부 시스템 호출 간 외부 시스템이 정상이 아닌 경우에 대한 처리, 익셉션이 발생했을 시 롤백 / 커밋 등에 대해 고민해야 하며, 외부 시스템의 응답이 과도하게 느린 경우에 대한 고민도 필수다.

이런 문제의 대부분의 원인이 컨텍스트 간 **강결합**으로 인해 발생한다. 이런 강결합을 없애기 위해, 이벤트를 사용하고 비동기를 고려한다.

# 이벤트

‘과거에 벌어진 어떤 것’, 예를 들어 사용자가 암호를 변경한 것을 ‘암호를 변경했음 이벤트’로 표현, 주문을 취소했다면 ‘주문을 취소했음 이벤트’가 발생한 것이다. 

- 책에서는 이벤트가 발생한다는 것이 상태가 변경된다는 것으로 표현하는데
- 오직 그 형태에 국한되어 있는지는 조금 더 알아봐야 할 듯

## 구성 요소

이벤트 생성 주체 → 이벤트 디스패처(이벤트 퍼블리셔) → 이벤트 핸들러(이벤트 구독자)

생성 주체는 도메인, 밸류, 도메인 서비스와 같은 도메인 객체를 의미한다. 상태가 바뀌면, 관련 이벤트를 발생

이벤트 핸들러는 이러한 이벤트에 반응하여, 기능을 수행하는 역할.

디스패처는 중간자, 디스패처 구현 방식에 따라 이벤트 생성과 처리를 동기나, 비동기로 한다거나 결정

## 예시

- 이벤트 종류 : 클래스 이름으로 이벤트 종류를 표현
- 이벤트 발생 시간
- 추가 데이터 : 주문번호, 신규 배송지 정보 등 이벤트와 관련된 정보

배송지를 변경할 때 발생하는 이벤트를 보자.

```java
 @Getter
 public class ShippingInfoChangedEvent {
	 
	 private String orderNumber;
	 private long timestamp;
	 private ShippingInfo newShippingInfo;
 }
```

- 클래스에 Changed로 과거 시제 표현
- 이벤트를 발생하는 주체는 Order, Order 애그리거트의 배송지 변경을 구현한 메서드에서 Event.raise()를 발생
    
    ```java
     public class Order {
     
    	 public void changeShippingInfo(ShippingInfo newShippingInfo) {
    		 
    		 // 유효성 검사 등
    		 Events.raise(new ShippingInfoChangedEvent(number, newShippingInfo));
    	 }
     }
    ```
    

- 이벤트 핸들러가 발생시킨 Event를 듣고 있어야 한다. 즉 EventLister의 역할을 하게 된다
    
    ```java
    public class ShippingInfoChangeHandler {
    	
    	@EventLister(ShippingInfoChangedEvent.class)
    	public void handle(ShippingInfoChangedEvent evt) {
    		shippingInfoSynchronizer.sync(
    			evt.getOrderNumber(),
    			evt.getNewShippingInfo());
    			
    		/*
    			이벤트가 필요한 데이터를 담고있지 않으면
    			핸들러가 레포지토리, 조회 API, DB 접근 등을 통해 필요한 데이터를 조회해야 한다.
    			과한 듯? 다 전달받게, 즉 이렇게 처리하지 않는게 우선은 좋아보인다.
    		*/	
    	}
    }
    ```
    

## 이벤트 용도

크게 두 가지 용도로 사용된다. 트리거와, 시스템 간의 데이터 동기화이다.

트리거(Trigger)는 도메인의 상태가 변경될 때, 다른 후처리가 필요하면 후처리를 실행하기 위한 트리거로 이벤트를 사용하는 것이고, 주문에서는 주문 취소 이벤트를 트리거로 사용한다.

즉, Order → EventDispatcher → OrderCancledEventHandler → RefundService.

두 번째는 시스템 간의 데이터 동기화로, 배송지를 변경하면 외부 배송 서비스에 바뀐 배송지 정보를 전송해야 하므로 이벤트를 발생시켜 알려주는 것.

## 장점

이런 이벤트를 사용함으로써 서로 다른 도메인 로직이 섞이는 것을 방지할 수 있다. 이벤트를 통해, 내가 직접 구현하는 것이 아닌 핸들러로 넘겨줌으로써 의존을 제거하는 것.
