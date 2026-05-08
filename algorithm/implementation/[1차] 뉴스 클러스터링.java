import java.util.*;

class Solution {
    public int solution(String str1, String str2) {
        Map<String, Integer> str1Map = new HashMap<>();
        Map<String, Integer> str2Map = new HashMap<>();

        for (int i = 1; i < str1.length(); i++) {
            char c1 = str1.charAt(i - 1);
            char c2 = str1.charAt(i);
            if (Character.isLetter(c1) && Character.isLetter(c2)) {
                String s = ("" + c1 + c2).toLowerCase();
                str1Map.put(s, str1Map.getOrDefault(s, 0) + 1);
            }
        }

        for (int i = 1; i < str2.length(); i++) {
            char c1 = str2.charAt(i - 1);
            char c2 = str2.charAt(i);
            if (Character.isLetter(c1) && Character.isLetter(c2)) {
                String s = ("" + c1 + c2).toLowerCase();
                str2Map.put(s, str2Map.getOrDefault(s, 0) + 1);
            }
        }

        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(str1Map.keySet());
        allKeys.addAll(str2Map.keySet());

        int intersectionCount = 0;
        int unionCount = 0;

        for (String key : allKeys) {
            int count1 = str1Map.getOrDefault(key, 0);
            int count2 = str2Map.getOrDefault(key, 0);
            intersectionCount += Math.min(count1, count2);
            unionCount += (count1 + count2 - Math.min(count1, count2));
        }

        if (unionCount == 0) {
            return 65536;
        }

        double jaccard = (double) intersectionCount / unionCount;
        return (int)(jaccard * 65536);
    }
}