package benchmark;

import app.algorithms.Kahn;
import app.algorithms.Kruskal;
import app.benchmark.BenchmarkRunner;
import app.benchmark.GraphGenerator;
import graph.Graph;
import app.metrics.Metrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BenchmarkTest {

    @TempDir
    Path directory;

    @Test
    void generatorsAreReproducible() {
        GraphGenerator generator = new GraphGenerator();

        assertEquals(
                generator.undirected(100, 400).edges(),
                generator.undirected(100, 400).edges()
        );

        assertEquals(
                generator.directed(100, 400).edges(),
                generator.directed(100, 400).edges()
        );

        assertEquals(
                generator.dag(100, 400).edges(),
                generator.dag(100, 400).edges()
        );
    }

    @Test
    void undirectedGeneratorProducesConnectedGraph() {
        Graph graph =
                new GraphGenerator().undirected(100, 400);

        assertEquals(400, graph.edgeCount());

        assertEquals(
                1,
                new Kruskal()
                        .run(graph, new Metrics())
                        .components()
        );
    }

    @Test
    void dagGeneratorDoesNotCreateCycles() {
        Graph graph =
                new GraphGenerator().dag(100, 2_000);

        assertEquals(2_000, graph.edgeCount());

        assertFalse(
                new Kahn()
                        .run(graph, new Metrics())
                        .hasCycle()
        );
    }

    @Test
    void smokeBenchmarkWritesExpectedCsv()
            throws Exception {

        Path output = directory.resolve("smoke.csv");

        new BenchmarkRunner().run(output, true);

        List<String> lines = Files.readAllLines(output);

        // Заголовок + 2 размера * 2 плотности * 5 алгоритмов.
        assertEquals(21, lines.size());

        assertEquals(
                "algorithm,graph,n,m,time_ms,"
                        + "dsu_ops,edge_scans,comparisons,"
                        + "max_depth,result",
                lines.get(0)
        );

        for (String line : lines.subList(1, lines.size())) {
            String[] columns = line.split(",");

            assertEquals(10, columns.length);
            assertTrue(Double.parseDouble(columns[4]) >= 0);
        }
    }
}