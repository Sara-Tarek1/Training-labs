Lab 1: Building a Java Application with Maven

1. Objective
Learn how to build, test, and package a Java Calculator application using Apache Maven.

 2. Prerequisites
- Java JDK 17
- Apache Maven
- Git

Check the installed versions:

```bash
java -version
mvn -version
git --version
```

3. Clone the Source Project
Clone the original project from GitHub:

```bash
git clone https://github.com/Ibrahim-Adel15/CalculatorMavenApp.git
cd CalculatorMavenApp
```

 4. Run Unit Tests

```bash
mvn test
```

 5. Build and Package the Application

```bash
mvn package
```

The generated JAR file is:

```text
target/calculator.jar
```

 6. Run the Application

```bash
java -jar target/calculator.jar
```

Follow the prompts in the terminal to enter the requested numbers.

 7. Technologies Used
- Java 17
- Apache Maven
- JUnit Jupiter 5.10.2

 8. Expected Outcome
- Unit tests execute successfully.
- The application is packaged as `calculator.jar`.
- The application starts from the generated JAR file.

output:
1/zstd-jni-1.5.5-11.jar (6.8 MB at 816 kB/s)
[INFO] Building jar: /home/sara/Training-labs/Build Tools/Lab1/target/calculator.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  26.316 s
[INFO] Finished at: 2026-10-20T17:39:22+03:00
[INFO] ------------------------------------------------------------------------
sara@DESKTOP-VDHI02P:~/Training-labs/Build Tools/Lab1$ java -jar target/calculator.jar
Enter first number: 5
Enter second number: 5
a = 5.0, b = 5.0
Sum: 10.0
Subtract: 0.0
Multiply: 25.0
Divide: 1.0