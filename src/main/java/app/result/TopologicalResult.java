package app.result;

public record TopologicalResult(
        int[] order,
        boolean hasCycle
) {
}