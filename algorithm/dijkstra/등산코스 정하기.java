import java.util.*;

class Solution {
    private List<Node>[] adj;
    private Set<Integer> sSet = new HashSet<>();
    private Set<Integer> gSet = new HashSet<>();

    public int[] solution(int n, int[][] paths, int[] gates, int[] summits) {
        init(n, paths, gates, summits);
        return dijkstra(n, summits);
    }

    private int[] dijkstra(int n, int[] summits) {
        PriorityQueue<Node> pq = new PriorityQueue<>();
        int[] dist = new int[n + 1];
        Arrays.fill(dist, Integer.MAX_VALUE);
        for (int g : gSet) {
            pq.add(new Node(g, 0));
            dist[g] = 0;
        }

        while (!pq.isEmpty()) {
            Node cur = pq.poll();

            if (cur.cost > dist[cur.to]) continue;

            for (Node next : adj[cur.to]) {
                // 현재 경로값과 다음 경로값의 최댓값 비교
                int nc = Math.max(next.cost, cur.cost);
                // 다음 경로에 저장되어 있는 값이 nc보다 크다면
                if (dist[next.to] > nc) {
                    dist[next.to] = nc;
                    pq.add(new Node(next.to, nc));
                }
            }
        }

        Arrays.sort(summits);
        int position = 0;
        int intensity = Integer.MAX_VALUE;
        for (int s : summits) {
            if (intensity > dist[s]) {
                intensity = dist[s];
                position = s;
            }
        }

        return new int[]{position, intensity};
    }

    private void init(int n, int[][] paths, int[] gates, int[] summits) {
        adj = new ArrayList[n + 1];
        for (int i = 1; i <= n; i++) {
            adj[i] = new ArrayList<>();
        }

        for (int g : gates) {
            gSet.add(g);
        }
        for (int s : summits) {
            sSet.add(s);
        }

        for (int[] p : paths) {
            int from = p[0];
            int to = p[1];
            int cost = p[2];

            // 출발점에서 나가거나, 도착점이 산봉우리인 경우
            if (gSet.contains(from) || sSet.contains(to)) {
                adj[from].add(new Node(to, cost));
            }

            // 도착지가 출발점이거나, 산봉우리에서 나가는 경우도착지가 출발점이거나, 산봉우리에서 나가는 경우
            else if (gSet.contains(to) || sSet.contains(from)) {
                adj[to].add(new Node(from, cost));
            }

            else {
                adj[from].add(new Node(to, cost));
                adj[to].add(new Node(from, cost));
            }
        }

    }
}

class Node implements Comparable<Node> {
    int to;
    int cost;

    public Node(int to, int cost) {
        this.to = to;
        this.cost = cost;
    }

    public int compareTo(Node o) {
        if (this.cost == o.cost) {
            return Integer.compare(this.to, o.to);
        }
        return Integer.compare(this.cost, o.cost);
    }
}