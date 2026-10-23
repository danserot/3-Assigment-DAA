package algorithms;

import app.algorithms.Kosaraju;
import app.graph.Edge;
import app.graph.Graph;
import app.graph.GraphReader;
import app.metrics.Metrics;
import org.junit.jupiter.api.Test;
import app.result.SccResult;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class KosarajuTest {

    @Test
    void agreesWithBruteForceReachability() {
        Random random = new Random(42);

        for (int test = 0; test < 30; test++) {
            int n = 1 + random.nextInt(200);
            Graph graph = new Graph(n, true);

            for (int edge = 0; edge < 4 * n; edge++) {
                graph.addEdge(
                        random.nextInt(n),
                        random.nextInt(n),
                        1
                );
            }

            Metrics metrics = new Metrics();

            SccResult result =
                    new Kosaraju().run(graph, metrics);

            boolean[][] reachable = reachability(graph);

            for (int first = 0; first < n; first++) {
                for (int second = 0; second < n; second++) {
                    boolean expected =
                            reachable[first][second]
                                    && reachable[second][first];

                    assertEquals(
                            expected,
                            result.componentOf()[first]
                                    == result.componentOf()[second]
                    );
                }
            }

            assertEquals(
                    Arrays.stream(result.componentOf())
                            .distinct()
                            .count(),
                    result.componentCount()
            );

            assertEquals(
                    2L * graph.edgeCount(),
                    metrics.edgeScans()
            );
        }
    }

    @Test
    void handlesPathWithOneHundredThousandVertices() {
        int n = 100_000;
        Graph graph = new Graph(n, true);

        for (int vertex = 0; vertex + 1 < n; vertex++) {
            graph.addEdge(vertex, vertex + 1, 1);
        }

        Metrics metrics = new Metrics();

        SccResult result = assertDoesNotThrow(
                () -> new Kosaraju().run(graph, metrics)
        );

        assertEquals(n, result.componentCount());
        assertEquals(n, metrics.maxDepth());
        assertEquals(2L * (n - 1), metrics.edgeScans());
    }

    @Test
    void findsComponentsInCyclicExample()
            throws Exception {

        Graph graph = new GraphReader().read(
                Path.of("data/cyclic.txt")
        );

        SccResult result =
                new Kosaraju().run(graph, new Metrics());

        assertEquals(3, result.componentCount());

        assertEquals(
                result.componentOf()[0],
                result.componentOf()[2]
        );

        assertEquals(
                result.componentOf()[3],
                result.componentOf()[4]
        );

        assertNotEquals(
                result.componentOf()[4],
                result.componentOf()[5]
        );
    }

    private boolean[][] reachability(Graph graph) {
        int n = graph.vertexCount();
        boolean[][] reachable = new boolean[n][n];

        for (int source = 0; source < n; source++) {
            ArrayDeque<Integer> queue = new ArrayDeque<>();

            reachable[source][source] = true;
            queue.add(source);

            while (!queue.isEmpty()) {
                int vertex = queue.remove();

                for (Edge edge : graph.neighbors(vertex)) {
                    if (!reachable[source][edge.to()]) {
                        reachable[source][edge.to()] = true;
                        queue.add(edge.to());
                    }
                }
            }
        }

        return reachable;
    }
}