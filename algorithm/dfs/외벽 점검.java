import java.util.*;

class Solution {
    private int[] gdist;
    private int[] gweak;
    private int[] selected;
    private int weakLen;

    public int solution(int n, int[] weak, int[] dist) {
        init(n, weak, dist);

        for (int i = 0; i < dist.length; i++) {

            if (dfs(i + 1, 0, 0)) {
                return i + 1;
            }
        }

        return -1;
    }

    private boolean check(int idx, int people) {
        int start = idx;

        for (int i = 0; i < people; i++) {
            int end = gweak[start] + selected[i];

            while (start < idx + weakLen && gweak[start] <= end) {
                start++;
            }

            if (start == idx + weakLen) return true;
        }

        return false;
    }

    private boolean dfs(int people, int cnt, int bit) {
        if (people == cnt) {

            for (int i = 0; i < weakLen; i++) {
                if (check(i, people)) return true;
            }

            return false;
        }

        for (int i = 0; i < gdist.length; i++) {
            if ((bit & (1 << i)) == 0) {
                selected[cnt] = gdist[i];

                if (dfs(people, cnt + 1, bit | (1 << i))) {
                    return true;
                }
            }
        }

        return false;
    }


    private void init(int n, int[] weak, int[] dist) {
        int dlen = dist.length;
        int wlen = weak.length;
        selected = new int[dlen];
        weakLen = wlen;
        Arrays.sort(dist);

        gdist = new int[dlen];

        for (int i = 0; i < dlen; i++) {
            gdist[i] = dist[dlen - 1 - i];
        }

        gweak = new int[wlen * 2 - 1];

        for (int i = 0; i < wlen; i++) {
            gweak[i] = weak[i];
        }

        for (int i = wlen; i < wlen * 2 - 1; i++) {
            gweak[i] = weak[i - wlen] + n;
        }
    }
}