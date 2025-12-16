# Distributed, Fault-Tolerant Search Engine Cluster – Stage 3

This repository contains the code and configuration files for **Stage 3** of our Big Data Architecture project. The project was developed as part of a university assignment by **Group Microsoft-2**.

In this stage, we extend the system built in previous phases and move from a mostly centralized setup to a **distributed architecture** that can run across multiple nodes and continue working even if one of them fails.

## Project Members

* Sergio Muela Santos
* Daniel Medina González
* Jorge Cubero Toribio
* Enrique Padrón Hernández

---

## Objectives

The main goals of this stage were the following:

1. **Scalability:** Allow the system to handle more data by adding more nodes instead of upgrading a single machine.
2. **Fault Tolerance:** Ensure that the system keeps working if one container or service stops unexpectedly.
3. **Asynchronous Processing:** Separate data ingestion from processing using a message broker.
4. **Data Consistency:** Keep search results consistent even when data is distributed across several nodes.

## System Architecture

The system follows a **service-oriented approach**, where each component has a specific responsibility and runs in its own container. This makes it easier to scale and debug individual parts of the system.

Each container runs a single service, which allowed us to replicate only the components that needed more capacity during testing.

### Logical Architecture

The cluster is divided into five main components:

| Layer        | Component          | Technology          | Description                                                                             |
| :----------- | :----------------- | :------------------ | :-------------------------------------------------------------------------------------- |
| Ingestion    | Crawlers           | Java, Gutendex API  | Retrieve book metadata and content from Project Gutenberg and store it in the datalake. |
| Coordination | ActiveMQ           | JMS                 | Acts as a message broker between crawlers and indexers.                                 |
| Processing   | Indexers           | Java, Hazelcast     | Consume messages, read raw files, and process text for indexing.                        |
| Storage      | Hazelcast          | In-memory data grid | Stores the distributed inverted index with replication.                                 |
| Serving      | Search API + Nginx | Java REST, Nginx    | Provides a search endpoint and load balances requests.                                  |

### Physical Architecture (Docker)

All components are deployed using **Docker Compose**, which made it easier to start and stop the entire system during development.

* A custom Docker network (`search-net`) is used so containers can communicate using service names.
* A shared named volume (`datalake-shared`) is used to simulate persistent storage for downloaded books.
* Services are configured with `restart: on-failure` to improve stability when dependencies are not ready at startup.

## Implementation Details

* **Crawler reliability:** The crawler includes a simple exponential backoff mechanism to handle API rate limits (HTTP 429 errors).
* **Message size optimization:** Instead of sending full book contents through ActiveMQ, only the book ID and file path are sent. The actual data is read from the shared volume.
* **Index structure:** Hazelcast’s `MultiMap` is used to implement the inverted index, mapping words to multiple document identifiers.

## Experiments and Results

We performed several tests by increasing the number of crawler, indexer, and search nodes to observe how the system behaves under different configurations.

| Configuration | Nodes (Crawler-Indexer-Search) | Ingestion Rate | Avg. CPU Usage |
| :-----------: | :----------------------------: | :------------: | :------------: |
|    Baseline   |              1-1-1             |  ~12 docs/min  |      ~45%      |
|     Medium    |              2-2-2             |  ~23 docs/min  |      ~60%      |
|      High     |              3-3-3             |  ~35 docs/min  |      ~78%      |

The results show that adding more nodes increases throughput, although with higher CPU usage.

### Fault Tolerance Test

To test fault tolerance, we manually stopped one of the search containers during execution:

1. Nginx stopped routing traffic to the failed container.
2. Hazelcast redistributed the data using its backup replicas.
3. Search requests continued working without losing indexed data.

---

## How to Download and Run the Project

This section explains the basic steps required to download and run the project locally using Docker.

### Step 1: Clone the repository

First, clone the repository from GitHub:

```bash
git clone https://github.com/Microsoft-2/stage-3
```

### Step 2: Move into the project directory

Once the repository has been cloned, navigate to the project folder:

```bash
cd stage-3
```

### Step 3: Start the Docker containers (PC 1)

On the first machine, start the Docker services using the corresponding Docker Compose file:

```bash
docker compose up [DOCKER-COMPOSE-FILE-PC1] --build -d
```

### Step 3.1: Start Docker on additional machines

If the project is deployed on multiple PCs, repeat the process on each machine using a different project name:

```bash
docker compose -p [DOCKER-COMPOSE-FILE-PC2-PC3-...] --build -d
```

### Step 4: Check the logs

In a new terminal, you can follow the logs of the crawler to verify that the system is running correctly:

```bash
docker logs -f stage-3-crawler-1
```

---
