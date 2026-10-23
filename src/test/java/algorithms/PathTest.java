package algorithms;

import app.algorithms.DagShortestPath;
import app.algorithms.Kahn;
import app.graph.Edge;
import app.graph.Graph;
import app.graph.GraphReader;
import app.metrics.Metrics;
import org.junit.jupiter.api.Test;
import app.result.ShortestPathResult;
import app.result.TopologicalResult;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class PathsTest {

    @Test
    void buildsValidTopologicalOrder() {
        Graph graph = new Graph(5, true);

        graph.addEdge(0, 2, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 3, 1);
        graph.addEdge(1, 4, 1);

        Metrics metrics = new Metrics();

        TopologicalResult result =
                new Kahn().run(graph, metrics);

        assertFalse(result.hasCycle());
        assertValidOrder(graph, result.order());

        assertEquals(
                graph.edgeCount(),
                metrics.edgeScans()
        );
    }

    @Test
    void detectsCycleFromExample() throws Exception {
        Graph graph = new GraphReader().read(
                Path.of("data/cyclic.txt")
        );

        TopologicalResult result =
                new Kahn().run(graph, new Metrics());

        assertTrue(result.hasCycle());
        assertEquals(0, result.order().length);

        assertThrows(
                IllegalArgumentException.class,
                () -> new DagShortestPath()
                        .run(graph, new Metrics())
        );
    }

    @Test
    void usesAllSourcesAndRestoresNegativePath() {
        Graph graph = new Graph(6, true);

        graph.addEdge(0, 2, 5);
        graph.addEdge(1, 2, -2);
        graph.addEdge(2, 3, 3);
        graph.addEdge(1, 4, 10);
        graph.addEdge(3, 4, -5);

        ShortestPathResult result =
                new DagShortestPath()
                        .run(graph, new Metrics());

        assertArrayEquals(
                new long[]{0, 0, -2, 1, -4, 0},
                result.distance()
        );

        assertEquals(
                List.of(1, 2, 3, 4),
                result.pathTo(4)
        );

        assertEquals(List.of(5), result.pathTo(5));
        assertEquals(-5, result.sumDistances());
    }

    @Test
    void agreesWithBellmanFordOnRandomDags() {
        Random random = new Random(42);

        for (int test = 0; test < 100; test++) {
            int n = 2 + random.nextInt(40);
            Graph graph = new Graph(n, true);

            // Ребра только от меньшего индекса к большему:
            // цикл появиться не может.
            for (int from = 0; from < n; from++) {
                for (int to = from + 1; to < n; to++) {
                    if (random.nextDouble() < 0.15) {
                        graph.addEdge(
                                from,
                                to,
                                random.nextInt(101) - 50
                        );
                    }
                }
            }

            Metrics metrics = new Metrics();

            ShortestPathResult actual =
                    new DagShortestPath()
                            .run(graph, metrics);

            assertArrayEquals(
                    bellmanFord(graph),
                    actual.distance()
            );

            assertEquals(
                    graph.edgeCount(),
                    metrics.edgeScans()
            );

            for (int vertex = 0; vertex < n; vertex++) {
                assertValidPath(graph, actual, vertex);
            }
        }
    }

    @Test
    void supportsEmptyDag() {
        Graph graph = new Graph(0, true);

        assertFalse(
                new Kahn()
                        .run(graph, new Metrics())
                        .hasCycle()
        );

        assertEquals(
                0,
                new DagShortestPath()
                        .run(graph, new Metrics())
                        .sumDistances()
        );
    }

    private long[] bellmanFord(Graph graph) {
        int n = graph.vertexCount();

        long[] distance = new long[n];
        Arrays.fill(distance, Long.MAX_VALUE);

        int[] inDegree = graph.inDegrees();

        for (int vertex = 0; vertex < n; vertex++) {
            if (inDegree[vertex] == 0) {
                distance[vertex] = 0;
            }
        }

        for (int iteration = 0;
             iteration < n - 1;
             iteration++) {

            boolean changed = false;

            for (Edge edge : graph.edges()) {
                if (distance[edge.from()] == Long.MAX_VALUE) {
                    continue;
                }

                long candidate =
                        distance[edge.from()] + edge.weight();

                if (candidate < distance[edge.to()]) {
                    distance[edge.to()] = candidate;
                    changed = true;
                }
            }

            if (!changed) {
                break;
            }
        }

        return distance;
    }

    private void assertValidOrder(
            Graph graph,
            int[] order
    ) {
        assertEquals(graph.vertexCount(), order.length);

        int[] position = new int[order.length];
        boolean[] seen = new boolean[order.length];

        for (int index = 0; index < order.length; index++) {
            int vertex = order[index];

            assertTrue(vertex >= 0 && vertex < order.length);
            assertFalse(seen[vertex]);

            seen[vertex] = true;
            position[vertex] = index;
        }

        for (Edge edge : graph.edges()) {
            assertTrue(
                    position[edge.from()]
                            < position[edge.to()]
            );
        }
    }

    private void assertValidPath(
            Graph graph,
            ShortestPathResult result,
            int target
    ) {
        List<Integer> path = result.pathTo(target);

        assertFalse(path.isEmpty());
        assertEquals(
                target,
                path.get(path.size() - 1).intValue()
        );

        assertEquals(
                0,
                graph.inDegrees()[path.get(0)]
        );

        long weight = 0;

        for (int index = 1; index < path.size(); index++) {
            int from = path.get(index - 1);
            int to = path.get(index);

            long difference =
                    result.distance()[to]
                            - result.distance()[from];

            boolean matchingEdge =
                    graph.neighbors(from)
                            .stream()
                            .anyMatch(edge ->
                                    edge.to() == to
                                            && edge.weight()
                                            == difference
                            );

            assertTrue(matchingEdge);
            weight += difference;
        }

        assertEquals(
                result.distance()[target],
                weight
        );
    }
}