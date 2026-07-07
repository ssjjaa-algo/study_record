import java.util.*;

class Solution {
    public String[] solution(String[] s) {
        String[] answer = new String[s.length];
        int idx = 0;

        for (String str : s) {
            int len = str.length();
            int oneCnt = 0;
            StringBuilder sb = new StringBuilder();
            StringBuilder rest = new StringBuilder();

            for (int i = 0; i < len; i++) {
                char c = str.charAt(i);
                if (sb.length() >= 2 && c == '0') {
                    if (sb.charAt(sb.length() - 1) == '1' &&
                            sb.charAt(sb.length() - 2) == '1') {
                        sb.delete(sb.length() - 2, sb.length());
                        rest.append("110");
                    } else {
                        sb.append(c);
                    }
                } else {
                    sb.append(c);
                }
            }

            if (rest.length() > 1) {
                int lastIndex = sb.lastIndexOf("0");
                sb.insert(lastIndex + 1, rest.toString());
            }
            answer[idx++] = sb.toString();

        }


        return answer;
    }
}