# Queue Simulation Project

## Overview

This project implements a simulation of a multi-service station queuing system in Java. The simulation features five distinct service stations with different queuing policies: a single queue that serves up to 15 customers, a round robin queue that processes customers in batches of five, a shortest queue that accepts customers when it is the smallest among all stations, and two additional regular queues that receive customers based on overall system load. The simulation dynamically displays the next quickest service station—calculated based on estimated waiting times—and tracks how frequently each station is selected as the “next quickest” option. Users interact with the simulation via the console, choosing to continue rounds, view the most efficient station, or exit the program.\
This simulation program models a multi-service station queuing system where customers are assigned to different service stations based on predefined rules. The system consists of five service stations: a single queue that processes up to 15 customers sequentially, a round robin queue that accepts exactly 5 customers in each batch, a shortest queue that is prioritized when its size is the smallest among all stations, and two additional regular queues that receive customers based on overall system load. New customers are generated randomly each round, and the system dynamically calculates and displays estimated waiting times. Additionally, the program tracks the frequency with which each station is selected as the “next quickest” option, offering insights into the efficiency of the various queue assignment strategies.\
The purpose of this simulation is to serve as an experiment tool for studying queue management algorithms and to simulate a real life station service efficiency. By simulating random customer arrivals and variable service durations, the program enables users to observe how different assignment rules affect overall system performance. This simulation not only demonstrates the practical application of data structures and algorithms in a real-world scenario but also provides a platform for experimenting with various queuing policies to determine which approaches yield the most efficient service under different load conditions.

## Installation

1. **Prerequisites:**  
   Ensure that you have Java JDK (version 8 or higher) installed on your machine.

2. **Clone the Repository:**  
   ```bash
   git clone <repository_url>
   cd <repository_directory>

## Usage
1. **To run the simulation:**  
   Execute: java QueueSimulation

2. **Follow the Instruction of the Simulation:**  
    
   
   