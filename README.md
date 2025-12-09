# RestAssured MockMvc Jakarta/Spring 7 Compatibility Matrix

This repository provides a minimal, reproducible example (MRE) to demonstrate the **fundamental binary incompatibility** between RestAssured MockMvc (HEAD of master / 5.5.7-SNAPSHOT) and the Jakarta-based Spring Framework 7 / Spring Boot 4.

The tests run two distinct scenarios using the **exact same test code** (`TC01MockMvcHeaderCrashTest.java`), proving that the failure is purely caused by the classpath and dependency configuration.

### 1. Prerequisites & Environment Setup

To successfully reproduce the issue, you must separate the **Build Environment** from the **Test Execution Environment**.

| Component | Required JDK | Usage Scope |
| :--- | :--- | :--- |
| **RestAssured Core** | **JDK 17** (Strict) | **Build Phase:** You must compile the local `HEAD` snapshot using JDK 17 to ensure binary compatibility with the project's current source level. |
| **Matrix Script** | **JDK 25** (or 17+) | **Execution Phase:** The `matrix.sh` script can be run with JDK 25 to simulate the future environment (Spring Boot 4 target), or JDK 17. Both will reproduce the crash. |

#### Step 1: Build RestAssured Core (The "Whole Ox")

First, clone and build the latest RestAssured version to install the snapshots locally.

⚠️ **Critical:** Switch to **JDK 17** for this step.

```bash
# 1. Activate JDK 17 (Use your installed version, e.g., 17.0.11-tem)
sdk use java <your-java-17-version> 

# 2. Clone the official repository
git clone https://github.com/rest-assured/rest-assured.git
cd rest-assured

# 3. Clean and build the full reactor project
mvn clean install -DskipTests
```

#### Step 2: Prepare for Execution

After building the core, you can switch to JDK 25 (optional, but recommended for full simulation) to run the matrix.

```bash
# Activate JDK 25 - Target Runtime for Spring Boot 4. (Use your installed version, e.g., 25.0.1-open)
sdk use java <your-java-25-version>

```
### 2. Running the Compatibility Matrix
Clone this reproduction repository and execute the driver script.

Execution Steps
1. **Clone the Repository:**
```bash
git clone https://github.com/spencerarq/ra-compat-matrix.git
cd ra-compat-matrix
```
2. **Grant Execution Permission:** 
Apply execution permissions to the script (required for Linux/WSL/macOS environments):
    ```bash
    chmod +x matrix.sh
    ```

3. **Execute the Matrix:** Run the command from the root of the `ra-compat-matrix` directory:
    ```bash
    ./matrix.sh
    ```

### 3. Scenario Results

| Scenario | Configuration | Expected Outcome | Actual Result (Check Logs) |
| :--- | :--- | :--- | :--- |
| **[1] Positive Case** | Spring Boot **2.7.18** (`javax`) + RA HEAD | **PASS** (Validates test harness reliability) | `positive.log` should show `BUILD SUCCESS` |
| **[2] Negative Case** | Spring Boot **4.0.0-SNAPSHOT** (`jakarta`) + RA HEAD | **FAIL** (Must detect Binary Incompatibility) | `negative.log` should show `NoClassDefFoundError` |

### 4. Root Cause (javax vs jakarta)
The consistent failure in the Negative Case is immediately triggered by a:
```bash
java.lang.NoClassDefFoundError: javax/servlet/http/HttpServlet
```
This is the definitive evidence that the RestAssured
    ```
    spring-mock-mvc
    ```
module requires the 
    ```
    javax 
    ```
servlet package, which has been removed/relocated to the 
    ```
    jakarta
    ```
namespace in Spring Framework 6/7. This is a critical blocker for adoption of Spring Boot 4.