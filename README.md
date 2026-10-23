# DAA Assignment 3 — Graphs

## Requirements

- Java 17 or newer.
- Maven.
- Python 3.10 or newer for plots.

## Implemented algorithms

- Disjoint Set Union:
  union by size and iterative path compression.
- Kruskal minimum spanning forest.
- Lazy Prim minimum spanning forest.
- Iterative Kosaraju strongly connected components.
- Kahn topological ordering and cycle detection.
- Multi-source shortest paths in a DAG.

All graph algorithms use the same adjacency-list Graph class.

## Build and tests

```bash
mvn clean test package
```

Tests include:

- DSU against a simple reference implementation.
- Kruskal vs Prim on 100 random connected graphs.
- Disconnected forests, loops and parallel edges.
- SCC against BFS-based mutual reachability.
- SCC on a path of 100,000 vertices.
- Topological order validation and cycle detection.
- DAG shortest paths against Bellman–Ford.
- Negative weights and multiple sources.
- Generator reproducibility and a smoke benchmark.

## Demo

```bash
java -jar target/graphs-1.0.0.jar demo data/connected.txt
java -jar target/graphs-1.0.0.jar demo data/disconnected.txt
java -jar target/graphs-1.0.0.jar demo data/cyclic.txt
```

Input format:

```text
n m directed|undirected
u v weight
...
```

Vertices are numbered from 0 to n-1.

Blank lines and lines starting with # are ignored.

## Smoke benchmark

```bash
java -Xmx2g -jar target/graphs-1.0.0.jar benchmark --smoke
```

Writes `results/smoke.csv`.

This checks the benchmark pipeline on smaller inputs.
It does not validate the assignment's reference table.

## Full benchmark

```bash
java -Xmx2g -jar target/graphs-1.0.0.jar benchmark
```

Sizes:

- 1,000
- 3,162
- 10,000
- 31,623
- 100,000

Densities:

- sparse: m = 4n
- dense: m = 20n

The benchmark creates three graph kinds per size/density pair.

Each algorithm has one discarded warmup and five measured runs.
CSV stores the median run and its counters.

The full benchmark checks:

- MST weight agreement.
- Forest structure and connectivity.
- Topological order validity.
- Kosaraju scans = 2m.
- Kahn scans = m on generated DAGs.
- DAG relaxation scans = m.
- Results against the assignment's reference table.

Results are written only after all checks pass.

## Counter conventions

- dsu_ops = find calls + successful unions.
- comparisons = comparisons of edge weights.
- edge_scans counts algorithm traversal/selection scans.
- max_depth is the maximum number of entries
  in an explicit DFS stack.

Kosaraju's graph reversal is excluded from its DFS scan counter.

Kahn's in-degree preparation is excluded from its traversal
scan counter.

DAG shortest-path edge_scans counts the relaxation pass.
Its measured time includes topological sorting.

Prim scans adjacency entries, so an undirected input edge
is scanned twice.

Operation totals in the first plot are the sum of recorded
dsu_ops, edge_scans and comparisons. These are selected
algorithmic operations, not every executed instruction.

## Plots and measured analysis

```bash
python -m pip install -r src/scripts/requirements.txt
python src/scripts/plot_results.py
```

Outputs:

- results/plots/operations_vs_n.png
- results/plots/scans_per_edge.png
- results/plots/kruskal_examined_share.png
- results/analysis.md

## Reproducibility

Each generated graph starts with a new Random(42).

Weights are generated immediately after the endpoints
of each accepted edge.

A reference mismatch must be investigated.
Do not replace the expected values with actual results
just to make the check pass.


## Result location

The Java benchmark and Python plotting script both use
`results/results.csv` in the project root.
The old empty `src/results/result.csv` is not an input.
Run all Java commands from the project root.

## Windows without Maven on PATH

The included `run.ps1` finds Maven on PATH or in IntelliJ IDEA.
It locates JDK 17+ using JAVA_HOME or installed JDK folders.

```powershell
.\run.ps1 test
.\run.ps1 smoke
.\run.ps1 benchmark
python src/scripts/plot_results.py
```
