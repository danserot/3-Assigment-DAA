package app.benchmark;

import app.algorithms.DagShortestPath;
import app.algorithms.Kahn;
import app.algorithms.Kosaraju;
import app.algorithms.Kruskal;
import app.algorithms.Prim;
import app.graph.Graph;
import app.metrics.Metrics;
import app.result.MstResult;
import app.result.SccResult;
import app.result.ShortestPathResult;
import app.result.TopologicalResult;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;


import static app.benchmark.ResultValidator.require;

public final class BenchmarkRunner {
    private static final int[] FULL_SIZES = {
            1_000,
            3_162,
            10_000,
            31_623,
            100_000
    };

    private static final int[] SMOKE_SIZES = {
            100,
            316
    };

    private static final int[] DENSITIES = {
            4,
            20
    };

    // Порядок: для каждого n сначала sparse, потом dense.
    private static final long[] EXPECTED_MST = {
            147_970, 30_363,
            468_911, 97_589,
            1_486_139, 309_550,
            4_715_515, 968_517,
            15_012_094, 3_056_681
    };

    private static final int[] EXPECTED_SCC = {
            27, 1,
            137, 1,
            373, 1,
            1_188, 1,
            3_923, 1
    };

    private static final long[] EXPECTED_DISTANCE_SUM = {
            396_274, 252_411,
            1_271_911, 723_157,
            3_941_828, 2_381_342,
            12_551_891, 7_376_612,
            40_096_539, 23_512_087
    };

    private record Measurement<T>(
            T result,
            Metrics metrics,
            double timeMs
    ) {
    }

    public void run(
            Path output,
            boolean smoke
    ) throws IOException {

        int[] sizes = smoke ? SMOKE_SIZES : FULL_SIZES;

        List<CsvWriter.Row> rows = new ArrayList<>();

        GraphGenerator generator = new GraphGenerator();
        ResultValidator validator = new ResultValidator();

        int referenceIndex = 0;

        for (int n : sizes) {
            for (int density : DENSITIES) {
                int m = Math.multiplyExact(n, density);

                String label = density == 4
                        ? "sparse"
                        : "dense";

                System.out.printf(
                        "Running n=%d, m=%d, density=%s%n",
                        n,
                        m,
                        label
                );

                // Одновременно держим только один вид графа.
                long mstWeight = measureMst(
                        generator,
                        validator,
                        rows,
                        n,
                        m,
                        label
                );

                int sccCount = measureScc(
                        generator,
                        rows,
                        n,
                        m,
                        label
                );

                long distanceSum = measureDag(
                        generator,
                        validator,
                        rows,
                        n,
                        m,
                        label
                );

                System.out.printf(
                        "  MST=%d, SCC=%d, DAG sum=%d%n",
                        mstWeight,
                        sccCount,
                        distanceSum
                );

                if (!smoke) {
                    checkReference(
                            referenceIndex,
                            n,
                            m,
                            mstWeight,
                            sccCount,
                            distanceSum
                    );
                }

                referenceIndex++;
            }
        }

        int expectedRows =
                sizes.length * DENSITIES.length * 5;

        require(
                rows.size() == expectedRows,
                "Incorrect CSV row count"
        );

        // CSV записывается только после всех проверок.
        new CsvWriter().write(output, rows);

        System.out.println(
                "Saved " + rows.size() + " rows to " + output
        );
    }

    private long measureMst(
            GraphGenerator generator,
            ResultValidator validator,
            List<CsvWriter.Row> rows,
            int n,
            int m,
            String label
    ) {
        Graph graph = generator.undirected(n, m);

        Measurement<MstResult> kruskal =
                measure(metrics ->
                        new Kruskal().run(graph, metrics)
                );

        Measurement<MstResult> prim =
                measure(metrics ->
                        new Prim().run(graph, metrics)
                );

        validator.validateForest(graph, kruskal.result());
        validator.validateForest(graph, prim.result());

        require(
                kruskal.result().totalWeight()
                        == prim.result().totalWeight(),
                "Kruskal and Prim weights differ"
        );

        require(
                kruskal.result().components() == 1
                        && prim.result().components() == 1,
                "Generated MST graph is disconnected"
        );

        rows.add(row(
                "Kruskal",
                "undirected_" + label,
                graph,
                kruskal,
                kruskal.result().totalWeight()
        ));

        rows.add(row(
                "Prim",
                "undirected_" + label,
                graph,
                prim,
                prim.result().totalWeight()
        ));

        return kruskal.result().totalWeight();
    }

