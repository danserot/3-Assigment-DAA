package app.algorithms;

import app.graph.Edge;
import graph.Graph;
import app.metrics.Metrics;
import app.result.SccResult;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;

public final class Kosaraju {

    private static final class Frame {
        private final int vertex;
        private final List<Edge> outgoing;
        private int nextEdge;

        private Frame(Graph graph, int vertex) {
            this.vertex = vertex;
            this.outgoing = graph.neighbors(vertex);
        }
    }

    public SccResult run(Graph graph, Metrics metrics) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException(
                    "Kosaraju requires a directed graph"
            );
        }

        int n = graph.vertexCount();
        boolean[] visited = new boolean[n];
        int[] finishOrder = new int[n];
        int finished = 0;

        ArrayDeque<Frame> stack = new ArrayDeque<>();

        for (int start = 0; start < n; start++) {
            if (visited[start]) {
                continue;
            }

            visited[start] = true;
            stack.push(new Frame(graph, start));
            metrics.recordDepth(stack.size());

            while (!stack.isEmpty()) {
                Frame frame = stack.peek();

                if (frame.nextEdge == frame.outgoing.size()) {
                    finishOrder[finished++] = frame.vertex;
                    stack.pop();
                    continue;
                }

                Edge edge = frame.outgoing.get(
                        frame.nextEdge++
                );

                metrics.scanEdge();

                if (!visited[edge.to()]) {
                    visited[edge.to()] = true;

                    stack.push(
                            new Frame(graph, edge.to())
                    );

                    metrics.recordDepth(stack.size());
                }
            }
        }

        // Построение обратного графа не входит
        // в счетчик ребер двух DFS-проходов.
        Graph reversed = graph.reversed();

        int[] componentOf = new int[n];
        Arrays.fill(componentOf, -1);

        int componentCount = 0;
        ArrayDeque<Integer> vertices = new ArrayDeque<>();

        for (int i = finished - 1; i >= 0; i--) {
            int start = finishOrder[i];

            if (componentOf[start] != -1) {
                continue;
            }

            componentOf[start] = componentCount;
            vertices.push(start);
            metrics.recordDepth(vertices.size());

            while (!vertices.isEmpty()) {
                int vertex = vertices.pop();

                for (Edge edge : reversed.neighbors(vertex)) {
                    metrics.scanEdge();

                    if (componentOf[edge.to()] == -1) {
                        componentOf[edge.to()] =
                                componentCount;

                        vertices.push(edge.to());
                        metrics.recordDepth(vertices.size());
                    }
                }
            }

            componentCount++;
        }

        return new SccResult(
                componentOf,
                componentCount
        );
    }
}