import java.util.*;

class Solution {
    private List<Integer> adj[];
    private long[] values;
    private int[] degree;
    private boolean[] visited;

    public long solution(int[] a, int[][] edges) {
        init(a, edges);
        long sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i];
        }
        if (sum != 0) return -1;

        return bfs();
    }

    private long bfs() {
        long answer = 0;
        Queue<Integer> q = new ArrayDeque<>();

        for (int i = 0; i < degree.length; i++) {
            if (degree[i] == 1) {
                q.add(i);
            }
        }

        while (!q.isEmpty()) {
            int cur = q.poll();
            visited[cur] = true;

            for (int next : adj[cur]) {
                if (visited[next]) continue;
                values[next] += values[cur];
                answer += Math.abs(values[cur]);
                if (--degree[next] == 1) {
                    q.add(next);
                }
            }
        }

        return answer;
    }

    private void init(int[] a, int[][] edges) {
        int len = a.length;
        values = new long[len];
        visited = new boolean[len];
        degree = new int[len];
        for (int i = 0; i < len; i++) {
            values[i] = a[i];
        }
        adj = new ArrayList[len];
        for (int i = 0; i < len; i++) {
            adj[i] = new ArrayList<>();
        }

        int elen = edges.length;
        for (int i = 0; i < elen; i++) {
            adj[edges[i][0]].add(edges[i][1]);
            adj[edges[i][1]].add(edges[i][0]);
            degree[edges[i][0]]++;
            degree[edges[i][1]]++;
        }

    }

}