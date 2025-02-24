# Queue Simulation Project

## Overview

This project implements a simulation of a multi-service station queuing system in Java. The simulation models five distinct service stations with different queuing policies:  
1. **SingleQueue** – a regular single queue that serves up to 15 customers, where the service station always takes customers from the front of the queue.  
2. **RoundRobinQueue** – a queue that accepts customers in fixed batches of 5 and does not accept new customers until the current 5 are serviced.  
3. **ShortestQueue** – a queue that is prioritized if it is the shortest among all service stations.  
4. **RegularQ1 and RegularQ2** – additional queues that receive new customers randomly if the above rules do not apply.

For each queuing policy, the simulation outputs several key metrics:  
- The **duration of the simulation** (which may extend beyond the input time as waiting queues persist even after check-in closes).  
- The **maximum length of each queue** reached during the simulation.  
- The **average and maximum waiting time** for each queue.  
- The **occupancy rate** (percentage of time each service station was busy).  
- The **real-time evolution** of the queues during runtime.\


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
    
   
   