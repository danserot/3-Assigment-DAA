package benchmark;

import app.algorithms.DagShortestPath;
import app.algorithms.Kosaraju;
import app.algorithms.Kruskal;
import app.benchmark.GraphGenerator;
import app.metrics.Metrics;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReferenceValuesTest {
    @Test
    void reproducesPdfValuesForBothSmallestGraphDensities() {
        GraphGenerator generator = new GraphGenerator();
        int[] edges = {4_000, 20_000};
        long[] mst = {147_970, 30_363};
        int[] scc = {27, 1};
        long[] distances = {396_274, 252_411};

        for (int i = 0; i < edges.length; i++) {
            assertEquals(mst[i], new Kruskal().run(
                    generator.undirected(1_000, edges[i]), new Metrics()
            ).totalWeight());
            assertEquals(scc[i], new Kosaraju().run(
                    generator.directed(1_000, edges[i]), new Metrics()
            ).componentCount());
            assertEquals(distances[i], new DagShortestPath().run(
                    generator.dag(1_000, edges[i]), new Metrics()
            ).sumDistances());
        }
    }
}
