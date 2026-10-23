package app.metrics;

public final class Metrics {
    private long findCalls;
    private long successfulUnions;
    private long edgeScans;
    private long comparisons;
    private int maxDepth;

    public void findCalled() {
        findCalls++;
    }

    public void unionSucceeded() {
        successfulUnions++;
    }

    public void scanEdge() {
        edgeScans++;
    }

    public int compareWeights(int left, int right) {
        comparisons++;
        return Integer.compare(left, right);
    }

    public void recordDepth(int depth) {
        maxDepth = Math.max(maxDepth, depth);
    }

    public long findCalls() {
        return findCalls;
    }

    public long successfulUnions() {
        return successfulUnions;
    }

    public long dsuOps() {
        return findCalls + successfulUnions;
    }

    public long edgeScans() {
        return edgeScans;
    }

    public long comparisons() {
        return comparisons;
    }

    public int maxDepth() {
        return maxDepth;
    }
}