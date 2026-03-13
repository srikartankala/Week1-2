import java.util.*;

class VideoData {
    String videoId;
    String content;

    public VideoData(String videoId, String content) {
        this.videoId = videoId;
        this.content = content;
    }
}

public class MultiLevelCacheSystem {

    private final int L1_SIZE = 10000;

    LinkedHashMap<String, VideoData> L1Cache =
            new LinkedHashMap<>(L1_SIZE, 0.75f, true) {
                protected boolean removeEldestEntry(Map.Entry eldest) {
                    return size() > L1_SIZE;
                }
            };

    private final int L2_SIZE = 100000;

    HashMap<String, VideoData> L2Cache = new HashMap<>();

    HashMap<String, VideoData> database = new HashMap<>();

    HashMap<String, Integer> accessCount = new HashMap<>();

    int L1Hits = 0;
    int L2Hits = 0;
    int L3Hits = 0;

    public MultiLevelCacheSystem() {
        for (int i = 1; i <= 200000; i++) {
            database.put("video_" + i,
                    new VideoData("video_" + i, "Video Content " + i));
        }
    }

    public VideoData getVideo(String videoId) {

        if (L1Cache.containsKey(videoId)) {
            L1Hits++;
            System.out.println("L1 Cache HIT (0.5ms)");
            return L1Cache.get(videoId);
        }

        System.out.println("L1 Cache MISS");

        if (L2Cache.containsKey(videoId)) {
            L2Hits++;
            System.out.println("L2 Cache HIT (5ms)");

            VideoData video = L2Cache.get(videoId);

            promoteToL1(video);

            return video;
        }

        System.out.println("L2 Cache MISS");

        if (database.containsKey(videoId)) {
            L3Hits++;
            System.out.println("L3 Database HIT (150ms)");

            VideoData video = database.get(videoId);

            addToL2(video);

            return video;
        }

        return null;
    }

    private void promoteToL1(VideoData video) {

        accessCount.put(video.videoId,
                accessCount.getOrDefault(video.videoId, 0) + 1);

        if (accessCount.get(video.videoId) > 2) {
            L1Cache.put(video.videoId, video);
            System.out.println("Promoted to L1 Cache");
        }
    }

    private void addToL2(VideoData video) {

        if (L2Cache.size() >= L2_SIZE) {
            String firstKey = L2Cache.keySet().iterator().next();
            L2Cache.remove(firstKey);
        }

        L2Cache.put(video.videoId, video);
    }

    public void invalidate(String videoId) {

        L1Cache.remove(videoId);
        L2Cache.remove(videoId);
        database.remove(videoId);

        System.out.println("Cache invalidated for " + videoId);
    }

    public void getStatistics() {

        int total = L1Hits + L2Hits + L3Hits;

        System.out.println("Cache Statistics");

        System.out.println("L1 Hits: " + L1Hits);
        System.out.println("L2 Hits: " + L2Hits);
        System.out.println("L3 Hits: " + L3Hits);

        double hitRate = (double)(L1Hits + L2Hits) / total * 100;

        System.out.println("Overall Hit Rate: " + hitRate + "%");
    }

    public static void main(String[] args) {

        MultiLevelCacheSystem cache = new MultiLevelCacheSystem();

        cache.getVideo("video_123");

        System.out.println();

        cache.getVideo("video_123");

        System.out.println();

        cache.getVideo("video_999");

        cache.getStatistics();
    }
}