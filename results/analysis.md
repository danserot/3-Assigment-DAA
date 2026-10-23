# Measured benchmark analysis

Generated from results/results.csv.

## Counter normalization

| Algorithm | Density | n | scans/(n+m) | dsu_ops/m | comparisons/(m log2 m) |
|---|---|---:|---:|---:|---:|
| DagShortestPath | dense | 1000 | 0.952381 | 0.000000 | 0.000000 |
| DagShortestPath | dense | 3162 | 0.952381 | 0.000000 | 0.000000 |
| DagShortestPath | dense | 10000 | 0.952381 | 0.000000 | 0.000000 |
| DagShortestPath | dense | 31623 | 0.952381 | 0.000000 | 0.000000 |
| DagShortestPath | dense | 100000 | 0.952381 | 0.000000 | 0.000000 |
| DagShortestPath | sparse | 1000 | 0.800000 | 0.000000 | 0.000000 |
| DagShortestPath | sparse | 3162 | 0.800000 | 0.000000 | 0.000000 |
| DagShortestPath | sparse | 10000 | 0.800000 | 0.000000 | 0.000000 |
| DagShortestPath | sparse | 31623 | 0.800000 | 0.000000 | 0.000000 |
| DagShortestPath | sparse | 100000 | 0.800000 | 0.000000 | 0.000000 |
| Kahn | dense | 1000 | 0.952381 | 0.000000 | 0.000000 |
| Kahn | dense | 3162 | 0.952381 | 0.000000 | 0.000000 |
| Kahn | dense | 10000 | 0.952381 | 0.000000 | 0.000000 |
| Kahn | dense | 31623 | 0.952381 | 0.000000 | 0.000000 |
| Kahn | dense | 100000 | 0.952381 | 0.000000 | 0.000000 |
| Kahn | sparse | 1000 | 0.800000 | 0.000000 | 0.000000 |
| Kahn | sparse | 3162 | 0.800000 | 0.000000 | 0.000000 |
| Kahn | sparse | 10000 | 0.800000 | 0.000000 | 0.000000 |
| Kahn | sparse | 31623 | 0.800000 | 0.000000 | 0.000000 |
| Kahn | sparse | 100000 | 0.800000 | 0.000000 | 0.000000 |
| Kosaraju | dense | 1000 | 1.904762 | 0.000000 | 0.000000 |
| Kosaraju | dense | 3162 | 1.904762 | 0.000000 | 0.000000 |
| Kosaraju | dense | 10000 | 1.904762 | 0.000000 | 0.000000 |
| Kosaraju | dense | 31623 | 1.904762 | 0.000000 | 0.000000 |
| Kosaraju | dense | 100000 | 1.904762 | 0.000000 | 0.000000 |
| Kosaraju | sparse | 1000 | 1.600000 | 0.000000 | 0.000000 |
| Kosaraju | sparse | 3162 | 1.600000 | 0.000000 | 0.000000 |
| Kosaraju | sparse | 10000 | 1.600000 | 0.000000 | 0.000000 |
| Kosaraju | sparse | 31623 | 1.600000 | 0.000000 | 0.000000 |
| Kosaraju | sparse | 100000 | 1.600000 | 0.000000 | 0.000000 |
| Kruskal | dense | 1000 | 0.204571 | 0.479550 | 0.912651 |
| Kruskal | dense | 3162 | 0.217132 | 0.505961 | 0.918800 |
| Kruskal | dense | 10000 | 0.196905 | 0.463495 | 0.928465 |
| Kruskal | dense | 31623 | 0.286916 | 0.652522 | 0.935345 |
| Kruskal | dense | 100000 | 0.353932 | 0.793257 | 0.938332 |
| Kruskal | sparse | 1000 | 0.616800 | 1.791750 | 0.891417 |
| Kruskal | sparse | 3162 | 0.708412 | 2.020952 | 0.907017 |
| Kruskal | sparse | 10000 | 0.773040 | 2.182575 | 0.918239 |
| Kruskal | sparse | 31623 | 0.792891 | 2.232220 | 0.923529 |
| Kruskal | sparse | 100000 | 0.799754 | 2.249382 | 0.932257 |
| Prim | dense | 1000 | 1.904762 | 0.000000 | 1.826206 |
| Prim | dense | 3162 | 1.904762 | 0.000000 | 1.848806 |
| Prim | dense | 10000 | 1.904762 | 0.000000 | 1.867894 |
| Prim | dense | 31623 | 1.904762 | 0.000000 | 1.880362 |
| Prim | dense | 100000 | 1.904762 | 0.000000 | 1.892077 |
| Prim | sparse | 1000 | 1.600000 | 0.000000 | 1.831054 |
| Prim | sparse | 3162 | 1.600000 | 0.000000 | 1.866399 |
| Prim | sparse | 10000 | 1.600000 | 0.000000 | 1.899848 |
| Prim | sparse | 31623 | 1.600000 | 0.000000 | 1.929673 |
| Prim | sparse | 100000 | 1.600000 | 0.000000 | 1.945822 |

## Log-log slopes

| Algorithm | Density | Edge scans slope | Weight comparisons slope |
|---|---|---:|---:|
| DagShortestPath | dense | 1.0000 | N/A |
| DagShortestPath | sparse | 1.0000 | N/A |
| Kahn | dense | 1.0000 | N/A |
| Kahn | sparse | 1.0000 | N/A |
| Kosaraju | dense | 1.0000 | N/A |
| Kosaraju | sparse | 1.0000 | N/A |
| Kruskal | dense | 1.1194 | 1.0891 |
| Kruskal | sparse | 1.0549 | 1.1050 |
| Prim | dense | 1.0000 | 1.0904 |
| Prim | sparse | 1.0000 | 1.1091 |

Kruskal's sorting cost is reflected in weight comparisons, not in the post-sort edge scan count.

## Wall-clock measurements

- DagShortestPath, dense: median-of-five times range from 0.928200 to 127.411500 ms across the tested sizes.
- DagShortestPath, sparse: median-of-five times range from 0.897100 to 49.498300 ms across the tested sizes.
- Kahn, dense: median-of-five times range from 0.434700 to 85.341700 ms across the tested sizes.
- Kahn, sparse: median-of-five times range from 0.320600 to 24.004000 ms across the tested sizes.
- Kosaraju, dense: median-of-five times range from 1.530000 to 373.711100 ms across the tested sizes.
- Kosaraju, sparse: median-of-five times range from 1.121800 to 95.690600 ms across the tested sizes.
- Kruskal, dense: median-of-five times range from 5.360500 to 879.421400 ms across the tested sizes.
- Kruskal, sparse: median-of-five times range from 1.326800 to 153.947300 ms across the tested sizes.
- Prim, dense: median-of-five times range from 4.364400 to 1699.771400 ms across the tested sizes.
- Prim, sparse: median-of-five times range from 1.554700 to 209.630900 ms across the tested sizes.

These measurements describe this execution environment. They are not hardware-independent performance guarantees.
