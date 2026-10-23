package app.algorithms;

import app.metrics.Metrics;

import java.util.Objects;

public final class DisjointSetUnion {
    private int[] parent;
    private int[] size;
    private int components;

    private final Metrics metrics;

    public DisjointSetUnion(int n, Metrics metrics) {
        this.metrics = Objects.requireNonNull(metrics);
        makeSet(n);
    }

    public void makeSet(int n) {
        if (n < 0) {
            throw new IllegalArgumentException(
                    "Set count must be non-negative"
            );
        }

        parent = new int[n];
        size = new int[n];
        components = n;

        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }
    }

    public int find(int vertex) {
        checkVertex(vertex);
        metrics.findCalled();

        int root = vertex;

        while (root != parent[root]) {
            root = parent[root];
        }

        // Полное сжатие пути без рекурсии.
        while (vertex != root) {
            int next = parent[vertex];
            parent[vertex] = root;
            vertex = next;
        }

        return root;
    }

    public boolean union(int first, int second) {
        int rootFirst = find(first);
        int rootSecond = find(second);

        if (rootFirst == rootSecond) {
            return false;
        }

        if (size[rootFirst] < size[rootSecond]) {
            int temporary = rootFirst;
            rootFirst = rootSecond;
            rootSecond = temporary;
        }

        parent[rootSecond] = rootFirst;
        size[rootFirst] += size[rootSecond];

        components--;
        metrics.unionSucceeded();

        return true;
    }

    public int components() {
        return components;
    }

    private void checkVertex(int vertex) {
        if (vertex < 0 || vertex >= parent.length) {
            throw new IllegalArgumentException(
                    "Invalid element: " + vertex
            );
        }
    }
}