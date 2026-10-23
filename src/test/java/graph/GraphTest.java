package graph;

import app.graph.Edge;
import graph.Graph;
import app.graph.GraphReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class GraphTest {

    @TempDir
    Path directory;

    @Test
    void storesUndirectedEdgesInBothDirections() {
        Graph graph = new Graph(3, false);

        graph.addEdge(0, 1, 7);

        assertEquals(1, graph.edgeCount());
        assertEquals(
                new Edge(0, 1, 7),
                graph.neighbors(0).get(0)
        );
        assertEquals(
                new Edge(1, 0, 7),
                graph.neighbors(1).get(0)
        );
        assertTrue(graph.neighbors(2).isEmpty());
    }

    @Test
    void handlesLoopsAndParallelEdges() {
        Graph graph = new Graph(2, false);

        graph.addEdge(0, 0, 1);
        graph.addEdge(0, 1, 2);
        graph.addEdge(0, 1, 3);

        assertEquals(3, graph.edgeCount());
        assertEquals(4, graph.neighbors(0).size());
        assertEquals(2, graph.neighbors(1).size());
    }

    @Test
    void reversesDirectedEdges() {
        Graph graph = new Graph(2, true);
        graph.addEdge(0, 1, -5);

        Graph reversed = graph.reversed();

        assertTrue(reversed.neighbors(0).isEmpty());
        assertEquals(
                new Edge(1, 0, -5),
                reversed.neighbors(1).get(0)
        );
    }

    @Test
    void readsAllExampleFiles() throws Exception {
        GraphReader reader = new GraphReader();

        assertEquals(
                7,
                reader.read(Path.of("data/connected.txt"))
                        .edgeCount()
        );

        assertEquals(
                6,
                reader.read(Path.of("data/disconnected.txt"))
                        .vertexCount()
        );

        assertTrue(
                reader.read(Path.of("data/cyclic.txt"))
                        .isDirected()
        );
    }

    @Test
    void rejectsMissingEdges() throws Exception {
        Path file = directory.resolve("invalid.txt");

        Files.writeString(
                file,
                "3 2 directed\n0 1 5\n"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new GraphReader().read(file)
        );
    }

    @Test
    void rejectsInvalidVertex() {
        Graph graph = new Graph(2, true);

        assertThrows(
                IllegalArgumentException.class,
                () -> graph.addEdge(0, 2, 1)
        );
    }
}