import java.util.*;

class Solution {
    private List<Integer> adj[];
    private boolean[] visited;

    public int[] solution(int n, int[][] roads, int[] sources, int destination) {
        init(n, roads);
        int[] answer = new int[sources.length];
        int idx = 0;

        for (int source : sources) {
            answer[idx++] = bfs(source, destination);
            Arrays.fill(visited, false);
        }

        return answer;
    }

    public int bfs(int s, int e) {
        visited[s] = true;
        int cnt = 0;
        Queue<Integer> q = new ArrayDeque<>();
        q.add(s);

        while(!q.isEmpty()) {
            int size = q.size();

            for (int i = 0; i < size; i++) {
                int cur = q.poll();
                if (cur == e) return cnt;

                for (int next : adj[cur]) {
                    if (visited[next]) continue;
                    visited[next] = true;
                    q.add(next);
                }
            }
            cnt++;
        }

        return -1;
    }

    private void init(int n, int[][] roads) {
        visited = new boolean[n + 1];
        adj = new ArrayList[n + 1];
        for (int i = 1; i <= n; i++) {
            adj[i] = new ArrayList<>();
        }

        for (int[] r : roads) {
            adj[r[0]].add(r[1]);
            adj[r[1]].add(r[0]);
        }
    }
}