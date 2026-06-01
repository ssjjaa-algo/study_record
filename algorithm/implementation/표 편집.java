import java.util.*;

class Solution {
    private int[] prev;
    private int[] next;
    Stack<Integer> stack = new Stack<>();
    private int gIdx;

    public String solution(int n, int k, String[] cmd) {
        init(n, k);

        for (String c : cmd) {
            if (c.equals("C")) {
                delete(n);
            } else if (c.equals("Z")) {
                rollback();
            } else {
                String[] s = c.split(" ");
                int num = Integer.parseInt(s[1]);
                if (s[0].equals("U")) {
                    up(num, n);
                } else {
                    down(num, n);
                }
            }
        }

        char[] arr = new char[n];
        Arrays.fill(arr, 'O');
        for (int num : stack) {
            arr[num] = 'X';
        }

        return String.valueOf(arr);
    }

    private void delete(int n) {
        if (prev[gIdx] != -1) {
            next[prev[gIdx]] = next[gIdx];
        }
        if (next[gIdx] != -1) {
            prev[next[gIdx]] = prev[gIdx];
        }
        stack.add(gIdx);
        if (next[gIdx] == -1) {
            gIdx = prev[gIdx];
        } else {
            gIdx = next[gIdx];
        }
    }

    private void rollback() {
        int cur = stack.pop();
        if (prev[cur] != -1) {
            next[prev[cur]] = cur;
        }
        if (next[cur] != -1) {
            prev[next[cur]] = cur;
        }
    }

    private void up(int num, int n) {
        while (prev[gIdx] != -1 && num-- > 0) {
            gIdx = prev[gIdx];
        }
    }

    private void down(int num, int n) {
        while (next[gIdx] != -1 && num-- > 0) {
            gIdx = next[gIdx];
        }
    }

    private void init(int n, int k) {
        gIdx = k;
        prev = new int[n];
        next = new int[n];
        for (int i = 0; i < n; i++) {
            prev[i] = -1 + i;
            next[i] = i + 1;
        }
        next[n - 1] = -1;
    }
}