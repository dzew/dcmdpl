# Dawit's Continuous MDP Library

DCMDPL is a collection of some popular reinforcement learning problems
along with implementations of select continuous MDP solvers.
DCMDPL is designed for modularity, allowing for implementations of learners
and problem instances to be swapped in and out with minimal overhead.

This repository contains all relevant documents for DCMDPL. That includes:
implementation, write-up, and experiment results. 
This document provides a high level overview of the contents, structure,
usage, and future of DCMDPL.


## Contents
The DCMDPL code has four main parts: **domains**, **learners**, **math,** and **utilities**. 

The **domains** are implementations of MDPs. DCMDPL enforces modularity of MDPs
through an API. DCMDPL comes with implementations of Acrobot, Mountain-Car,
and PinBall that conform to this API.

The **learners** are implementations of continuous MDP solvers.
DCMDPL provides implementations of LSTD, KBRL, KBSF, and DKBRL.

The **math** module contains implementations of all non-trivial mathematical
operations; this keeps the code in the rest of the project readable.

The rest of DCMDPL is collectively called **utility**. Utility includes
visualization tools, tests, and experiments.

## Project Structure
The project hierarchy is as follows

    drl
    |
    |-drl.data                     \\ Tools to collect or display data.
    |  | 
    |  |-drl.data.print            \\ Print raw data to file or console.
    |  |
    |  |-drl.data.vis              \\ Visualize data through a GUI.
    | 
    |-drl.experiments              \\ Experiments in the thesis and report.
    |
    |-drl.math                     \\ Collection of mathematical tools.
    |  |
    |  |-drl.math.algs             \\ Miscellaneous algorithms.
    |  |
    |  |-drl.math.geom             \\ Geometry.
    |  |
    |  |-drl.math.tfs              \\ Transforming the state space.
    |  |
    |  |-drl.math.vfa              \\ Value function approximation.
    |
    |-drl.mdp                      \\ Implementations of MDPs.
    |  |
    |  |-drl.mdp.api               \\ The API for MDPs.
    |  |
    |  |-drl.mdp.instance          \\ Implementations of specific domains.
    |  |   |
    |  |
    |  |-drl.mdp.utils             \\ Functionality common to all MDPs.   
    |
    |-drl.solver                   \\ Implementations of MDP solvers.
    |  |
    |  |-drl.solver.leastsquares   \\ Solvers that use least-squares.
    |  |
    |  |-drl.solver.smoothing      \\ Solvers that use local averaging.
    |
    |-drl.tests                    \\ Tests.
    |  |
    |  |-drl.tests.functional      \\ Tests to see how well features work.
    |  |
    |  |-drl.tests.unit            \\ Tests to verify correct implementation.
    |
    |-drl.tutorial                 \\ Sample code showing how to use DCMDPL.

## Usage
For a step by step guide through DCMDPL, work through the tutorial in
drl.tutorial. For exemplary code on how to use some particular tool or
feature look inside drl.tests.functional.

## License
DCMDPL is released under the GNU General Public License Version 3.0

DCMDPL comes with the following third party software:

|    Software     | License                                  |
|-----------------|------------------------------------------|
|EJML 0.23        |  LGPL                                    |
|JUnit 4.7        |  IBM's Common Public License Version 0.5 |
|Acrobot**        |  Apache 2.0                              |
|Mountain-Car**   |  Apache 2.0                              |
|PinBall Domain** |  GPL 3.0                                 |

**The code distributed in DCMDPL contains substantial modifications to
the original code. 

The complete text of each license is available in the licenses directory.

## Future Work
DCMDPL is no longer under active development. No new features or extensions
have been planned. If you notice any bugs, please let us know so we may
post a work-around. 
