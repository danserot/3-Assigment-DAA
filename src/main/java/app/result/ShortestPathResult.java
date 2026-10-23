package app.result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record ShortestPathResult(
        long[] distance,
        int[] parent
) {

    public long sumDistances() {
        long sum = 0;

        for (long value : distance) {
            if (value == Long.MAX_VALUE) {
                throw new IllegalStateException(
                        "An unreachable vertex has no finite distance"
                );
            }

            sum = Math.addExact(sum, value);
        }

        return sum;
    }

    public List<Integer> pathTo(int vertex) {
        if (vertex < 0 || vertex >= distance.length) {
            throw new IllegalArgumentException(
                    "Invalid vertex: " + vertex
            );
        }

        if (distance[vertex] == Long.MAX_VALUE) {
            return List.of();
        }

        List<Integer> path = new ArrayList<>();

        for (int current = vertex;
             current != -1;
             current = parent[current]) {

            path.add(current);
        }

        Collections.reverse(path);

        return List.copyOf(path);
    }
}