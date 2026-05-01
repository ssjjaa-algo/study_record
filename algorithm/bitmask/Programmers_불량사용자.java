import java.util.*;

/*
    1. user_id String을 int형의 idx로 변환
    2. banned_id의 Set<Integer> 집합에 대상을 집어넣는다
    3. 최대 8개의 크기를 가질 수 있다 -> bit mask 사용
*/

class 불량사용자 {
    private Set<Integer> answerSet = new HashSet<>();
    private Map<String, Integer> wordMap = new HashMap<>();
    private Map<String, Set<Integer>> map = new HashMap<>();

    public int solution(String[] user_id, String[] banned_id) {
        int idx = 0;
        for (String b : banned_id) {
            for (String u : user_id) {
                if (check(b, u)) {
                    if (!wordMap.containsKey(u)) {
                        wordMap.put(u, idx++);
                    }
                    Set<Integer> set = map.getOrDefault(b, new HashSet<>());
                    if (set.isEmpty()) {
                        map.put(b, set);
                    }
                    set.add(wordMap.get(u));
                }
            }
        }

        dfs(0, 0, banned_id, banned_id.length);

        return answerSet.size();
    }

    public boolean check(String b, String u) {
        int len = b.length();
        if (len != u.length()) {
            return false;
        }

        for (int i = 0; i < len; i++) {
            if (b.charAt(i) == '*') {
                continue;
            }
            if (b.charAt(i) != u.charAt(i)) {
                return false;
            }
        }

        return true;
    }

    public void dfs(int cnt, int mask, String[] banned_id, int limit) {
        if (cnt == limit) {
            answerSet.add(mask);
            return;
        }

        for (Integer num : map.get(banned_id[cnt])) {
            if ((mask & (1 << num)) != 0) {
                continue;
            }
            dfs(cnt + 1, mask | (1 << num), banned_id, limit);
        }
    }
}