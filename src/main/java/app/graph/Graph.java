package main.java.app.graph;

public final class Graph {
    public int vertexCount();
    public int edgeCount();
    public boolean isDirected();

    public List<Arc> neighbors(int vertex);
    public List<Edge> edges();

    public void addEdge(int from, int to, int weight);
    public Graph reversed();
}