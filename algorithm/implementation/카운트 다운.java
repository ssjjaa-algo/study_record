import java.util.*;

class Solution {
    private int dartCnt = Integer.MAX_VALUE;
    private int sOrBCnt;

    public int[] solution(int target) {
        int temp = 0;

        while (target - 60 > 1000) {
            target -= 60;
            temp++;
        }

        dfs(target, 0, temp);

        return new int[]{dartCnt, sOrBCnt};
    }

    private void dfs (int target, int sCnt, int dCnt) {
        if (target == 0) {
            if (dCnt > dartCnt) return;

            else if (dCnt < dartCnt) {
                dartCnt = dCnt;
                sOrBCnt = sCnt;
            }
            else {
                if (sOrBCnt < sCnt) {
                    sOrBCnt = sCnt;
                }
            }

            return;
        }

        if (target >= 60) {
            dfs(target - 60, sCnt, dCnt + 1);
            dfs(target - 50, sCnt + 1, dCnt + 1);
        }
        // 50점 이상인 경우
        else if (target >= 50) {
            // 트리플로 끝낼 수 있는 경우라면 1번 만에 끝낸다.
            if (target % 3 == 0) {
                dfs(0, sCnt, dCnt + 1);
            }
            // 아니라면 어차피 2번 해야하는데 이런 경우 50점과 같이 넣어주는게 좋다.
            else {
                dfs(target - 50, sCnt + 1, dCnt + 1);
            }
        }

        else if (target > 40 && target < 50) {
            // 트리플로 끝낼 수 있는 경우라면 1번 만에 끝낸다.
            if (target % 3 == 0) {
                dfs(0, sCnt, dCnt + 1);
            }
            // 아니라면 어차피 2번 해야한다. 임의의 값을 넣어주고 싱글로 돌아가도록 한다.
            else {
                dfs (target - (target - 1), sCnt, dCnt + 1);
            }
        }

        else if (target > 20 && target <= 40) {
            // 트리플이나 더블로 끝낼 수 있는 경우라면 1번만에 끝낸다
            if (target % 3 == 0 || target % 2 == 0) {
                dfs(0, sCnt, dCnt + 1);
            }
            // 아니라면 어차피 2번 해야한다. 싱글 2번으로 돌아가도록 한다.
            else {
                dfs(target - (target - 1), sCnt + 1, dCnt + 1);
            }
        }
        else {
            dfs(0, sCnt + 1, dCnt + 1);
        }

        return;

    }
}
