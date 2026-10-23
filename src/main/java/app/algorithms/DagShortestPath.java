package app.algorithms;

import app.graph.Edge;
import app.graph.Graph;
import app.metrics.Metrics;
import app.result.ShortestPathResult;
import app.result.TopologicalResult;

import java.util.Arrays;

public final class DagShortestPath {

    public ShortestPathResult run(
            Graph graph,
            Metrics metrics
    ) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException(
                    "DAG shortest paths require a directed graph"
            );
        }

        TopologicalResult topological =
                new Kahn().run(graph, new Metrics());

        if (topological.hasCycle()) {
            throw new IllegalArgumentException(
                    "Shortest paths require an acyclic graph"
            );
        }

        int n = graph.vertexCount();

        long[] distance = new long[n];
        int[] parent = new int[n];

        Arrays.fill(distance, Long.MAX_VALUE);
        Arrays.fill(parent, -1);

        int[] inDegree = graph.inDegrees();

        for (int vertex = 0; vertex < n; vertex++) {
            if (inDegree[vertex] == 0) {
                distance[vertex] = 0;
            }
        }

        for (int vertex : topological.order()) {
            for (Edge edge : graph.neighbors(vertex)) {
                metrics.scanEdge();

                if (distance[vertex] == Long.MAX_VALUE) {
                    continue;
                }

                long candidate =
                        distance[vertex] + edge.weight();

                if (candidate < distance[edge.to()]) {
                    distance[edge.to()] = candidate;
                    parent[edge.to()] = vertex;
                }
            }
        }

        return new ShortestPathResult(
                distance,
                parent
        );
    }
}