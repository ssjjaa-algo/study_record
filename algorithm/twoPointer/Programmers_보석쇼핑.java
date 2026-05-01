import java.util.*;

class Solution {
    private Set<String> set = new HashSet<>(); // 보석 가지수
    private Map<String, Integer> map = new HashMap<>();

    public int[] solution(String[] gems) {
        init(gems);
        int left = 0;
        int right = 0;
        int lAns = 0;
        int rAns = 0;
        int min = Integer.MAX_VALUE;

        for (String g : gems) {
            map.put(g, map.getOrDefault(g, 0) + 1);
            right++;

            if (getAllKindOfJewerly()) {
                while(true) {
                    int cnt = map.get(gems[left]) - 1;
                    if (cnt == 0) {
                        map.remove(left);
                        break;
                    }   else {
                        map.put(gems[left], cnt);
                    }
                    left++;
                }
                if (right - left < min) {
                    min = right - left;
                    lAns = left;
                    rAns = right;
                }
            }
        }

        return new int[]{lAns + 1, rAns};
    }

    public boolean getAllKindOfJewerly() {
        return map.size() == set.size();
    }

    public void init(String[] gems) {
        for (String g: gems) {
            set.add(g);
        }
    }

}