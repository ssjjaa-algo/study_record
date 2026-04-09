package methodhiding;

public class Main {

    public static void main(String[] args) {

        Vehicle v = new Car();
        v.overrideMethod();
        v.staticMethod();
    }
}
