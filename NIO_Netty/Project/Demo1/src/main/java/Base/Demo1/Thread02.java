package Base.Demo1;

public class Thread02 extends Thread{
    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            synchronized (MyLock.o){
                System.out.println(2);
                MyLock.o.notify();
                try {
                    MyLock.o.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
