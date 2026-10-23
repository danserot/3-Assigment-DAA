package app.algorithms;

import app.graph.Edge;
import graph.Graph;
import app.metrics.Metrics;
import app.result.TopologicalResult;

import java.util.ArrayDeque;

public final class Kahn {

    public TopologicalResult run(
            Graph graph,
            Metrics metrics
    ) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException(
                    "Kahn requires a directed graph"
            );
        }

        int n = graph.vertexCount();

        // Подготовка степеней учитывается во времени,
        // но не в edgeScans основного прохода.
        int[] inDegree = graph.inDegrees();

        ArrayDeque<Integer> queue = new ArrayDeque<>();

        for (int vertex = 0; vertex < n; vertex++) {
            if (inDegree[vertex] == 0) {
                queue.add(vertex);
            }
        }

        int[] order = new int[n];
        int count = 0;

        while (!queue.isEmpty()) {
            int vertex = queue.remove();
            order[count++] = vertex;

            for (Edge edge : graph.neighbors(vertex)) {
                metrics.scanEdge();

                inDegree[edge.to()]--;

                if (inDegree[edge.to()] == 0) {
                    queue.add(edge.to());
                }
            }
        }

        if (count != n) {
            // Не выдаем частичный результат за полный порядок.
            return new TopologicalResult(new int[0], true);
        }

        return new TopologicalResult(order, false);
    }
}