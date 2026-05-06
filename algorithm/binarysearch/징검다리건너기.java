class Solution {
    public int solution(int[] stones, int k) {
        int left = 0;
        int right = 200_000_000;
        int ans = 1;

        while (left <= right) {
            int mid = (left + right) >> 1;
            if (check(mid, stones, k)) {
                ans = mid;
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        return ans;
    }

    public boolean check(int mid, int[] stones, int k) {
        int ans = 0;
        for (int i = 0; i < stones.length; i++) {
            if (stones[i] - mid <= 0) {
                ans++;
            } else {
                ans = 0;
            }
            if (ans >= k) return false;
        }
        return true;

    }
}