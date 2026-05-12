import java.util.*;

class Solution {
    public int solution(int n, int[][] costs) {
        List<List<Node>> graph = new ArrayList<>();
        for (int i = 0; i < n; i++) graph.add(new ArrayList<>());
        
        for (int[] c : costs) {
            graph.get(c[0]).add(new Node(c[1], c[2]));
            graph.get(c[1]).add(new Node(c[0], c[2]));
        }
        
        boolean[] visited = new boolean[n];
        PriorityQueue<Node> pq = new PriorityQueue<>((a,b) -> a.cost - b.cost);
        
        pq.offer(new Node(0, 0));
        int answer = 0;
        int count = 0;
        
        while (!pq.isEmpty()) {
            Node cur = pq.poll();
            if (visited[cur.to]) continue;
            
            visited[cur.to] = true;
            answer += cur.cost;
            count++;
            if (count == n) break;
            
            for (Node next : graph.get(cur.to)) {
                if (!visited[next.to]) pq.offer(next);
            }
        }
        
        return answer;
    }
    
    static class Node {
        int to, cost;
        public Node(int to, int cost) {
            this.to = to;
            this.cost = cost;
        }
    }
}
