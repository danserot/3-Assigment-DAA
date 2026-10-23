package graph;

import app.graph.Edge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Graph {
    private final int vertexCount;
    private final boolean directed;

    private final List<Edge> edges = new ArrayList<>();
    private final List<Edge> edgeView =
            Collections.unmodifiableList(edges);

    private final List<List<Edge>> adjacency;
    private final List<List<Edge>> adjacencyViews;

    public Graph(int vertexCount, boolean directed) {
        if (vertexCount < 0) {
            throw new IllegalArgumentException(
                    "Vertex count must be non-negative"
            );
        }

        this.vertexCount = vertexCount;
        this.directed = directed;

        adjacency = new ArrayList<>(vertexCount);
        adjacencyViews = new ArrayList<>(vertexCount);

        for (int i = 0; i < vertexCount; i++) {
            List<Edge> outgoing = new ArrayList<>();
            adjacency.add(outgoing);
            adjacencyViews.add(
                    Collections.unmodifiableList(outgoing)
            );
        }
    }

    public int vertexCount() {
        return vertexCount;
    }

    public int edgeCount() {
        return edges.size();
    }

    public boolean isDirected() {
        return directed;
    }

    public List<Edge> edges() {
        return edgeView;
    }

    public List<Edge> neighbors(int vertex) {
        checkVertex(vertex);
        return adjacencyViews.get(vertex);
    }

    public void addEdge(int from, int to, int weight) {
        checkVertex(from);
        checkVertex(to);

        Edge edge = new Edge(from, to, weight);

        edges.add(edge);
        adjacency.get(from).add(edge);

        if (!directed) {
            adjacency.get(to).add(
                    new Edge(to, from, weight)
            );
        }
    }

    public int[] inDegrees() {
        int[] degrees = new int[vertexCount];

        for (List<Edge> outgoing : adjacency) {
            for (Edge edge : outgoing) {
                degrees[edge.to()]++;
            }
        }

        return degrees;
    }

    public Graph reversed() {
        Graph reversed = new Graph(vertexCount, directed);

        for (Edge edge : edges) {
            reversed.addEdge(
                    edge.to(),
                    edge.from(),
                    edge.weight()
            );
        }

        return reversed;
    }

    private void checkVertex(int vertex) {
        if (vertex < 0 || vertex >= vertexCount) {
            throw new IllegalArgumentException(
                    "Invalid vertex: " + vertex
            );
        }
    }
}