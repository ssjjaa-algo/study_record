import java.util.*;

class Solution {
    private boolean[] choice; // dice 선택 index
    private int[][] gdice; // 전역 접근 dice
    private List<Integer> a = new ArrayList<>();
    private List<Integer> b = new ArrayList<>();
    private int max = 0;
    private int[] ans;
    
    public int[] solution(int[][] dice) {
        gdice = dice;
        choice = new boolean[dice.length];
        ans = new int[dice.length / 2];
        combination(0, 0, dice.length);
        
        return ans;
    }
    
    private void dice() {
        // 선택한 주사위로 만들 수 있는 모든 점수의 경우의 수를 만든다
        makeScores(0, 0, 0, true); // 선택받은 것은 true
        makeScores(0, 0, 0, false); // 아닌 것은 false
        Collections.sort(a);
        Collections.sort(b);
        
        int win = calculate();
        if (win > max) {
            max = win;
            int idx = 0;
            for (int i = 0; i < gdice.length; i++) {
                if (choice[i]) {
                    ans[idx++] = i + 1;
                }
            }
        }
        a.clear();
        b.clear();
    }
    
    private int calculate() {     
        int win = 0;
        int len = a.size();
        for (int i = 0; i < len; i++) {
            int cnt = binary(a.get(i));
            if (cnt == len) {
                win += (len - i) * a.size();
                break;
            } 
            else {
                win += cnt;
            }
        }
        
        return win;
    }
    
    private int binary(int num) {
        int left = 0;
        int right = b.size() - 1;
        
        while (left <= right) {
            int mid = (left + right) >> 1;
            int find = b.get(mid);
            
            if (num <= find) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }
        return left;
    }
    
    private void makeScores(int start, int cnt, int sum, boolean flag) {
        if (cnt == gdice.length / 2) {
            if (flag) {
                a.add(sum);
            } 
            else {
                b.add(sum);
            }
            return;
        }
        
        for (int i = start; i < gdice.length; i++) {
            if (choice[i] == flag) {
                for (int num : gdice[i]) {
                    makeScores(i + 1, cnt + 1, sum + num, flag);
                }
            }
        }
    }
    
    
    
    private void combination(int start, int cnt, int limit) {
        if (cnt == limit / 2) {
            dice();
            return;
        }
        
        for (int i = start; i < limit; i++) {
            choice[i] = true;
            combination(i + 1, cnt + 1, limit);
            choice[i] = false;
        }
    }
}
