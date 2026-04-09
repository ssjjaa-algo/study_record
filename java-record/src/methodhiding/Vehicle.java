package methodhiding;

public class Vehicle {

    public static void staticMethod() {
        System.out.println("Vehicle's method");
    }

    public void overrideMethod() {
        System.out.println("Vehicle's override method");
    }
}
