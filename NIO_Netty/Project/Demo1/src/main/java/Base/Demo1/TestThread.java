package Base.Demo1;

public class TestThread {
    public static void main(String[] args) {
        Thread01 thread01 = new Thread01();
        Thread02 thread02 = new Thread02();
        thread01.start();
        thread02.start();
    }
}
