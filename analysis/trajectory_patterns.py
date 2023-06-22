import datetime
import pickle
import matplotlib as mpl
import matplotlib.pyplot as plt
import pandas as pd
import numpy as np

# import os
# import itertools
# import glob
# import datetime

# import numpy as np
# import pandas as pd
# import seaborn as sns
# import matplotlib as mpl
# import matplotlib.ticker
# import matplotlib.pyplot as plt


# sns.set(font_scale=0.9, style='whitegrid', font='CMU Sans Serif')
# pal = sns.color_palette(['#4477AA', '#EE6677', '#228833', '#CCBB44', '#66CCEE', '#AA3377', '#BBBBBB'])
# sns.set_palette(pal)

mpl.rcParams["pdf.fonttype"] = 42
mpl.rcParams["ps.fonttype"] = 42
mpl.rcParams["figure.figsize"] = (4.5, 3)
mpl.rcParams["figure.dpi"] = 100
plt.rcParams["font.family"] = "sans-serif"
plt.rcParams["font.sans-serif"] = ["CMU Sans Serif"]

from nodeGrid import Location, getNodeGrid

node_grid = getNodeGrid(100)

user = "000"

with open(f"geolife_data_transformed/{user}.pkl", "rb") as f:
    data = pickle.load(f)

training_data = dict()
training_data_all = list()
test_data = dict()
node_change_total_time = 0.0
node_id_oldest = None
training_lenght = int(0.75 * len(data["locations"]))
for i in range(1, len(data["locations"]) - 1):
    duration: datetime.timedelta = (
        data["locations"][i + 1]["time"] - data["locations"][i]["time"]
    )
    if duration.total_seconds() > 180:
        continue
    node_id_old = node_grid.getClosestNodeID(
        Location(data["locations"][i]["lat"], float(data["locations"][i]["long"]))
    )
    node_id_new = node_grid.getClosestNodeID(
        Location(
            data["locations"][i + 1]["lat"], float(data["locations"][i + 1]["long"])
        )
    )
    if node_id_old != node_id_new:
        if node_id_oldest is not None:
            training_data_all.append(
                {
                    "starting_date": data["locations"][i]["time"],
                    "duration": float(node_change_total_time),
                }
            )
            id = node_id_oldest + "," + node_id_old
            if id not in training_data:
                training_data[id] = []
            if id not in test_data:
                test_data[id] = []
            # training_data[id].append({'duration': node_change_total_time, 'starting_date': data["locations"][i]["time"].hour * 60 + data["locations"][i]["time"].second})
            # day_of_the_week = data["locations"][i]["time"].toordinal()%7
            if i < training_lenght:
                training_data[id].append(
                    {
                        "duration": node_change_total_time,
                        "starting_date": data["locations"][i]["time"],
                    }
                )
            else:
                test_data[id].append(
                    {
                        "duration": node_change_total_time,
                        "starting_date": data["locations"][i]["time"],
                    }
                )
        node_change_total_time = 0
        node_id_oldest = node_id_old
    else:
        node_change_total_time += duration.total_seconds()

df: pd.DataFrame = pd.DataFrame(
    training_data_all, columns=["starting_date", "duration"]
)
timeseries = df["duration"]

# Show the final plot
rolmean = pd.Series(timeseries).rolling(window=30).mean()
# plt.figure(figsize=(8, 4.8))
plt.plot(timeseries, color="lightskyblue", label="Raw Trajectory Data")
plt.plot(rolmean, label="Rolling Mean (x30)")
av = sum(timeseries) / len(timeseries)
plt.axhline(y=av, color="r", label="Arithmetic Mean")
plt.ylabel("Duration (s)")
plt.xlabel("Time (s)")
plt.legend(fontsize=10, title_fontsize=15)
plt.rcParams["font.size"] = "60"
plt.yticks(np.arange(0, 50000, 1000))
plt.xticks(np.arange(0, 700, 50))
print(timeseries.max())
plt.xlim(301, 680)
plt.ylim(0, 5000)
plt.savefig("./figures/trajectory_pattern.pdf", bbox_inches="tight")
# plt.show()
# debug_stop = 1
