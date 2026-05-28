import java.util.*;

class Solution {
    private List<Integer>[] adj;
    private int maxSheep = 1;
    private int[] gInfo;
    public int solution(int[] info, int[][] edges) {
        init(info, edges);
        dfs(1, 0, 0, 0, 0);

        return maxSheep;
    }

    private void dfs(int start, int visited, int sheepAndWolfBit, int sheep, int wolf) {
        if ((sheepAndWolfBit & (1 << start)) == 0) { // 아직 이 지역의 늑대나 양을 섭취하지 않았으면
            if (gInfo[start] == 1) {
                wolf++;
            } else {
                sheep++;
            }
            sheepAndWolfBit |= (1 << start); // 섭취 표시를 하고
            visited = (1 << start); // 방문 초기화 후 현재 위치만 방문 처리한다
            maxSheep = Math.max(maxSheep, sheep);
        }

        if (wolf >= sheep) return;

        for (int next : adj[start]) {
            if ((visited & (1 << next)) != 0) continue;
            dfs(next, visited | (1 << start), sheepAndWolfBit, sheep, wolf);
        }
    }

    private void init(int[] info, int[][] edges) {
        int len = info.length;
        gInfo = new int[len + 1];
        gInfo[0] = 0;
        for (int i = 0; i < len; i++) {
            gInfo[i + 1] = info[i];
        }
        adj = new ArrayList[len + 1];
        for (int i = 1; i <= len; i++) {
            adj[i] = new ArrayList<>();
        }

        for (int[] e : edges) {
            adj[e[0] + 1].add(e[1] + 1);
            adj[e[1] + 1].add(e[0] + 1);
        }

    }
}