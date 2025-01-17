import matplotlib as mpl
import matplotlib.pyplot as plt

mpl.rcParams["pdf.fonttype"] = 42
mpl.rcParams["ps.fonttype"] = 42
mpl.rcParams["figure.figsize"] = (4.5, 3)
mpl.rcParams["figure.dpi"] = 100
plt.rcParams["font.family"] = "sans-serif"
plt.rcParams["font.sans-serif"] = ["CMU Sans Serif"]

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

percentiles = {
    "availability": [
        75.47,
        75.04,
        74.64,
        74.16,
        73.93,
        73.31,
        72.90,
        72.74,
        72.36,
        71.77,
        71.12,
        70.66,
    ],
    "excess_data": [
        71.68,
        70.89,
        70.07,
        68.97,
        68.42,
        66.63,
        65.44,
        64.95,
        64.01,
        62.33,
        60.22,
        57.84,
    ],
    "labels": [
        "0 pctl",
        "10 pctl",
        "20 pctl",
        "30 pctl",
        "40 pctl",
        "50 pctl",
        "FOMM",
        "60 pctl",
        "70 pctl",
        "80 pctl",
        "90 pctl",
        "100 pctl",
    ],
}

labels = [
    ("T-FOMM (0 pctl)", (-0.5, 0, "right", "center", True)),
    ("T-FOMM (50 pctl)", (-0.0, 0.8, "center", "bottom", False)),
    ("T-FOMM (DA, daysOfWeek)", (0.01, 0.7, "right", "center", False)),
    ("FOMM", (-0.1, -0.1, "right", "center", False)),
    ("T-FOMM (HWES, node)", (0.05, 0, "left", "center", False)),
    ("T-FOMM\n(HWES, discretization)", (0.11, 0.5, "center", "bottom", False)),
    ("T-FOMM (HWES, user)", (1, -10, "center", "top", True)),
    ("T-FOMM (100 pctl)", (-1, 0, "right", "center", True)),
    ("VOMM", (1, 0, "left", "center", True)),
    ("Keep-on-closest", (1, 0, "left", "center", True)),
    ("T-FOMM (DA, months)", (0, -0.7, "left", "center", False)),
    ("T-FOMM (DA, hours)", (-0.01, 0.4, "right", "center", False)),
    ("T-FOMM (DA, daysOfWeek, 50 pctl)", (-0.01, 0.9, "right", "center", False)),
    ("T-FOMM (DA, hours, 50 pctl)", (-0.1, 0.5, "right", "bottom", False)),
    ("T-FOMM (DA, months, 50 pctl)", (-0.07, -0.5, "left", "top", False)),
]

Xs = availability
Ys = excess_data
x_desc = False
y_desc = True

sorted_list = sorted(
    [[((-1) ** x_desc) * Xs[i], ((-1) ** y_desc) * Ys[i]] for i in range(len(Xs))],
    reverse=True,
)
pareto_front = [sorted_list[0]]
for pair in sorted_list[1:]:
    if pair[1] >= pareto_front[-1][1]:
        pareto_front.append(pair)


f1, a1 = plt.subplots(figsize=(4.5, 3))

a1.scatter(Xs, Ys)

texts = []
if labels is not None:
    for i, (l, p) in enumerate(labels):
        if p[4]:
            texts.append(
                a1.annotate(
                    l,
                    xy=(Xs[i], Ys[i]),
                    arrowprops=dict(arrowstyle="-", color="black", lw=0.5),
                    xytext=(Xs[i] + p[0], Ys[i] + p[1]),
                    ha=p[2],
                    va=p[3],
                    color="black",
                    fontsize=9,
                )
            )

pf_X = [((-1) ** x_desc) * pair[0] for pair in pareto_front]
pf_Y = [((-1) ** y_desc) * pair[1] for pair in pareto_front]

a1.plot(percentiles["availability"], percentiles["excess_data"], color="red", ls="--")

a1.plot(pf_X, pf_Y)

a1.set(xlabel="% Availability", ylabel="% Excess Data")
f1.savefig("./figures/pareto.pdf", bbox_inches="tight")


sorted_list = sorted(
    [[((-1) ** x_desc) * Xs[i], ((-1) ** y_desc) * Ys[i]] for i in range(len(Xs))],
    reverse=True,
)
pareto_front = [sorted_list[0]]
for pair in sorted_list[1:]:
    if pair[1] >= pareto_front[-1][1]:
        pareto_front.append(pair)

f2, a2 = plt.subplots(figsize=(4.5, 3))

a2.scatter(Xs, Ys)

texts = []
if labels is not None:
    for i, (l, p) in enumerate(labels):
        if not p[4]:
            texts.append(
                a2.annotate(
                    l,
                    xy=(Xs[i], Ys[i]),
                    arrowprops=dict(arrowstyle="-", color="black", lw=0.5),
                    xytext=(Xs[i] + p[0], Ys[i] + p[1]),
                    ha=p[2],
                    va=p[3],
                    color="black",
                    fontsize=9,
                )
            )

pf_X = [((-1) ** x_desc) * pair[0] for pair in pareto_front]
pf_Y = [((-1) ** y_desc) * pair[1] for pair in pareto_front]

a2.plot(percentiles["availability"], percentiles["excess_data"], color="red", ls="--")

a2.plot(pf_X, pf_Y)

a2.set(xlabel="% Availability", ylabel="% Excess Data")
a2.set_xlim(72.5, 73.5)
a2.set_ylim(61, 68)

f2.savefig("./figures/pareto-zoomed.pdf", bbox_inches="tight")


# plt.show()

# debug_stop = 1
