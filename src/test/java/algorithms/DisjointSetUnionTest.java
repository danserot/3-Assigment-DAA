package algorithms;

import app.algorithms.DisjointSetUnion;
import app.metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class DisjointSetUnionTest {

    @Test
    void agreesWithSimpleReference() {
        int n = 80;

        int[] labels = new int[n];

        for (int i = 0; i < n; i++) {
            labels[i] = i;
        }

        Metrics metrics = new Metrics();
        DisjointSetUnion dsu =
                new DisjointSetUnion(n, metrics);

        Random random = new Random(42);

        for (int step = 0; step < 1_000; step++) {
            int first = random.nextInt(n);
            int second = random.nextInt(n);

            int oldLabel = labels[second];
            int newLabel = labels[first];

            boolean expectedMerge = oldLabel != newLabel;

            if (expectedMerge) {
                for (int i = 0; i < n; i++) {
                    if (labels[i] == oldLabel) {
                        labels[i] = newLabel;
                    }
                }
            }

            assertEquals(
                    expectedMerge,
                    dsu.union(first, second)
            );

            long expectedComponents =
                    Arrays.stream(labels).distinct().count();

            assertEquals(
                    expectedComponents,
                    dsu.components()
            );

            for (int i = 0; i < n; i++) {
                assertEquals(
                        labels[i] == labels[first],
                        dsu.find(i) == dsu.find(first)
                );
            }
        }
    }

    @Test
    void countsFindsAndSuccessfulUnions() {
        Metrics metrics = new Metrics();
        DisjointSetUnion dsu =
                new DisjointSetUnion(3, metrics);

        assertTrue(dsu.union(0, 1));
        assertFalse(dsu.union(1, 0));

        assertEquals(4, metrics.findCalls());
        assertEquals(1, metrics.successfulUnions());
        assertEquals(5, metrics.dsuOps());
        assertEquals(2, dsu.components());
    }
}