import java.util.*;

class UsernameChecker {
    private HashMap<String, Integer> userMap = new HashMap<>();
    private HashMap<String, Integer> attemptCount = new HashMap<>();

    public UsernameChecker() {
        userMap.put("virat_kohli", 1);
        userMap.put("admin", 2);
        userMap.put("user123", 3);
    }

    public boolean checkAvailability(String username) {
        attemptCount.put(username, attemptCount.getOrDefault(username, 0) + 1);
        return !userMap.containsKey(username);
    }

    public List<String> suggestAlternatives(String username) {
        List<String> suggestions = new ArrayList<>();
        if (!userMap.containsKey(username)) {
            suggestions.add(username);
            return suggestions;
        }
        for (int i = 1; i <= 5; i++) {
            String candidate = username + i;
            if (!userMap.containsKey(candidate)) {
                suggestions.add(candidate);
            }
        }
        String modified = username.replace("_", ".");
        if (!userMap.containsKey(modified)) {
            suggestions.add(modified);
        }
        return suggestions;
    }

    public String getMostAttempted() {
        String result = null;
        int max = 0;
        for (Map.Entry<String, Integer> entry : attemptCount.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                result = entry.getKey();
            }
        }
        return result;
    }

    public static void main(String[] args) {
        UsernameChecker checker = new UsernameChecker();
        System.out.println(checker.checkAvailability("virat_kohli"));
        System.out.println(checker.checkAvailability("kajal_agarwal"));
        System.out.println(checker.suggestAlternatives("virat_kohli"));
        System.out.println(checker.getMostAttempted());
    }
}