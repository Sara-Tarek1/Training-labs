# Lab 2: Building and Packaging a Calculator Application with Gradle

## Step 1: Install Gradle

Install Java JDK 17 and verify the installation:

```bash
sudo apt update
sudo apt install openjdk-17-jdk
java -version
```

Install Gradle and verify the installation:

```bash
sudo apt install gradle
gradle --version
```

## Step 2: Clone the Source Code

Clone the Calculator Gradle application repository:

```bash
git clone https://github.com/Ibrahim-Adel15/CalculatorGradleApp.git
cd CalculatorGradleApp
```

## Step 3: Run Unit Tests

Execute the unit tests to verify the application code:

```bash
gradle test
```

Verify that the build completes successfully.

## Step 4: Build and Package the Application

Build the application and generate the JAR artifact:

```bash
gradle build
```

Verify that the JAR file exists:

```bash
ls -l build/libs/calculator.jar
```

Expected artifact:

`build/libs/calculator.jar`

## Step 5: Run the Application

Run the Calculator application:

```bash
gradle run
```

Enter two numbers when prompted by the application.

## Step 6: Verify the Application

Test the application using the inputs `10` and `20`.

Expected output:

```text
Sum: 30.0
Subtract: -10.0
Multiply: 200.0
Divide: 0.5
```

The application is working correctly when it displays the expected calculation results and the execution completes successfully.
successfully. BUILD SUCCESSFUL in 1s
3 actionable tasks: 3 up-to-date
sara@DESKTOP-VDHI02P:~/lab2$ gradle build

BUILD SUCCESSFUL in 1s
7 actionable tasks: 7 up-to-date
sara@DESKTOP-VDHI02P:~/lab2$ gradle run

> Task :run
Enter first number:
<=<=========----> 75Enter second number:
<=<=========----> 75% EXECUTING [11s]    a = 10.0, b = 20.0
Sum: 30.0
Subtract: -10.0
Multiply: 200.0
Divide: 0.5

BUILD SUCCESSFUL in 12s


