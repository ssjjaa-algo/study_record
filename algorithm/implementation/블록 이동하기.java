import java.util.*;

class Solution {
    int n;
    boolean[][][] visited;
    int[] rDir = {-1, 0, 1, 0};
    int[] cDir = {0, 1, 0, -1};
    int[][] gboard;
    Queue<Node> q = new ArrayDeque<>();

    public int solution(int[][] board) {
        n = board.length;
        gboard = board;
        visited = new boolean[n][n][2];

        return bfs();
    }

    public int bfs() {
        q.add (new Node(0, 0, 0, 1, 0)); // 수평 = 0, 수직 = 1
        visited[0][0][0] = true;
        visited[0][1][0] = true;
        int cnt = 0;

        while(!q.isEmpty()) {
            int size = q.size();

            for (int i = 0; i < size; i++) {
                Node cur = q.poll();
                if ((cur.r1 == n - 1 && cur.c1 == n - 1) || (cur.r2 == n - 1 && cur.c2 == n - 1)) {
                    return cnt;
                }

                for (int j = 0; j < 4; j++) {
                    int nr1 = cur.r1 + rDir[j];
                    int nc1 = cur.c1 + cDir[j];
                    int nr2 = cur.r2 + rDir[j];
                    int nc2 = cur.c2 + cDir[j];
                    int dir = cur.dir;

                    if (isPossible(nr1, nc1, nr2, nc2, dir)) {
                        addAndVisited(nr1, nc1, nr2, nc2, dir);
                    }
                }

                if (cur.dir == 0) {
                    if (isPossible(cur.r1 - 1, cur.c1, cur.r1 - 1, cur.c2, 1)) {
                        addAndVisited(cur.r1 - 1, cur.c1, cur.r1, cur.c1, 1);
                        addAndVisited(cur.r2 - 1, cur.c2, cur.r2, cur.c2, 1);
                    }

                    if (isPossible(cur.r1 + 1, cur.c1, cur.r1 + 1, cur.c2, 1)) {
                        addAndVisited(cur.r1, cur.c1, cur.r1 + 1, cur.c1, 1);
                        addAndVisited(cur.r2, cur.c2, cur.r2 + 1, cur.c2, 1);
                    }

                }

                else {
                    if (isPossible(cur.r1, cur.c1 - 1, cur.r2, cur.c1 - 1, 0)) {
                        addAndVisited(cur.r1, cur.c1 - 1, cur.r1, cur.c1, 0);
                        addAndVisited(cur.r2, cur.c1 - 1, cur.r2, cur.c2, 0);
                    }

                    if (isPossible(cur.r1, cur.c1 + 1, cur.r2, cur.c1 + 1, 0)) {
                        addAndVisited(cur.r1, cur.c1 + 1, cur.r1, cur.c1, 0);
                        addAndVisited(cur.r2, cur.c1 + 1, cur.r2, cur.c2, 0);
                    }
                }

            }

            cnt++;
        }

        return -1;
    }
    private boolean isPossible(int r1, int c1, int r2, int c2, int dir) {
        return !isInvalid(r1, c1, r2, c2) &&
                boardCheck(r1, c1, r2, c2) &&
                !visitedCheck(r1, c1, r2, c2, dir);
    }

    private void addAndVisited(int r1, int c1, int r2, int c2, int dir) {
        q.add(new Node(r1, c1, r2, c2, dir));
        visited[r1][c1][dir] = true;
        visited[r2][c2][dir] = true;
    }

    private boolean boardCheck(int r1, int c1, int r2, int c2) {
        return gboard[r1][c1] == 0 && gboard[r2][c2] == 0;
    }

    private boolean visitedCheck(int r1, int c1, int r2, int c2, int dir) {
        return visited[r1][c1][dir] && visited[r2][c2][dir];
    }

    private boolean isInvalid(int r1, int c1, int r2, int c2) {
        return r1 < 0 || r1 >= n || c1 < 0 || c1 >= n ||
                r2 < 0 || r2 >= n || c2 < 0 || c2 >= n;
    }
}

class Node {
    int r1, c1, r2, c2;
    int dir;

    public Node(int r1, int c1, int r2, int c2, int dir) {
        this.r1 = r1;
        this.c1 = c1;
        this.r2 = r2;
        this.c2 = c2;
        this.dir = dir;
    }

}