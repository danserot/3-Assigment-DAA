package app.benchmark;

import app.algorithms.DisjointSetUnion;
import app.graph.Edge;
import graph.Graph;
import app.metrics.Metrics;
import app.result.MstResult;
import app.result.TopologicalResult;

public final class ResultValidator {

    public void validateForest(
            Graph graph,
            MstResult result
    ) {
        DisjointSetUnion checker = new DisjointSetUnion(
                graph.vertexCount(),
                new Metrics()
        );

        long totalWeight = 0;

        for (Edge edge : result.edges()) {
            require(
                    checker.union(edge.from(), edge.to()),
                    "MST result contains a cycle"
            );

            totalWeight += edge.weight();
        }

        require(
                totalWeight == result.totalWeight(),
                "Incorrect MST weight"
        );

        require(
                checker.components() == result.components(),
                "Incorrect forest component count"
        );

        require(
                result.edges().size()
                        == graph.vertexCount()
                        - result.components(),
                "Incorrect forest edge count"
        );

        // Каждое исходное ребро должно оставаться
        // внутри одной компоненты полученного леса.
        for (Edge edge : graph.edges()) {
            require(
                    checker.find(edge.from())
                            == checker.find(edge.to()),
                    "Forest does not span an input component"
            );
        }
    }

    public void validateTopologicalOrder(
            Graph graph,
            TopologicalResult result
    ) {
        require(
                !result.hasCycle(),
                "Generated DAG unexpectedly contains a cycle"
        );

        int n = graph.vertexCount();

        require(
                result.order().length == n,
                "Topological order has incorrect length"
        );

        int[] position = new int[n];
        boolean[] seen = new boolean[n];

        for (int index = 0; index < n; index++) {
            int vertex = result.order()[index];

            require(
                    vertex >= 0 && vertex < n,
                    "Invalid vertex in topological order"
            );

            require(
                    !seen[vertex],
                    "Duplicate vertex in topological order"
            );

            seen[vertex] = true;
            position[vertex] = index;
        }

        for (Edge edge : graph.edges()) {
            require(
                    position[edge.from()]
                            < position[edge.to()],
                    "Invalid topological order"
            );
        }
    }

    public static void require(
            boolean condition,
            String message
    ) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
