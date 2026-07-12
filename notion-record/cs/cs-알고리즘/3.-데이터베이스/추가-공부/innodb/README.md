# InnoDB

[트랜잭션 격리 수준](https://tecoble.techcourse.co.kr/post/2022-11-07-mysql-isolation/)

[MySQL :: MySQL 8.0 Reference Manual :: 15.7.1 InnoDB Locking](https://dev.mysql.com/doc/refman/8.0/en/innodb-locking.html)

# MVCC

Multi Version Concurrency Control로, 격리 시점에 따라 상이하나 Repeatable Read 기준으로 본다면 특정 시점의 snapshot을 바탕으로 Consistent Non-Locking Read를 할 수 있게 한다.

최초 SELECT문이 실행됐을 시의 snapshot이 생성, 다른 트랜잭션에 의해 해당 데이터가 변경되어도 undo 영역에 있는 내용을 읽는다. 하여 다른 트랜잭션의 commit이 일어났더라도, 기존 값을 읽어내는 것.

(innodb 기준으로)

다른 트랜잭션에 의해 변경될 가능성이 있기 때문에 두가지 Locking-Read를 제공하고 있다.

1. Shared Lock  

읽은 Row에 설정할 경우, 다른 트랜잭션이 해당 Row를 읽는 것은 가능하나 수정하는 것은 불가

2. Exclusive Lock

Row에 걸려있을 경우, 다른 트랜잭션에서 읽기, 수정 모두 불가

# SELECT

InnoDB는 SELECT에 대해서는 별도로 잠금 처리를 하지 않음.

# UPDATE, DELETE

참조하는 인덱스의 모든 레코드에 잠금을 건다.

변경하고자 하는 레코드에 잠금을 거는 비관적 락 방식을 사용하고 있음

# 비관적 락 vs 낙관적 락

시선의 차이 : 동시성 문제가 발생할 것이다 vs 발생하지 않을 것이다

비관적 락 : 이미 한 트랜잭션이 락을 선점한 경우 다른 트랜잭션은 반드시 대기

낙관적 락 :
