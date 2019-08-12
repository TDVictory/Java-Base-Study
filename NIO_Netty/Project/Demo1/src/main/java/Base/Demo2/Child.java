package Base.Demo2;

public class Child extends Thread {
    @Override
    public void run() {
        //小孩的工作是不断吃水果
        while (true){
            synchronized (Kuang.kuang){
                //如果框内水果等于0，小孩休息
                if(Kuang.kuang.size() == 0){
                    try {
                        Kuang.kuang.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //吃水果
                Kuang.kuang.remove("fruit");
                System.out.println("小孩吃了一个水果，现在框内还有" + Kuang.kuang.size() + "个水果");
                Kuang.kuang.notify();
            }

            //控制速度
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
