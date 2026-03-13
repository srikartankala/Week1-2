import java.util.*;

class TrieNode {
    Map<Character, TrieNode> children = new HashMap<>();
    Map<String, Integer> freqMap = new HashMap<>();
    boolean isEnd;
}

class AutocompleteSystem {

    private TrieNode root = new TrieNode();
    private Map<String, Integer> globalFreq = new HashMap<>();

    public void addQuery(String query, int freq) {
        globalFreq.put(query, globalFreq.getOrDefault(query, 0) + freq);

        TrieNode node = root;
        for (char c : query.toCharArray()) {
            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);
            node.freqMap.put(query, globalFreq.get(query));
        }
        node.isEnd = true;
    }

    public void updateFrequency(String query) {
        int freq = globalFreq.getOrDefault(query, 0) + 1;
        globalFreq.put(query, freq);

        TrieNode node = root;
        for (char c : query.toCharArray()) {
            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);
            node.freqMap.put(query, freq);
        }
        node.isEnd = true;
    }

    public List<String> search(String prefix) {
        TrieNode node = root;

        for (char c : prefix.toCharArray()) {
            if (!node.children.containsKey(c)) return new ArrayList<>();
            node = node.children.get(c);
        }

        PriorityQueue<Map.Entry<String, Integer>> pq =
                new PriorityQueue<>(Comparator.comparingInt(Map.Entry::getValue));

        for (Map.Entry<String, Integer> entry : node.freqMap.entrySet()) {
            pq.offer(entry);
            if (pq.size() > 10) pq.poll();
        }

        List<String> result = new ArrayList<>();
        while (!pq.isEmpty()) {
            result.add(pq.poll().getKey() + " (" + globalFreq.get(pq.peek() != null ? pq.peek().getKey() : entryKey(entryFallback(node))) + " searches)");
        }

        Collections.reverse(result);
        return result;
    }

    private String entryKey(String s) {
        return s;
    }

    private String entryFallback(TrieNode node) {
        for (String k : node.freqMap.keySet()) return k;
        return "";
    }
}

public class AutocompleteSystemDemo {
    public static void main(String[] args) {

        AutocompleteSystem system = new AutocompleteSystem();

        system.addQuery("java tutorial", 1234567);
        system.addQuery("javascript", 987654);
        system.addQuery("java download", 456789);
        system.addQuery("java 21 features", 1);

        System.out.println("Suggestions for 'jav':");

        List<String> suggestions = system.search("jav");
        int rank = 1;

        for (String s : suggestions) {
            System.out.println(rank + ". " + s);
            rank++;
        }

        system.updateFrequency("java 21 features");
        system.updateFrequency("java 21 features");
        system.updateFrequency("java 21 features");

        System.out.println("\nAfter updates:");
        suggestions = system.search("java");

        rank = 1;
        for (String s : suggestions) {
            System.out.println(rank + ". " + s);
            rank++;
        }
    }
}