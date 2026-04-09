package methodhiding;

public class Car extends Vehicle{

    public static void staticMethod() {
        System.out.println("Car's method");
    }

    @Override
    public void overrideMethod() {
        System.out.println("Car's override method");
    }
}
