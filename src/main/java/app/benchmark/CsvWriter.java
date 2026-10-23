package app.benchmark;

import app.metrics.Metrics;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public final class CsvWriter {

    public record Row(
            String algorithm,
            String graph,
            int n,
            int m,
            double timeMs,
            Metrics metrics,
            long result
    ) {
    }

    public void write(
            Path path,
            List<Row> rows
    ) throws IOException {

        Path parent = path.toAbsolutePath().getParent();
        Files.createDirectories(parent);

        try (BufferedWriter writer = Files.newBufferedWriter(
                path,
                StandardCharsets.UTF_8
        )) {
            writer.write(
                    "algorithm,graph,n,m,time_ms,"
                            + "dsu_ops,edge_scans,comparisons,"
                            + "max_depth,result"
            );

            writer.newLine();

            for (Row row : rows) {
                Metrics metrics = row.metrics();

                writer.write(String.format(
                        Locale.ROOT,
                        "%s,%s,%d,%d,%.6f,%d,%d,%d,%d,%d",
                        row.algorithm(),
                        row.graph(),
                        row.n(),
                        row.m(),
                        row.timeMs(),
                        metrics.dsuOps(),
                        metrics.edgeScans(),
                        metrics.comparisons(),
                        metrics.maxDepth(),
                        row.result()
                ));

                writer.newLine();
            }
        }
    }
}