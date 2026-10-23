package app;

import app.algorithms.DagShortestPath;
import app.algorithms.Kahn;
import app.algorithms.Kosaraju;
import app.algorithms.Kruskal;
import app.algorithms.Prim;
import app.benchmark.BenchmarkRunner;
import app.graph.Graph;
import app.graph.GraphReader;
import app.metrics.Metrics;
import app.result.MstResult;
import app.result.SccResult;
import app.result.ShortestPathResult;
import app.result.TopologicalResult;

import java.nio.file.Path;
import java.util.Arrays;

public final class Main {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            printUsage();
            return;
        }

        switch (args[0]) {
            case "demo" -> {
                if (args.length != 2) {
                    throw new IllegalArgumentException(
                            "Usage: demo <graph-file>"
                    );
                }

                demo(Path.of(args[1]));
            }

            case "benchmark" -> {
                boolean smoke = args.length == 2
                        && args[1].equals("--smoke");

                if (args.length > 2
                        || (args.length == 2 && !smoke)) {
                    throw new IllegalArgumentException(
                            "Usage: benchmark [--smoke]"
                    );
                }

                Path output = Path.of(
                        smoke
                                ? "results/smoke.csv"
                                : "results/results.csv"
                );

                new BenchmarkRunner().run(output, smoke);
            }

            default -> throw new IllegalArgumentException(
                    "Unknown command: " + args[0]
            );
        }
    }

    private static void demo(Path input) throws Exception {
        Graph graph = new GraphReader().read(input);

        System.out.printf(
                "Vertices: %d, edges: %d, directed: %s%n",
                graph.vertexCount(),
                graph.edgeCount(),
                graph.isDirected()
        );

        if (!graph.isDirected()) {
            MstResult kruskal =
                    new Kruskal().run(graph, new Metrics());

            MstResult prim =
                    new Prim().run(graph, new Metrics());

            System.out.println(
                    "Kruskal weight: " + kruskal.totalWeight()
            );

            System.out.println(
                    "Prim weight: " + prim.totalWeight()
            );

            System.out.println(
                    "Components: " + kruskal.components()
            );

            System.out.println(
                    "Forest edges: " + kruskal.edges()
            );

            return;
        }

        SccResult scc =
                new Kosaraju().run(graph, new Metrics());

        System.out.println(
                "SCC count: " + scc.componentCount()
        );

        System.out.println(
                "Component labels: "
                        + Arrays.toString(scc.componentOf())
        );

        TopologicalResult topological =
                new Kahn().run(graph, new Metrics());

        if (topological.hasCycle()) {
            System.out.println(
                    "Cycle detected. Topological order "
                            + "and DAG shortest paths are unavailable."
            );

            return;
        }

        System.out.println(
                "Topological order: "
                        + Arrays.toString(topological.order())
        );

        ShortestPathResult paths =
                new DagShortestPath()
                        .run(graph, new Metrics());

        System.out.println(
                "Distances: "
                        + Arrays.toString(paths.distance())
        );

        for (int vertex = 0;
             vertex < graph.vertexCount();
             vertex++) {

            System.out.println(
                    "Path to " + vertex
                            + ": " + paths.pathTo(vertex)
            );
        }
    }

    private static void printUsage() {
        System.out.println("""
                Commands:
                  demo <graph-file>
                  benchmark --smoke
                  benchmark
                """);
    }
}