from __future__ import annotations

import csv
import math
import statistics
from collections import defaultdict
from pathlib import Path


import matplotlib

matplotlib.use("Agg")

import matplotlib.pyplot as plt


# src/scripts/plot_results.py -> project root.
ROOT = Path(__file__).resolve().parents[2]
CSV_PATH = ROOT / "results" / "results.csv"
PLOT_DIR = ROOT / "results" / "plots"
ANALYSIS_PATH = ROOT / "results" / "analysis.md"


def load_rows() -> list[dict]:
    if not CSV_PATH.is_file():
        raise ValueError(
            f"Benchmark CSV not found: {CSV_PATH}. "
            "Run the full Java benchmark first: "
            "java -Xmx2g -jar target/graphs-1.0.0.jar benchmark"
        )

    with CSV_PATH.open(encoding="utf-8-sig", newline="") as file:
        reader = csv.DictReader(file)
        required = {
            "algorithm", "graph", "n", "m", "time_ms",
            "dsu_ops", "edge_scans", "comparisons",
            "max_depth", "result",
        }
        if not required.issubset(reader.fieldnames or []):
            raise ValueError(
                f"Invalid or empty benchmark CSV: {CSV_PATH}. "
                "Generate it with the full Java benchmark."
            )
        rows = list(reader)

    if len(rows) != 50:
        raise ValueError(
            f"Expected 50 benchmark rows, got {len(rows)}"
        )

    integer_fields = (
        "n",
        "m",
        "dsu_ops",
        "edge_scans",
        "comparisons",
        "max_depth",
        "result",
    )

    for row in rows:
        for field in integer_fields:
            row[field] = int(row[field])

        row["time_ms"] = float(row["time_ms"])
        row["density"] = row["graph"].rsplit("_", 1)[1]

    keys = {
        (row["algorithm"], row["density"], row["n"])
        for row in rows
    }

    if len(keys) != 50:
        raise ValueError("Duplicate benchmark cases")

    return rows


def group_rows(rows: list[dict]) -> dict:
    groups = defaultdict(list)

    for row in rows:
        key = (row["algorithm"], row["density"])
        groups[key].append(row)

    for points in groups.values():
        points.sort(key=lambda row: row["n"])

    return groups


def operations(row: dict) -> int:
    return (
        row["dsu_ops"]
        + row["edge_scans"]
        + row["comparisons"]
    )


def fit_slope(xs: list[int], ys: list[int]) -> float:
    pairs = [
        (math.log(x), math.log(y))
        for x, y in zip(xs, ys)
        if x > 0 and y > 0
    ]

    if len(pairs) < 2:
        return float("nan")

    mean_x = statistics.mean(x for x, _ in pairs)
    mean_y = statistics.mean(y for _, y in pairs)

    numerator = sum(
        (x - mean_x) * (y - mean_y)
        for x, y in pairs
    )

    denominator = sum(
        (x - mean_x) ** 2
        for x, _ in pairs
    )

    return numerator / denominator


def save_operations_plot(groups: dict) -> None:
    fig, ax = plt.subplots(figsize=(11, 7))

    for (algorithm, density), points in sorted(groups.items()):
        ax.plot(
            [row["n"] for row in points],
            [operations(row) for row in points],
            marker="o",
            label=f"{algorithm}, {density}",
        )

    ax.set_xscale("log")
    ax.set_yscale("log")

    ax.set_xlabel("Vertices n")
    ax.set_ylabel("Counted operations")
    ax.set_title("Operations vs graph size")

    ax.grid(True, which="both", alpha=0.25)
    ax.legend(fontsize=8)

    fig.tight_layout()
    fig.savefig(
        PLOT_DIR / "operations_vs_n.png",
        dpi=180,
    )
    plt.close(fig)


