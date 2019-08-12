package Base.Demo2;

public class MainTest {
    public static void main(String[] args) {
        Child child = new Child();
        Farmer farmer = new Farmer();
        child.start();
        farmer.start();
    }
}
