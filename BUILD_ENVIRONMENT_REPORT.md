# Build Environment Analysis & Java Compatibility Report

---

## Executive Summary
This report analyzes the local system build environment for **StockPulse** (Spring Boot 3 + Java 17/21). It details default environment paths, JDK vs JRE availability, `javac` presence, and configuration updates made to `pom.xml` to guarantee cross-compatibility across Java 17 and Java 21 compilers.

---

## 1. Build Environment Analysis

| Parameter | Value / Detection | Details |
| :--- | :--- | :--- |
| **1. Java Version** | `21.0.12` | OpenLogic-OpenJDK `21.0.12+8-adhoc.Administrator.jdk21u` |
| **2. JAVA_HOME** | `C:\Program Files\OpenLogic\jre-21.0.12.8-hotspot` | System default environment variable points to JRE installation |
| **3. Maven Version** | `3.9.16` | Apache Maven `3.9.16` located at `C:\Users\poweroot\Downloads\...` |
| **4. `javac` Availability** | **Not Available in System PATH** (Available in Corretto JDK 17) | Default OpenLogic path contains `java.exe` but lacks `javac.exe` |
| **5. Environment Type** | **JRE (Java Runtime Environment)** | Default environment is JRE-only; compilation requires setting JDK path (`Corretto JDK 17`) |

---

## 2. JDK vs JRE & `javac` Findings

1. **System Default State**:
   - The primary environment variable (`JAVA_HOME`) and system `PATH` point to **OpenLogic JRE 21** (`C:\Program Files\OpenLogic\jre-21.0.12.8-hotspot`).
   - Because it is a JRE (Runtime Environment), running `javac` directly against system PATH returns:
     `javac : The term 'javac' is not recognized as the name of a cmdlet, function, script file, or operable program.`
2. **Compilation Solution**:
   - Amazon Corretto JDK 17 (`C:\Users\poweroot\jdk17_temp\jdk17.0.20_12`) contains the complete Java Development Kit including `javac.exe`.
   - Compiling with `JAVA_HOME=C:\Users\poweroot\jdk17_temp\jdk17.0.20_12` resolves compilation and Maven execution cleanly (`BUILD SUCCESS`).

---

## 3. `pom.xml` Java 17 & Java 21 Compatibility Review

### Analysis
- Spring Boot `3.2.5` (`spring-boot-starter-parent`) requires Java 17 as the baseline version and supports Java 21.
- In `pom.xml`, `<properties><java.version>17</java.version></properties>` sets default target compatibility.
- Explicitly adding `<source>17</source>` and `<target>17</target>` to `maven-compiler-plugin` guarantees that whether compiled using a JDK 17 compiler or JDK 21 compiler, the output class files (bytecode version `61.0`) remain fully compatible with both Java 17 and Java 21 runtime environments.

---

## 4. Configuration Changes

### Update to `backend/pom.xml`:
Configured `maven-compiler-plugin` explicitly with `<source>17</source>` and `<target>17</target>`:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <source>17</source>
        <target>17</target>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>${lombok.version}</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

---
*Report generated for StockPulse Build Environment Verification.*
