package algorithms;

import app.algorithms.DisjointSetUnion;
import app.algorithms.Kruskal;
import app.algorithms.Prim;
import app.graph.Edge;
import app.graph.Graph;
import app.graph.GraphReader;
import app.metrics.Metrics;
import org.junit.jupiter.api.Test;
import app.result.MstResult;

import java.nio.file.Path;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class MstTest {

    @Test
    void algorithmsAgreeOnOneHundredConnectedGraphs() {
        Random random = new Random(42);

        for (int test = 0; test < 100; test++) {
            int n = 2 + random.nextInt(60);
            Graph graph = new Graph(n, false);

            // Сначала строим связный граф.
            for (int vertex = 1; vertex < n; vertex++) {
                graph.addEdge(
                        random.nextInt(vertex),
                        vertex,
                        random.nextInt(101) - 50
                );
            }

            // Допускаем петли и параллельные ребра.
            for (int i = 0; i < 4 * n; i++) {
                graph.addEdge(
                        random.nextInt(n),
                        random.nextInt(n),
                        random.nextInt(101) - 50
                );
            }

            MstResult kruskal =
                    new Kruskal().run(graph, new Metrics());

            MstResult prim =
                    new Prim().run(graph, new Metrics());

            assertEquals(
                    kruskal.totalWeight(),
                    prim.totalWeight()
            );

            assertForest(n, kruskal);
            assertForest(n, prim);

            assertEquals(1, kruskal.components());
            assertEquals(n - 1, kruskal.edges().size());
        }
    }

    @Test
    void returnsForestForDisconnectedExample()
            throws Exception {

        Graph graph = new GraphReader().read(
                Path.of("data/disconnected.txt")
        );

        MstResult kruskal =
                new Kruskal().run(graph, new Metrics());

        MstResult prim =
                new Prim().run(graph, new Metrics());

        assertEquals(4, kruskal.totalWeight());
        assertEquals(4, prim.totalWeight());
        assertEquals(3, kruskal.components());
        assertEquals(3, prim.components());

        assertForest(graph.vertexCount(), kruskal);
        assertForest(graph.vertexCount(), prim);
    }

    @Test
    void findsExpectedWeightOnConnectedExample()
            throws Exception {

        Graph graph = new GraphReader().read(
                Path.of("data/connected.txt")
        );

        assertEquals(
                10,
                new Kruskal()
                        .run(graph, new Metrics())
                        .totalWeight()
        );

        assertEquals(
                10,
                new Prim()
                        .run(graph, new Metrics())
                        .totalWeight()
        );
    }

    @Test
    void handlesEmptyGraphAndSelfLoop() {
        Graph empty = new Graph(0, false);

        assertEquals(
                0,
                new Prim()
                        .run(empty, new Metrics())
                        .components()
        );

        assertEquals(
                0,
                new Kruskal()
                        .run(empty, new Metrics())
                        .totalWeight()
        );

        Graph loop = new Graph(1, false);
        loop.addEdge(0, 0, -100);

        assertEquals(
                0,
                new Prim()
                        .run(loop, new Metrics())
                        .totalWeight()
        );

        assertEquals(
                0,
                new Kruskal()
                        .run(loop, new Metrics())
                        .totalWeight()
        );
    }

    private void assertForest(int n, MstResult result) {
        DisjointSetUnion checker =
                new DisjointSetUnion(n, new Metrics());

        long sum = 0;

        for (Edge edge : result.edges()) {
            assertTrue(
                    checker.union(edge.from(), edge.to()),
                    "Result contains a cycle"
            );

            sum += edge.weight();
        }

        assertEquals(sum, result.totalWeight());
        assertEquals(
                checker.components(),
                result.components()
        );

        assertEquals(
                n - result.components(),
                result.edges().size()
        );
    }
}