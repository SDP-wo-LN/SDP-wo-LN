# Usage

## Installation Instructions

1. **Download the Project Files:** 
   Start by downloading the files for this project from the repository.

2. **Set up the Classpath for commons-math3-3.6.1.jar:**
   Ensure that the classpath is set for `commons-math3-3.6.1.jar`. This is necessary for the project to run correctly.

## Instructions for Running the Code

To run the BD-Shuffle algorithm, please execute the `BDMainData.java` file. For running the UDS-Shuffle algorithm, you should execute the `UDSMainData.java` file.

### Configuration Settings:
- **Epsilon Value:** Set the desired value for epsilon in the `epsilon_servers` variable.
- **Delta Value:** The delta value can be configured in the `targetDeltas` variable.

### Using Custom Datasets:
By default, the repository includes sample datasets used in our paper. If you wish to use your own dataset, such as "rfid.txt", simply set the `dataNames` variable to "rfid".

### Sample execution:
- Compile: javac -classpath ./src src/BD/BDMain.java
- Execution: java -classpath ./bin BD.BDMain

# Execution Environment
We used Rocky Linux 8.6 and Java 1.8.0.362, and Windows 11 Pro and Java 18.

# External Libraries used in our source code.
- [Apache Commons Math](https://commons.apache.org/proper/commons-math/) is distributed under the [Apache License 2.0].
