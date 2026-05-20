import java.util.*;

class Solution {
    private int[] dp = new int[100001];

    public int solution(int n, int[] money) {
        Arrays.sort(money);
        dp[0] = 1;
        for (int m : money) {
            for (int i = m; i <= n; i++) {
                dp[i] += (dp[i - m]);
                dp[i] %= 1_000_000_007;
            }
        }

        return dp[n];
    }
}