def save_scans_plot(groups: dict) -> None:
    fig, ax = plt.subplots(figsize=(11, 7))

    for (algorithm, density), points in sorted(groups.items()):
        ax.plot(
            [row["n"] for row in points],
            [
                row["edge_scans"] / row["m"]
                for row in points
            ],
            marker="o",
            label=f"{algorithm}, {density}",
        )

    ax.set_xscale("log")
    ax.set_xlabel("Vertices n")
    ax.set_ylabel("edge_scans / m")
    ax.set_title("Scans per input edge")

    ax.grid(True, which="both", alpha=0.25)
    ax.legend(fontsize=8)

    fig.tight_layout()
    fig.savefig(
        PLOT_DIR / "scans_per_edge.png",
        dpi=180,
    )
    plt.close(fig)


def save_kruskal_plot(groups: dict) -> None:
    fig, ax = plt.subplots(figsize=(9, 6))

    for density in ("sparse", "dense"):
        points = groups[("Kruskal", density)]

        ax.plot(
            [row["n"] for row in points],
            [
                row["edge_scans"] / row["m"]
                for row in points
            ],
            marker="o",
            label=density,
        )

    ax.set_xscale("log")
    ax.set_xlabel("Vertices n")
    ax.set_ylabel("Examined edges / m")
    ax.set_title("Share of edges examined by Kruskal")

    ax.grid(True, which="both", alpha=0.25)
    ax.legend()

    fig.tight_layout()
    fig.savefig(
        PLOT_DIR / "kruskal_examined_share.png",
        dpi=180,
    )
    plt.close(fig)


def write_analysis(
    rows: list[dict],
    groups: dict,
) -> None:
    lines = [
        "# Measured benchmark analysis",
        "",
        "Generated from results/results.csv.",
        "",
        "## Counter normalization",
        "",
        "| Algorithm | Density | n | scans/(n+m) "
        "| dsu_ops/m | comparisons/(m log2 m) |",
        "|---|---|---:|---:|---:|---:|",
    ]

    for row in sorted(
        rows,
        key=lambda item: (
            item["algorithm"],
            item["density"],
            item["n"],
        ),
    ):
        n = row["n"]
        m = row["m"]

        lines.append(
            f"| {row['algorithm']} "
            f"| {row['density']} "
            f"| {n} "
            f"| {row['edge_scans'] / (n + m):.6f} "
            f"| {row['dsu_ops'] / m:.6f} "
            f"| "
            f"{row['comparisons'] / (m * math.log2(m)):.6f} "
            f"|"
        )

    lines += [
        "",
        "## Log-log slopes",
        "",
        "| Algorithm | Density | Edge scans slope "
        "| Weight comparisons slope |",
        "|---|---|---:|---:|",
    ]

    for (algorithm, density), points in sorted(groups.items()):
        xs = [row["n"] for row in points]

        scan_slope = fit_slope(
            xs,
            [row["edge_scans"] for row in points],
        )

        comparison_slope = fit_slope(
            xs,
            [row["comparisons"] for row in points],
        )

        comparison_text = (
            f"{comparison_slope:.4f}"
            if math.isfinite(comparison_slope)
            else "N/A"
        )

        lines.append(
            f"| {algorithm} "
            f"| {density} "
            f"| {scan_slope:.4f} "
            f"| {comparison_text} |"
        )

    lines += [
        "",
        "Kruskal's sorting cost is reflected in weight "
        "comparisons, not in the post-sort edge scan count.",
        "",
        "## Wall-clock measurements",
        "",
    ]

    for (algorithm, density), points in sorted(groups.items()):
        times = [row["time_ms"] for row in points]

        lines.append(
            f"- {algorithm}, {density}: "
            f"median-of-five times range from "
            f"{min(times):.6f} to {max(times):.6f} ms "
            f"across the tested sizes."
        )

    lines += [
        "",
        "These measurements describe this execution environment. "
        "They are not hardware-independent performance guarantees.",
        "",
    ]

    ANALYSIS_PATH.write_text(
        "\n".join(lines),
        encoding="utf-8",
    )


def main() -> None:
    rows = load_rows()
    groups = group_rows(rows)

    PLOT_DIR.mkdir(parents=True, exist_ok=True)

    save_operations_plot(groups)
    save_scans_plot(groups)
    save_kruskal_plot(groups)

    write_analysis(rows, groups)

    print(f"Plots: {PLOT_DIR}")
    print(f"Analysis: {ANALYSIS_PATH}")


if __name__ == "__main__":
    main()