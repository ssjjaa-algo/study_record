import java.util.*;

class Solution {
    private Set<Integer> set = new HashSet<>();
    private Set<Integer> coinSet = new HashSet<>();
    private int n;
    private int sum; // 카드에 적힌 수의 합 = (n + 1)

    public int solution(int coin, int[] cards) {
        n = cards.length;
        sum = n + 1;

        return calculate(cards, coin);
    }

    private int calculate(int[] cards, int coin) {
        for (int i = 0; i < n / 3; i++) {
            set.add(cards[i]);
        }
        int round = 0;
        int start = (n / 3);

        while(true) {
            round++;

            if (start >= n) break;
            for (int i = start; i < start + 2; i++) {
                coinSet.add(cards[i]);
            }
            start += 2;
            boolean flag = false;
            // 1. 동전을 쓰지 않고 확인할 수 있는지
            for (int num : set) {
                if (set.contains(sum - num)) {
                    set.remove(num);
                    set.remove(sum - num);
                    flag = true;
                    break;
                }
            }

            if (!flag && coin > 0) {
                for (int num : coinSet) {
                    if (set.contains(sum - num)) {
                        set.remove(sum - num);
                        coinSet.remove(num);
                        coin--;
                        flag = true;
                        break;
                    }
                }
            }

            if (!flag && coin >= 2) {
                for (int num : coinSet) {
                    if (coinSet.contains(sum - num)) {
                        coinSet.remove(num);
                        coinSet.remove(sum - num);
                        coin -= 2;
                        flag = true;
                        break;
                    }
                }
            }

            if (!flag) break;
        }

        return round;
    }
}
