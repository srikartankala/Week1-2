import java.util.*;
import java.util.concurrent.*;

class RealTimeAnalyticsDashboard {

    private Map<String, Integer> pageViews = new ConcurrentHashMap<>();
    private Map<String, Set<String>> uniqueVisitors = new ConcurrentHashMap<>();
    private Map<String, Integer> trafficSources = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public RealTimeAnalyticsDashboard() {
        scheduler.scheduleAtFixedRate(this::printDashboard, 0, 5, TimeUnit.SECONDS);
    }

    public void processEvent(Map<String, String> event) {
        String url = event.get("url");
        String userId = event.get("userId");
        String source = event.get("source");

        pageViews.merge(url, 1, Integer::sum);
        uniqueVisitors.computeIfAbsent(url, k -> ConcurrentHashMap.newKeySet()).add(userId);
        trafficSources.merge(source, 1, Integer::sum);
    }

    private void printDashboard() {
        System.out.println("Top Pages:");
        PriorityQueue<Map.Entry<String, Integer>> topPages = new PriorityQueue<>(
                Map.Entry.comparingByValue()
        );
        for (Map.Entry<String, Integer> entry : pageViews.entrySet()) {
            topPages.offer(entry);
            if (topPages.size() > 10) topPages.poll();
        }
        List<Map.Entry<String, Integer>> result = new ArrayList<>();
        while (!topPages.isEmpty()) result.add(topPages.poll());
        Collections.reverse(result);
        int rank = 1;
        for (Map.Entry<String, Integer> entry : result) {
            String url = entry.getKey();
            int views = entry.getValue();
            int unique = uniqueVisitors.getOrDefault(url, Collections.emptySet()).size();
            System.out.println(rank + ". " + url + " - " + views + " views (" + unique + " unique)");
            rank++;
        }
        System.out.println("Traffic Sources: " + trafficSources);
        System.out.println("----------");
    }

    public static void main(String[] args) throws InterruptedException {
        RealTimeAnalyticsDashboard dashboard = new RealTimeAnalyticsDashboard();

        dashboard.processEvent(Map.of("url", "/article/breaking-news", "userId", "user_123", "source", "google"));
        dashboard.processEvent(Map.of("url", "/article/breaking-news", "userId", "user_456", "source", "facebook"));
        dashboard.processEvent(Map.of("url", "/sports/championship", "userId", "user_789", "source", "direct"));

        Thread.sleep(6000);
        dashboard.processEvent(Map.of("url", "/article/breaking-news", "userId", "user_789", "source", "twitter"));

        Thread.sleep(6000);
        dashboard.scheduler.shutdown();
    }
}