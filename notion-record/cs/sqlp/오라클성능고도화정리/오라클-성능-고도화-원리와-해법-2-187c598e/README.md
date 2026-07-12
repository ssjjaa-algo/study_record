# 오라클 성능 고도화 원리와 해법 2

텍스트: 쿼리변환

## 쿼리 변환의 종류

- 서브쿼리 Unnesting
- 뷰 Merging
- 조건절 Pushing
- 조건절 이행
- 공통 표현식 제거
- Outer 조인을 Inner 조인으로 변환
- 실체화 뷰 쿼리로 재작성
- Star 변환
- Outer 조인 뷰에 대한 조인 조건 Pushdown
- OR-expansion
- etc

<aside>
💡

서브쿼리 Unnesting은 중첩된 서브쿼리(WHERE절)과 관련이 있고,
뷰 Merging은 인라인 뷰와 관련있다.

</aside>

# 서브쿼리 처리 방식

### 필터 방식

- 중첩된 서브쿼리는 메인쿼리와 ‘부모와 자식’이라는 종속적이고 계층적인 관계가 존재
- 메인쿼리와 서브쿼리는 각각 최적화 방식을 수행함
- 실행계획에 **`FILTER`** 오퍼레이션 등장
- 처리 과정은 필터 방식
    - **메인 쿼리에서 읽히는 레코드마다 서브쿼리를 반복적으로 수행**

### Unnesting

- 동일한 결과를 보장하는 **`조인문으로 변환`**하고 나서 최적화
- 변환한다는 것 → **`다양한`** 조인문으로 변환할 수 있다
    - nl, hash, nl_sj, …. 등.
- 따라서 다양한 최적화가 가능

### 서브쿼리의 또다른 최적화 방식

- where 조건절에 사용된 서브쿼리
    - 메인쿼리와 상관관계에 있지 않으면서
    - 단일 로우를 리턴하는 형태의 서브쿼리를 처리할 때
- Fetch가 아닌 Execute 시점에 먼저 수행되며 그 결과를 상수로 전달한다.

### 서브쿼리 Unnesting의 이점

- 다양한 액세스 경로와 조인 메서드를 평가할 수 있다.
- unnest : 서브쿼리를 Unnesting 하여 조인 방식으로 최적화 유도

```sql
select *
from emp
where deptno in (
  select/*+ unnest */ deptno
  from dept
)

# 변환 순서

select *
from (select deptno from dept) a, emp b
where b.deptno = a.deptno

# 뷰 Merging 과정을 거쳐 아래의 형태가 된다

select emp.* from dept, emp
where emp.deptno = dept.deptno
```

- **이게 결국 Unnesting 한다는 것이.. 최종적으로 뷰 Merging까지 간다는 의미인지는 아직 모르겠다.**
- **(20250117) 일단 맞는거 같음.**
- no_unnest : 서브쿼리를 그대로 둔 상태에서 필터 방식으로 최적화 유도

```sql
select *
from emp
where deptno in (
  select/*+ no_unnest */from dept
)

# 메인 쿼리에서 읽히는 레코드마다 값을 넘기면서 서브쿼리를 반복수행? 이게 정확히 뭔말이지.
```

### Unnesting된 쿼리의 조인 순서 조정

- 어느 집합이든 드라이빙 집합으로 선택할 수 있다. (10g 이후)
- 쿼리 블록마다 이름을 지정할 수 있는 **qb_name**은 참고해서 공부해보자.

```sql
select/*+ leading(dept@qb1) */ *
from emp
where deptno in (
	select/*+ unnest qb_name(qb1) */ deptno
    from dept
)
```

## 서브쿼리가 M쪽 집합이거나 Nonunique 인덱스일 때

- 메인 쿼리에 서브쿼리가 종속적인 관계이므로 일반 조인문으로 변환해도 결과집합에 오류가 없어야함
    - 오류가 없어야 변환을 할 수 있다는 것

### 서브쿼리가 M쪽 집합인 경우

```sql
select * from dept
where deptno in (select deptno from emp)
```

- dept 테이블 기준으로 결과집합은 1 집합이 되어야함
- 아래의 일반 조인문으로 변환한다면 emp 단위의 결과집합이 만들어짐 ( M * 1 = M), 오류

```sql
select * 
from (select deptno from emp) a, deptno b
where b.deptno = a.deptno
```

### 테이블 간의 관계를 알 수 없을 때

```sql
select * from emp
where  deptno in (select deptno from dept)
```

- M쪽 집합을 드라이빙해 1쪽 집합을 서브쿼리로 필터링 → 변환해도 오류 없음.
- 만약 이 상황에서 dept 테이블에 **`PK/UNIQUE 인덱스가 없다면`** 두 테이블간의 관계를 알 수가 없음
    - 따라서 조인문으로 변환을 시도하지 않음.

### 서브쿼리 쪽 집합을 1쪽 집합으로 만들기 위해 옵티마이저는 두 가지 방식 중 하나 선택

