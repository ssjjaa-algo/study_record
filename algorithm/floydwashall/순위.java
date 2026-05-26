import java.util.*;

class Solution {
    private boolean[][] win;

    public int solution(int n, int[][] results) {
        win = new boolean[n + 1][n + 1];
        for (int[] r : results) {
            win[r[0]][r[1]] = true;
        }

        for (int k = 1; k <= n; k++) {
            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= n; j++) {
                    if (i == k || k == j || i == j) continue;
                    if (win[i][k] && win[k][j]) {
                        win[i][j] = true;
                    }
                }
            }
        }

        int answer = 0;
        for (int i = 1; i <= n; i++) {
            int cnt = 0;
            for (int j = 1; j <= n; j++) {
                if (i == j) continue;
                if (win[i][j] || win[j][i]) cnt++; // 내가 이기거나, 내가 졌거나 횟수
            }
            if (cnt == n - 1) answer++;
        }

        return answer;
    }
}