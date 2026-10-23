package app.algorithms;

import app.graph.Edge;
import app.graph.Graph;
import app.metrics.Metrics;
import app.result.MstResult;

import java.util.ArrayList;
import java.util.List;

public final class Kruskal {

    public MstResult run(Graph graph, Metrics metrics) {
        if (graph.isDirected()) {
            throw new IllegalArgumentException(
                    "Kruskal requires an undirected graph"
            );
        }

        List<Edge> sorted = new ArrayList<>(graph.edges());

        sorted.sort((left, right) -> {
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
                    : Integer.compare(left.to(), right.to());
        });

        DisjointSetUnion dsu = new DisjointSetUnion(
                graph.vertexCount(),
                metrics
        );

        List<Edge> selected = new ArrayList<>();
        long totalWeight = 0;

        for (Edge edge : sorted) {
            if (dsu.components() <= 1) {
                break;
            }

            metrics.scanEdge();

            if (dsu.union(edge.from(), edge.to())) {
                selected.add(edge);
                totalWeight += edge.weight();
            }
        }

        return new MstResult(
                selected,
                totalWeight,
                dsu.components()
        );
    }
}