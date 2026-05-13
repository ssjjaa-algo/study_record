class Solution {
    public long solution(int n, int[] times) {
        long left = 1;
        long right = 1_000_000_000l * 100_000l;

        while (left <= right) {
            long mid = (left + right) >> 1;

            if (calculate(mid, n, times)) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }

        return left;
    }

    public boolean calculate(long time, int n, int[] times) {
        long sum = 0;
        for (int t : times) {
            sum += (time / t);
        }

        return sum >= n;
    }
}