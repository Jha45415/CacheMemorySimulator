# Cache Memory Simulator

A JavaFX-based desktop application designed to visualize and demonstrate the behavior of different computer memory cache architectures, including Direct-Mapped, Set-Associative, and a full L1-L2-L3 Multi-Level hierarchy.

This project was developed as an educational tool to make abstract computer architecture concepts tangible and easy to understand through real-time simulation and visualization.

---

## 📸 Demo

![Cache Simulator Screenshot](screenshot.png)

*A quick look at the simulator processing memory addresses in Multi-Level Cache mode.*

---

## ✨ Key Features

*   **Multiple Cache Architectures:**
    *   **Direct-Mapped Cache:** The simplest cache design where each memory address maps to a single cache block.
    *   **N-Way Set-Associative Cache:** A flexible design where addresses map to a *set* of blocks (supports 2-Way, 4-Way, etc.).
    *   **Multi-Level Cache Hierarchy:** A realistic simulation of a modern L1 → L2 → L3 cache system.
*   **Real-Time Visualization:**
    *   **Live Statistics:** Instantly updated Hit/Miss counts and Hit Ratio.
    *   **Dynamic Pie Chart:** A color-coded (Green for Hit, Red for Miss) visual representation of cache performance.
    *   **Detailed Cache Tables:** See the state of each cache block, including its `Valid` bit, `Tag`, and `Index`. Tables for L1, L2, and L3 are displayed dynamically.
*   **Interactive Simulation:**
    *   **Manual & File Input:** Input memory addresses one by one or load a batch of addresses from a `.txt` file.
    *   **Hex & Decimal Support:** Accepts addresses in both standard decimal (e.g., `42`) and hexadecimal (e.g., `0x2A`) formats.
    *   **Output Logging:** Save the detailed simulation log (hits, misses, promotions) to a text file for analysis.
*   **LRU Eviction Policy:**
    *   The Set-Associative and Multi-Level caches use the **Least Recently Used (LRU)** policy to decide which block to evict when a set is full.

---

## 🛠️ Built With

*   **Core Logic:** Java 17
*   **GUI Framework:** JavaFX 17
*   **Build Tool:** Apache Maven
*   **IDE:** IntelliJ IDEA

---

## 🚀 Getting Started

Follow these instructions to get a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

You will need the following software installed on your machine:
*   Java Development Kit (JDK) 17 or later.
*   Apache Maven

### Installation & Running

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/Jha45415/CacheMemorySimulator.git
    ```

2.  **Navigate to the project directory:**
    ```bash
    cd CacheMemorySimulator
    ```

3.  **Build the project using Maven:**
    This command will compile the source code and package it into a runnable JAR file.
    ```bash
    mvn clean package
    ```

4.  **Run the application:**
    The runnable JAR will be created in the `target` directory.
    ```bash
    java -jar target/CacheMemorySimulator-1.0-SNAPSHOT.jar 
    ```

---

## 📖 How to Use

1.  **Select a Cache Type:** Use the dropdown menu to choose between "Direct-Mapped", "Set-Associative", or "Multi-Level Cache".
2.  **Input Memory Addresses:**
    *   Type addresses directly into the text area. Each address can be separated by a new line or a space.
    *   Alternatively, click "Browse File" to load addresses from a `.txt` file.
3.  **Run Simulation:** Click the **"Simulate"** button.
4.  **Analyze Results:**
    *   Observe the **Pie Chart** and **Statistics** on the right for a high-level performance summary.
    *   Examine the **Cache Tables** below to see the detailed state of each cache block.
    *   Read the **Output Log** for a step-by-step report of each memory access.
5.  **Reset:** Click the **"Reset"** button to clear all cache states and statistics.

---

## 📂 Project Structure

*   `MainUI.java`: The entry point of the application; sets up the JavaFX user interface and handles user events.
*   `CacheInterface.java`: A Java interface defining the common methods (`access`, `reset`) that all cache types must implement.
*   `DirectMappedCache.java`: The implementation of a direct-mapped cache.
*   `SetAssociativeCache.java`: The implementation of a set-associative cache, including the LRU logic using a `Deque`.
*   `MultiLevelCache.java`: The class that orchestrates the L1-L2-L3 hierarchy, handling data promotion.
*   `CacheBlock.java`: A simple data class representing a single block in the cache.

---

## 📄 License

This project is licensed under the MIT License.

---

## 🙏 Acknowledgments & References

*   **Computer Organization and Design** by Patterson & Hennessy.
*   **Josh Gracie's Cache Simulator Article**.
*   The **Oracle JavaFX Documentation**.