- 1쪽 집합임을 확신할 수 없다면, 우선 1쪽 집합으로 만들기 위한 **`SORT UNIQUE`** 오퍼레이션 수행
    
    ```sql
    select *
    from emp
    where deptno in (
    	select deptno
        from dept
    )
    
    # deptno 컬럼에 NonUunique 인덱스가 있다고 하면 아래와 같이 쿼리 변환
    # sort unique operation을 수행하고자 order by가 추가되었다
    
    select b.*
    from (
    	select/*+ no_merge */ distinct deptno
        from dept
        order by deptno
    ) a, emp b
    where b.deptno = a.deptno
    ```
    
- 메인쿼리 쪽 테이블이 드라이빙 된다면 **`세미 조인`** 방식으로 실행

### **세미조인**

- Outer 테이블의 한 로우가 Inner 테이블의 한 로우와 조인에 성공하는 순간 진행을 멈추고 Outer 테이블의 다음 로우 처리
    - **`서브쿼리에서 중복을 제거`**
    - nl, hash, sort merge .. 모두 가능하다는 것. (세미 방식으로)

```java
for (int i = 0; ; i++)
	for (int j = 0; ; j++)
    	if (i == j) break; // 조건을 만족하면 벗어난다.
```

### 필터 오퍼레이션과 세미 조인의 캐싱 효과

- 필터 방식 사용 시 서브쿼리의 결과를 캐싱하여 사용할 수 있음
- 세미 조인도 캐싱 효과를 가짐

### Anti 조인

- not exists, not in의 Unnesting -> Anti 조인 방식으로 처리
- 힌트 : nl_aj, merge_aj, hash_aj

### Sort Unique 오퍼레이션 수행

### 10g 버전 - 집계 서브쿼리 제거

- 집계 합수를 포함하는 서브쿼리를 Unnesting하고, 이를 분석 함수로 대체하는 쿼리 변환.

```sql
select d.deptno, d.dname
from emp e, dept d
where d.deptno = e.deptno
and e.sal >= (select avg(sal) from emp where deptno = d.deptno)

# 1차 변환

select d.deptno, d.dname
from (select deptno, avg(sal) avg_sal from emp group by deptno) x, emp e, dept d
where d.deptno = e.deptno
and e.deptno = x.deptno
and e.sal >= x.avg_sal

# 한번 더 쿼리 변환을 시도해 인라인 뷰를 Merging하거나 그대로 둔 채 최적화할 수 있다.
#/*+ unnest merge */와/*+ unnest no_merge */select deptno, dname, sal
from (
  select d.deptno, d.dname, e.sal
  (case when e.sal >= avg(sal) over (partition by d.deptno)
   then e.row_id end) max_sal_rowid
  from emp e, dept d
  where d.deptno = e.deptno
)
where max_sal_rowid is not null
```

### Pushing 서브쿼리

- Unnesting되지 않은 서브쿼리는 항상 필터 방식으로 처리되며, 대개 마지막 단계에서 처리
- 서브쿼리를 실행계획 상 가능한 앞 단계에서 처리하도록 하는 힌트
- **push_subq 힌트는 항상 no_unnest힌트와 같이 기술**하는 것이 올바른 사용법

## 뷰 Merging

```sql
select *
from
    (select * from emp where job = 'abcd') a,
    (select * from dept where loc = 'seoul') b
where a.deptno = b.deptno

# 변환

select *
from emp a, dept b
where a.deptno = b.deptno
and a.job = 'abcd'
and b.loc = 'seoul'
```

- 변환 전 쿼리의 뷰 쿼리 블록은 액세스 쿼리 블록(뷰를 참조하는 쿼리 블록)과의 **merge** 과정을 거쳐 변환
- 힌트는 merge / no_merge

### 단순 뷰(Simple View) Merging

- 조건절과 조인문만을 포함하는 단순 뷰(Simple View)는 no_merge 힌트를 사용하지 않는 한 언제든 merging 일어남

### 복합 뷰(Complex View) Merging

- group by, distinct 연산을 포함하는 복합 뷰는 파라미터 설정 또는 힌트 사용에 의해서 merging
- 집합 연산자(union, union all, intersect, minus), connect by, rownum, analytic function, select-list에 집계합수 (group by 없이 전체를 집계하는 경우)에는 뷰 머징이 불가하다.

```sql
select d.dname, avg_sal_dept
from dept d
   , (select deptno, avg(sal) avg_sal_dept
      from emp
      group by deptno) e
where d.deptno = e.deptno
and d.loc = 'CHICAGO'

# 뷰 머징 (뷰 쿼리 블록을 액세스 쿼리 블록과 Merging)

select d.dname, avg(sal)
from dept d, emp e
where d.deptno = e.deptno
and d.loc = 'CHICAGO'
group by d.rowid, d.dname

# d.loc 조건을 필터링한 후 group by하여 비용을 줄임, 물론 항상 옳지는 않음
```

## 비용기반 쿼리 변환

- _optimizer_cost_based_transformation
- 더 나은 실행계획을 위함이므로 관련된 기능 off는 부적절
- 쿼리 레벨에서 파라미터 변경 가능 -> opt_param 힌트

