# sql 자격검정 실전문제

# 실기 2

- 주문상세 unique scan → 1,000건 → unique
- 주문 range scan → 10,000건 →
- 고객 → 1000 → 1000 → **`full`**

### 가정

- 주문상세 → 주문 → 고객
- order by 1 → to_char(a.주문일시 ~)
    - a.주문일시
        - 밖에서 to_char
- rn between 101 and 200
    - inline view 안에서도 써져야 한다.

```sql
(inline view)

select to_char(a.주문일시 ~),
			 나머지
from (select a.*, rownum as rn
			from
			(select /*+ leading(a b c) use_nl(b) use_nl(c) */
						 a.주문일시,
						 a.주문번호,
						 b.주문수량,
						 c.고객번호
			from 주문 a, 고객상세 b, 고객 c
			where 1=1 
						and   b.주문번호 = a.주문번호
						and   b.주문상품코드 = 'A01'
						and   a.주문일시 ~
						and   c.고객번호 = a.주문고객번호
			order by 1) a
			where rownum <= 200)
where rn between 101 and 200;

			 

```