    private int measureScc(
            GraphGenerator generator,
            List<CsvWriter.Row> rows,
            int n,
            int m,
            String label
    ) {
        Graph graph = generator.directed(n, m);

        Measurement<SccResult> kosaraju =
                measure(metrics ->
                        new Kosaraju().run(graph, metrics)
                );

        require(
                kosaraju.metrics().edgeScans() == 2L * m,
                "Kosaraju must scan 2m edges"
        );

        rows.add(row(
                "Kosaraju",
                "directed_" + label,
                graph,
                kosaraju,
                kosaraju.result().componentCount()
        ));

        return kosaraju.result().componentCount();
    }

    private long measureDag(
            GraphGenerator generator,
            ResultValidator validator,
            List<CsvWriter.Row> rows,
            int n,
            int m,
            String label
    ) {
        Graph graph = generator.dag(n, m);

        Measurement<TopologicalResult> kahn =
                measure(metrics ->
                        new Kahn().run(graph, metrics)
                );

        validator.validateTopologicalOrder(
                graph,
                kahn.result()
        );

        Measurement<ShortestPathResult> paths =
                measure(metrics ->
                        new DagShortestPath()
                                .run(graph, metrics)
                );

        require(
                kahn.metrics().edgeScans() == m,
                "Kahn must scan m edges on a DAG"
        );

        require(
                paths.metrics().edgeScans() == m,
                "DAG relaxation must scan m edges"
        );

        long distanceSum = paths.result().sumDistances();

        rows.add(row(
                "Kahn",
                "dag_" + label,
                graph,
                kahn,
                kahn.result().order().length
        ));

        rows.add(row(
                "DagShortestPath",
                "dag_" + label,
                graph,
                paths,
                distanceSum
        ));

        return distanceSum;
    }

    private <T> Measurement<T> measure(
            Function<Metrics, T> algorithm
    ) {
        // Прогрев не попадает в медиану.
        algorithm.apply(new Metrics());

        List<Measurement<T>> runs = new ArrayList<>(5);

        for (int run = 0; run < 5; run++) {
            Metrics metrics = new Metrics();

            long started = System.nanoTime();
            T result = algorithm.apply(metrics);
            long elapsed = System.nanoTime() - started;

            runs.add(new Measurement<>(
                    result,
                    metrics,
                    elapsed / 1_000_000.0
            ));
        }

        runs.sort(
                Comparator.comparingDouble(
                        measurement -> measurement.timeMs()
                )
        );

        return runs.get(2);
    }

    private CsvWriter.Row row(
            String algorithm,
            String graphName,
            Graph graph,
            Measurement<?> measurement,
            long result
    ) {
        return new CsvWriter.Row(
                algorithm,
                graphName,
                graph.vertexCount(),
                graph.edgeCount(),
                measurement.timeMs(),
                measurement.metrics(),
                result
        );
    }

    private void checkReference(
            int index,
            int n,
            int m,
            long mst,
            int scc,
            long distanceSum
    ) {
        boolean matches =
                mst == EXPECTED_MST[index]
                        && scc == EXPECTED_SCC[index]
                        && distanceSum
                        == EXPECTED_DISTANCE_SUM[index];

        if (!matches) {
            throw new IllegalStateException(
                    "Reference mismatch for n=" + n
                            + ", m=" + m
                            + ". Actual: MST=" + mst
                            + ", SCC=" + scc
                            + ", DAG sum=" + distanceSum
                            + ". Expected: MST="
                            + EXPECTED_MST[index]
                            + ", SCC=" + EXPECTED_SCC[index]
                            + ", DAG sum="
                            + EXPECTED_DISTANCE_SUM[index]
                            + ". Check the generator's Random call "
                            + "order against the assignment."
            );
        }
    }
}