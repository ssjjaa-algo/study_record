import java.util.*;
/*
    1. 앞에 붙을 0의 개수를 파악한다
    2. 트리를 재귀로 돌려서 확인한다.
        - 최초 진입 시에는 root가 0인 것을 파악한다
        - 이후 left, right를 재귀를 돌린다
            - 재귀를 돌릴 때는 부모 트리가 0인 상태가 있을 수 있다
                - 부모 트리가 0이면 자식이 모두 0이어야 한다
*/

class Solution {
    public int[] solution(long[] numbers) {
        List<Integer> list = new ArrayList<>();
        for (long num : numbers) {
            String binary = makeBinary(num);
            list.add(isBinaryTree(binary));
        }

        return list.stream().mapToInt(Integer::intValue).toArray();
    }

    private int isBinaryTree(String binary) {
        int root = binary.length() / 2;
        if (binary.charAt(root) == '0') return 0;

        String left = binary.substring(0, root);
        String right = binary.substring(root + 1);

        return possibleTree(left) && possibleTree(right) ? 1 : 0;
    }

    private boolean possibleTree(String binary) {
        int len = binary.length();
        if (len == 0) return true;

        int root = binary.length() / 2;
        String left = binary.substring(0, root);
        String right = binary.substring(root + 1);

        if (binary.charAt(root) == '0') {
            return zeroTree(left) && zeroTree(right);
        }

        return possibleTree(left) && possibleTree(right);
    }

    private boolean zeroTree(String binary) {
        int len = binary.length();
        if (len == 0) return true;

        int root = binary.length() / 2;
        String left = binary.substring(0, root);
        String right = binary.substring(root + 1);

        if (binary.charAt(root) != '0') return false;

        return zeroTree(left) && zeroTree(right);


    }

    private String makeBinary(long num) {
        String binary = Long.toString(num, 2);
        int len = binary.length();
        int cnt = 1;
        int level = 1;
        while (cnt < len) {
            level *= 2;
            cnt += level;
        }
        int diff = cnt - len;

        return "0".repeat(diff) + binary;
    }
}