```sql
# 미사용 힌트
select/* + opt_param('_optimizer_push_pred_cost_based', 'false') */ from ...
```

## 조건절 Pushing

- 뷰 액세스 쿼리 최적화할 때 뷰 Merging에 실패하는 경우, 2차적으로 조건 Pushing을 시도함
- **뷰를 참조하는 쿼리 블록의 조건절을 뷰 쿼리 블록 안으로 Pushing**
- **조건절이 가능한 빨리 처리되도록 하여 뷰 안에서의 처리 일량을 최소화 및 리턴 결과 건수를 줄인다.**
- 뷰 안에 rownum을 사용하면 merge가 되지 않고, push도 되지 않는다. 조건절 pushing이 작용하면, 기존에 없던 조건절이 생겨 같은 로우가 다른 값을 부여받을 수 있다.

### 종류

- **조건절 Pushdown**

- 쿼리 블록 밖에 있는 조건들을 쿼리 블록 안쪽으로

- **조건절 Pullup**

- 쿼리 블록 안에 있는 조건들을 쿼리 블록 밖으로, 그것을 다시 다른 쿼리 블록에 Pushdown 하는데 사용

(Predicate Move Around)

```sql
select * from
    (select depno, avg(sal) from emp where dpetno = 10 group by detpno) e1,
    (select deptno, min(sal), max(sal) from emp group by deptno) e2
where e1.deptno = e2.deptno

# 조건절 Pullup
# 쿼리 변환

(select depno, avg(sal) from emp where dpetno = 10 group by detpno) e1,
(select deptno, min(sal), max(sal) from emp where deptno = 10 group by deptno) e2
```

- **조인 조건 Pushdown**

- NL 조인 중에 드라이빙 테이블에서 읽은 값을 건건이 Inner쪽 뷰 쿼리 블록 안으로 밀어넣음

- 조인을 수행하는 중에 드라이빙 집합에서 얻은 값을 뷰 쿼리 블록 안에 실시간으로 Pushing

- 실행 계획에 VIEW PUSHED PREDICATE

- 힌트 : push_pred / no_push_pred

## 조건절 이행

- A = B 이고 B = C 이면 C = A 조건을 쿼리에서 그대로 이용함
- **조인조건은 상수와 변수 조건처럼 전이되지 않으므로, 최적의 조인 순서를 결정하고 순서에 따라 조인문을 기술해주어야 함**

## 조인 제거

- 조인문을 제외한 1쪽 테이블을 참고하고 있는 것이 없다면 M쪽 테이블만 읽도록 쿼리를 변환 = 조인 제거

```sql
# 조인 조건식을 제외하면 1쪽 집합인 dept에 대한 참조가 전혀 없다

select e.empno, e.ename
from dept d, emp e
where d.deptno = e.deptno

# 기능
alter session set "_optimizer_join_elimination_enabled" = true;
```

- PK와 FK 제약이 반드시 설정돼 있어야함

## Outer 조인을 Inner 조인으로 변환

- **일부 조건절에 Outer 기호(+)를 빠뜨리면** Inner 조인할 때와 같은 결과가 나온다.
- Outer NL, Outer 소트 머지 조인 시 **드라이빙 테이블은 항상 Outer 기호가 붙지 않은 쪽**으로 고정
- 이런 형식의 쿼리 변환을 시행하는 이유는 조인 순서의 자유로운 결정 때문.
- ANSI Outer 조인문에서 where 절에 기술한 Inner 쪽 필터 조건이 의미있게 사용되는 경우는 is null 조건뿐
- 제대로 된 Outer 조인 결과문을 얻으려면 on절에 기술
- Outer쪽 필터조건은 on절이든 where이든 성능 차이가 없음

## 실체화 뷰 쿼리

- 실체화 뷰(Materialized View, 이하 MV)는 물리적으로 실제 데이터를 가짐
- 실시간 또는 일정 주기로 데이터를 복제하는 데 사용하던 Snapshot 기술을 DW 분야에 적응시킨 것
- Refresh 옵션을 이용해 오라클이 집계 테이블을 자동 관리하도록 할 수 있다.
- 옵티마이저에 의한 Query Rewrite가 지원된다.
- **자동으로 쿼리가 재작성된다?**
- **사용자는 기준 테이블을 쿼리하지만 옵티마이저가 알아서 MV를 액세스하도록 쿼리를 변환해준다?**
- **이거 왜 씀?????????**

## 기타 쿼리 변환

```sql
select count(e.empno)
from emp e, dept d
where d.deptno = e.deptno
and sal <= 2900

# 쿼리 변환

select count(e.empno)
from emp e, dept d
where d.deptno = e.deptno
and sal <= 2900
and e.deptno is not null
and d.deptno is not null
```

- 조인 컬럼에 IS NOT NULL 추가

- **컬럼이 null인 데이터는 조인할 필요 없도록 하여 액세스 줄일 수 있다.**

- 옵티마이저는 null값 비중이 5%가 넘을 때만 쿼리변환을 해주기 때문에 써주는 습관은 좋다
