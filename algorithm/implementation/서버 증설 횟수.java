import java.util.*;

class Solution {
    public int solution(int[] players, int m, int k) {
        PriorityQueue<int[]> pq = new PriorityQueue<>((o1, o2) -> {
            return Integer.compare(o1[0], o2[0]);
        });

        int ans = 0;
        int server = 0;
        for (int i = 0; i < 24; i++) {
            // 1. 현재 시간에 증설 종료해야하는 서버 정리
            if (!pq.isEmpty() && pq.peek()[0] == i) {
                int[] cur = pq.poll();
                server -= cur[1];
            }
            // 2. 플레이어 수 확인
            int player = players[i];

            // 3. 최소 서버 개수 확인
            int need = player / m;

            // 4. 최소 서버 개수 만큼 없다면 증설 필요
            if (need > server) {
                int up = need - server;
                ans += up;
                pq.add(new int[]{i + k, up});
                server += up;
            }
        }

        return ans;
    }
}