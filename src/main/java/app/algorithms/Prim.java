package app.algorithms;

import app.graph.Edge;
import app.graph.Graph;
import app.metrics.Metrics;
import app.result.MstResult;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public final class Prim {

    public MstResult run(Graph graph, Metrics metrics) {
        if (graph.isDirected()) {
            throw new IllegalArgumentException(
                    "Prim requires an undirected graph"
            );
        }

        boolean[] visited =
                new boolean[graph.vertexCount()];

        PriorityQueue<Edge> queue =
                new PriorityQueue<>((left, right) -> {
                    int comparison = metrics.compareWeights(
                            left.weight(),
                            right.weight()
                    );

                    if (comparison != 0) {
                        return comparison;
                    }

                    comparison = Integer.compare(
                            left.from(),
                            right.from()
                    );

                    return comparison != 0
                            ? comparison
                            : Integer.compare(
                            left.to(),
                            right.to()
                    );
                });

        List<Edge> selected = new ArrayList<>();
        long totalWeight = 0;
        int components = 0;

        // Первая компонента начинается с 0.
        // Остальные запускаются от непосещенных вершин.
        for (int start = 0;
             start < graph.vertexCount();
             start++) {

            if (visited[start]) {
                continue;
            }

            components++;

            addFrontier(
                    graph,
                    start,
                    visited,
                    queue,
                    metrics
            );

            while (!queue.isEmpty()) {
                Edge edge = queue.remove();

                if (visited[edge.to()]) {
                    continue;
                }

                selected.add(edge);
                totalWeight += edge.weight();

                addFrontier(
                        graph,
                        edge.to(),
                        visited,
                        queue,
                        metrics
                );
            }
        }

        return new MstResult(
                selected,
                totalWeight,
                components
        );
    }

    private void addFrontier(
            Graph graph,
            int vertex,
            boolean[] visited,
            PriorityQueue<Edge> queue,
            Metrics metrics
    ) {
        visited[vertex] = true;

        for (Edge edge : graph.neighbors(vertex)) {
            metrics.scanEdge();

            if (!visited[edge.to()]) {
                queue.add(edge);
            }
        }
    }
}