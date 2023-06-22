import matplotlib.pyplot as plt
from helpers import plot_pareto_frontier

availability = [
    75.47,
    73.31,
    73.00,
    72.90,
    72.84,
    72.57,
    71.87,
    70.66,
    62.19,
    61.43,
    72.90,
    72.92,
    73.24,
    73.17,
    73.05,
]
excess_data = [
    71.68,
    66.63,
    65.56,
    65.44,
    61.58,
    63.15,
    49.61,
    57.84,
    23.40,
    0.00,
    65.31,
    65.40,
    66.33,
    66.05,
    65.83,
]
labels = [
    ("T-FOMM (0 pctl)", (0, 0, "center", "center")),
    ("T-FOMM (Median)", (0, 0, "left", "center")),
    ("T-FOMM (DA, daysOfWeek)", (0, 0, "left", "center")),
    ("FOMM", (0, 0, "left", "center")),
    ("T-FOMM (HWES, node)", (0, 0, "left", "center")),
    ("T-FOMM (HWES, discretization)", (0, 0, "left", "center")),
    ("T-FOMM (HWES, user)", (0, 0, "left", "center")),
    ("T-FOMM (100 pctl)", (0, 0, "left", "center")),
    ("VOMM", (0, 0, "left", "center")),
    ("Keep-on-closest", (0, 0, "left", "center")),
]

plot = plot_pareto_frontier(
    availability,
    excess_data,
    y_desc=True,
    labelX="% Availability",
    labelY="% Excess Data",
    labels=labels,
)

plt.savefig("./figures/pareto.pdf", bbox_inches="tight")

# plt.show()

# debug_stop = 1
