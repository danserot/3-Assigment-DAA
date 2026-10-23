package app.benchmark;

import app.graph.Graph;

import java.util.Random;

public final class GraphGenerator {

    public Graph undirected(int n, int m) {
        checkParameters(n, m);

        if (m < n - 1) {
            throw new IllegalArgumentException(
                    "Connected graph requires at least n - 1 edges"
            );
        }

        Random random = new Random(42);
        Graph graph = new Graph(n, false);

        int[] permutation = shuffled(n, random);

        for (int i = 1; i < n; i++) {
            int from = permutation[random.nextInt(i)];
            int to = permutation[i];

            graph.addEdge(
                    from,
                    to,
                    1 + random.nextInt(1_000)
            );
        }

        while (graph.edgeCount() < m) {
            int from = random.nextInt(n);
            int to;

            do {
                to = random.nextInt(n);
            } while (to == from);

            graph.addEdge(
                    from,
                    to,
                    1 + random.nextInt(1_000)
            );
        }

        return graph;
    }

    public Graph directed(int n, int m) {
        checkParameters(n, m);

        Random random = new Random(42);
        Graph graph = new Graph(n, true);

        while (graph.edgeCount() < m) {
            int from = random.nextInt(n);
            int to;

            do {
                to = random.nextInt(n);
            } while (to == from);

            graph.addEdge(
                    from,
                    to,
                    1 + random.nextInt(1_000)
            );
        }

        return graph;
    }

    public Graph dag(int n, int m) {
        checkParameters(n, m);

        Random random = new Random(42);
        Graph graph = new Graph(n, true);

        int[] rank = shuffled(n, random);

        while (graph.edgeCount() < m) {
            int from = random.nextInt(n);
            int to;

            do {
                to = random.nextInt(n);
            } while (to == from);

            if (rank[from] > rank[to]) {
                int temporary = from;
                from = to;
                to = temporary;
            }

            graph.addEdge(
                    from,
                    to,
                    1 + random.nextInt(1_000)
            );
        }

        return graph;
    }

    private int[] shuffled(int n, Random random) {
        int[] values = new int[n];

        for (int i = 0; i < n; i++) {
            values[i] = i;
        }

        for (int i = n - 1; i > 0; i--) {
            int other = random.nextInt(i + 1);

            int temporary = values[i];
            values[i] = values[other];
            values[other] = temporary;
        }

        return values;
    }

    private void checkParameters(int n, int m) {
        if (n < 1 || m < 0) {
            throw new IllegalArgumentException(
                    "Expected n >= 1 and m >= 0"
            );
        }

        if (n == 1 && m > 0) {
            throw new IllegalArgumentException(
                    "Cannot generate non-loop edges for n = 1"
            );
        }
    }
}