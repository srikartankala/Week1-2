import java.util.*;

class PlagiarismDetector {

    private final int N = 5; // n-gram size
    private Map<String, Set<String>> nGramIndex = new HashMap<>();

    public void analyzeDocument(String docId, String content) {
        List<String> words = tokenize(content);
        int totalNGrams = 0;

        for (int i = 0; i <= words.size() - N; i++) {
            String ngram = buildNGram(words, i, N);
            nGramIndex.computeIfAbsent(ngram, k -> new HashSet<>()).add(docId);
            totalNGrams++;
        }

        System.out.println("Extracted " + totalNGrams + " n-grams");
    }

    public Map<String, Double> findSimilarDocuments(String docId, String content) {
        List<String> words = tokenize(content);
        Map<String, Integer> matchCount = new HashMap<>();
        int totalNGrams = 0;

        for (int i = 0; i <= words.size() - N; i++) {
            String ngram = buildNGram(words, i, N);
            Set<String> docs = nGramIndex.getOrDefault(ngram, Collections.emptySet());
            for (String otherDocId : docs) {
                if (!otherDocId.equals(docId)) {
                    matchCount.put(otherDocId, matchCount.getOrDefault(otherDocId, 0) + 1);
                }
            }
            totalNGrams++;
        }

        Map<String, Double> similarity = new HashMap<>();
        for (Map.Entry<String, Integer> entry : matchCount.entrySet()) {
            double simPercent = (entry.getValue() * 100.0) / totalNGrams;
            similarity.put(entry.getKey(), simPercent);
        }
        return similarity;
    }

    private List<String> tokenize(String content) {
        return Arrays.asList(content.toLowerCase().split("\\W+"));
    }

    private String buildNGram(List<String> words, int start, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < start + n; i++) {
            sb.append(words.get(i));
            if (i < start + n - 1) sb.append(" ");
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        PlagiarismDetector detector = new PlagiarismDetector();

        String docA = "this is a sample document to test plagiarism detection system";
        String docB = "this is another sample document to detect plagiarism issues";
        String docC = "completely different text that has no match";

        detector.analyzeDocument("essay_089.txt", docA);
        detector.analyzeDocument("essay_092.txt", docB);
        detector.analyzeDocument("essay_123.txt", docC);

        Map<String, Double> similarities = detector.findSimilarDocuments("essay_123.txt", docC);
        for (var e : similarities.entrySet()) {
            System.out.printf("Found %.0f matching n-grams with \"%s\"\nSimilarity: %.1f%%\n",
                    e.getValue() * docC.split("\\W+").length, e.getKey(), e.getValue());
        }
    }
}