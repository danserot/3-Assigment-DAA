package app.graph;

import graph.Graph;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class GraphReader {

    public Graph read(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(
                path,
                StandardCharsets.UTF_8
        )) {
            String header = nextLine(reader);

            if (header == null) {
                throw new IllegalArgumentException(
                        "Graph file is empty"
                );
            }

            String[] parts = header.split("\\s+");

            if (parts.length != 3) {
                throw new IllegalArgumentException(
                        "Expected: n m directed|undirected"
                );
            }

            int n = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);

            if (m < 0) {
                throw new IllegalArgumentException(
                        "Edge count must be non-negative"
                );
            }

            boolean directed = switch (parts[2]) {
                case "directed" -> true;
                case "undirected" -> false;
                default -> throw new IllegalArgumentException(
                        "Unknown graph type: " + parts[2]
                );
            };

            Graph graph = new Graph(n, directed);

            for (int i = 0; i < m; i++) {
                String line = nextLine(reader);

                if (line == null) {
                    throw new IllegalArgumentException(
                            "Missing edge at index " + i
                    );
                }

                String[] values = line.split("\\s+");

                if (values.length != 3) {
                    throw new IllegalArgumentException(
                            "Expected edge: u v w"
                    );
                }

                graph.addEdge(
                        Integer.parseInt(values[0]),
                        Integer.parseInt(values[1]),
                        Integer.parseInt(values[2])
                );
            }

            if (nextLine(reader) != null) {
                throw new IllegalArgumentException(
                        "File contains more edges than declared"
                );
            }

            return graph;
        }
    }

    private String nextLine(BufferedReader reader)
            throws IOException {

        String line;

        while ((line = reader.readLine()) != null) {
            line = line.trim();

            if (!line.isEmpty() && !line.startsWith("#")) {
                return line;
            }
        }

        return null;
    }
}