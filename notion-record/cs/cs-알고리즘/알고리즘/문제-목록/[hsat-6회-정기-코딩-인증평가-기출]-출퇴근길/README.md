# [HSAT 6회 정기 코딩 인증평가 기출] 출퇴근길

태그: 역방향 간선 그래프, 정리 필요

역방향 간선 그래프에서 t가 어떤 정점에 도달할 수 있다는 것은, 해당 그래프에서 t에 도달하기 위한 경로가 존재한다는 의미입니다. 이는 정방향 간선 그래프에서 그 정점이 t에 도달할 수 있음을 의미합니다.

간선 그래프에서 정방향 간선은 한 정점에서 다른 정점으로 이동하는 방향을 가지는 간선을 말합니다. 따라서 정방향 간선 그래프에서 어떤 정점에서 다른 정점으로 가는 경로를 탐색할 때, 정방향 간선을 따라 이동합니다.

하지만 역방향 간선 그래프에서는 간선의 방향이 반대로 설정되어 있습니다. 즉, 정방향 간선 그래프의 방향과는 반대로 간선이 연결되어 있습니다. 따라서 역방향 간선 그래프에서는 어떤 정점에서 다른 정점으로 가는 경로를 탐색할 때, 역방향 간선을 따라 이동합니다.

역방향 간선 그래프에서 t가 어떤 정점에 도달할 수 있다는 것은, 역방향 간선을 따라 t로 가는 경로가 존재한다는 것을 의미합니다. 이는 역방향 간선 그래프에서 해당 정점이 t에 도달할 수 있다는 것과 동일합니다.

따라서, 역방향 간선 그래프에서 t가 어떤 정점에 도달할 수 있다는 것은, 정방향 간선 그래프에서 그 정점이 t에 도달할 수 있다는 의미입니다. 이는 역방향 간선 그래프와 정방향 간선 그래프 사이의 관계를 나타내며, 그래프 이론과 관련된 다양한 문제를 해결하는 데 유용하게 사용됩니다.

```java
package Baekjoon;

import java.io.*;
import java.util.*;

public class Main{
    static int n, m;
    static int s, t;
    static List<List<Integer>> graph;
    static List<List<Integer>> reverseGraph;
    public static void main(String[] args) throws Exception{
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        n = Integer.parseInt(st.nextToken());
        m = Integer.parseInt(st.nextToken());
        graph = new ArrayList<>();
        reverseGraph = new ArrayList<>();
        for(int i=0; i<=n; i++){
            graph.add(new ArrayList<>());
            reverseGraph.add(new ArrayList<>());
        }

        for(int i=0; i<m; i++){
            st = new StringTokenizer(br.readLine());
            int u = Integer.parseInt(st.nextToken());
            int v = Integer.parseInt(st.nextToken());
            graph.get(u).add(v);
            reverseGraph.get(v).add(u);
        }

        st = new StringTokenizer(br.readLine());
        s = Integer.parseInt(st.nextToken());
        t = Integer.parseInt(st.nextToken());

        Set<Integer> s1 = new HashSet<>();
        Set<Integer> s2 = new HashSet<>();
        //s에서 도달할 수 있는 중간 정점들
        dfs(s, t, graph, s1, new boolean[n+1]);
        //역방향 간선에 대해서 t에서 도달할 수 있는 정점 -> s->t에서 도달가능한 정점
        //dfs(t, -1, reverseGraph, s2, new boolean[n+1]);

        //s1.retainAll(s2); //교집합

        Set<Integer> s3 = new HashSet<>();
        Set<Integer> s4 = new HashSet<>();
        //t에서 도달할 수 있는 정점들
        dfs(t, s, graph, s3, new boolean[n+1]);
        //역방향 간선에 대해서 s에서 도달할 수 있는 정점들
        //dfs(s, -1, reverseGraph, s4, new boolean[n+1]);

        //s3.retainAll(s4);

        s1.retainAll(s3);

        int answer = s1.size();

        if(s1.contains(s)) answer--;
        if(s1.contains(t)) answer--;

        System.out.println(answer);
    }

    public static void dfs(int node, int stop, List<List<Integer>> graph, Set<Integer> set, boolean[] visited){
        if(stop!=-1 && node==stop){
            return;
        }

        for(int i=0; i<graph.get(node).size(); i++){
            int next = graph.get(node).get(i);

            if(visited[next]) continue;

            visited[node] = true;
            set.add(next);
            dfs(next, stop, graph, set, visited);
        }

        return;

    }
}
```
