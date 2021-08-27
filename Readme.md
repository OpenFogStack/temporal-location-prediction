# Predictive Replica Placement in FogStore - A Simulation Framework

---

## Structure:

- geolife-data: location of the raw Geolife Data
- geolife-data-transformed: populated by the `me.mbe.prp.TransformGeolife` main method in the test module
- src
    - main: code of the framework, algorithms, etc.
        - algorithms
        - base: helper methods
        - core: framework
        - data
        - metrics
        - network
        - nodes
    - test: code for the evaluation
- stats-out: evaluation results saved here

---

## Setup:

- Clone repository
- Setup gradle
- Copy the [Geolife Data][1] into the geolife-data folder.
- Run the `me.mbe.prp.TransformGeolife` main method in the test module
- Run the evaluations in `me.mbe.prp.geolife.Evaluation`
- Results can be found in the `stats-out` directory

---

## Algorithms:

- Baseline
  - `me.mbe.algorithms.Alg000`: Store data on all nodes at all times: Section 5.1 of the thesis
  - `me.mbe.algorithms.Alg001`: Store data only on closest node when application active: Section 5.1 of the thesis
- Next Node Prediction
  - `me.mbe.algorithms.nextnodepred.Alg003`: (Multi Order) Markov Model: Sections 5.2.1 and 5.2.2 of the thesis
  - `me.mbe.algorithms.nextnodepred.Alg004`: Variable Order Markov Model: Section 5.2.3 of the thesis
  - `me.mbe.algorithms.nextnodepred.Alg008`: Store also on some neighboring nodes: Not mentioned in the thesis
  - `me.mbe.algorithms.nextnodepred.Alg012`: Fusion Multi Order Markov Model: Section 5.2.4 of the thesis
- Startup Prediction
  - `me.mbe.algorithms.startuppred.Alg011`: Store for short pauses: Section 5.3.1 of the thesis
  - `me.mbe.algorithms.startuppred.Alg013`: Do not store anything after shutdown: Used together with the algorithms for next node prediction: Not mentioned explicitly in the thesis
  - `me.mbe.algorithms.startuppred.Alg014`: Store if short pause predicted: Section 5.3.2 of the thesis
  - `me.mbe.algorithms.startuppred.Alg015`: Clustering of startup times for long pauses: Not mentioned explicitly in the thesis, just as a side note in section 7.2.3 and in chapters 8 and 10

[1]: https://www.microsoft.com/en-us/download/details.aspx?id=52367