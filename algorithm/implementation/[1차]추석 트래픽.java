import java.util.*;

class Solution {
    public int solution(String[] lines) {
        List<Time> list = new ArrayList<>();
        
        int idx = 0;
        for (String line : lines) {
            String[] l = line.split(" ");
            String[] times = l[1].split(":");
            int end = 0;
            end += (int)(Double.parseDouble(times[0]) * 3600 * 1000);
            end += (int)(Double.parseDouble(times[1]) * 60 * 1000);
            end += (int)(Double.parseDouble(times[2]) * 1000);
            
            int work = (int)(Double.parseDouble(l[2].substring(0, l[2].length() - 1)) * 1000);
            int start = end - work + 1;
            list.add(new Time(start, end));
        }
        
        return calculate(list);
    }
    
    private int calculate(List<Time> list) {
        int res = 0;
        
        for (Time t1 : list) {
            int a = 0;
            int b = 0;
            for (Time t2 : list) {
                if (check(t1.s, t2)) a++;
                if (check(t1.e, t2)) b++;
            }
            res = Math.max(res, Math.max(a, b));
        }
        
        return res;
    }
    
    private boolean check(int time, Time t2) {
        return (
            (time <= t2.s && time + 999 >= t2.s) ||
            (time <= t2.e && time + 999 >= t2.e) ||
            (time > t2.s && time + 999 < t2.e)
        );
    }
}

class Time {
    int s;
    int e;
    
    public Time(int s, int e) {
        this.s = s;
        this.e = e;
    }
    
}