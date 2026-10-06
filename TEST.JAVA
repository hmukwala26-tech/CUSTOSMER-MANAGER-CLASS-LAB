public class Calculator {
    int add(int a, int b) {
        return a + b;
    }

    double average(int total, int count) {
        return (double) total / count;
    }
}

Calculator c = new Calculator();
System.out.println(c.add(4, 6));