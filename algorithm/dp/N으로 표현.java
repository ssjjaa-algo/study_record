import java.util.*;

class Solution {
    private List<Set<Integer>> list = new ArrayList<>();

    public int solution(int N, int number) {
        if (number == N) return 1;

        for (int i = 0; i <= 8; i++) {
            list.add(new HashSet<>());
        }
        list.get(1).add(N);

        for (int i = 2; i <= 8; i++) {
            Set<Integer> set = list.get(i);

            for (int j = 1; j < i; j++) {
                Set<Integer> a = list.get(j);
                Set<Integer> b = list.get(i - j);

                for (int num1 : a) {
                    for (int num2 : b) {
                        set.add(num1 + num2);
                        set.add(num1 - num2);
                        set.add(num1 * num2);
                        if (num1 != 0 && num2 != 0) {
                            set.add(num1 / num2);
                        }
                    }
                }
            }
            set.add(Integer.parseInt(String.valueOf(N).repeat(i)));
            if (set.contains(number)) return i;
        }

        return -1;
    }
}