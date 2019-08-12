package Base.Demo2;

public class Farmer extends Thread{
    @Override
    public void run() {
        //农夫的工作是不断放水果
        while (true){
            synchronized (Kuang.kuang){
                //如果框内水果超过10，农夫休息
                if(Kuang.kuang.size() == 10){
                    try {
                        Kuang.kuang.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //放置水果
                Kuang.kuang.add("fruit");
                System.out.println("农夫放进了一个水果，现在框内还有" + Kuang.kuang.size() + "个水果");
                Kuang.kuang.notify();
            }

            //控制速度
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
