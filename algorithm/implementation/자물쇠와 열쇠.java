import java.util.*;

class Solution {
    private List<int[]> keys = new ArrayList<>();
    private int lockCnt = 0;

    public boolean solution(int[][] key, int[][] lock) {
        int N = lock.length;
        lockCnt = 0;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (lock[i][j] == 0) lockCnt++;
            }
        }

        for (int rot = 0; rot < 4; rot++) {
            keys.clear();
            populateKeysFromKey(key);
            if (isLocked(key, lock)) return true;
            key = rotate(key);
        }

        return false;
    }

    private void populateKeysFromKey(int[][] key) {
        int len = key.length;
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < len; j++) {
                if (key[i][j] == 1) {
                    keys.add(new int[]{i, j});
                }
            }
        }
    }

    private boolean isLocked(int[][] key, int[][] lock) {
        int M = key.length;
        int N = lock.length;

        for (int offsetR = -M + 1; offsetR <= N - 1; offsetR++) {
            for (int offsetC = -M + 1; offsetC <= N - 1; offsetC++) {
                boolean collision = false;
                int filled = 0;

                for (int[] k : keys) {
                    int kr = k[0];
                    int kc = k[1];
                    int lr = kr + offsetR;
                    int lc = kc + offsetC;

                    if (isInvalid(lr, lc, N)) {
                        continue;
                    }

                    if (lock[lr][lc] == 1) {
                        collision = true;
                        break;
                    } else if (lock[lr][lc] == 0) {
                        filled++;
                    }
                }

                if (!collision && filled == lockCnt) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isInvalid(int nr, int nc, int len) {
        return nr < 0 || nr >= len || nc < 0 || nc >= len;
    }

    private int[][] rotate(int[][] key) {
        int len = key.length;
        int[][] temp = new int[len][len];

        for (int i = 0; i < len; i++) {
            for (int j = 0; j < len; j++) {
                temp[i][j] = key[len - 1 - j][i];
            }
        }

        return temp;
    }
}