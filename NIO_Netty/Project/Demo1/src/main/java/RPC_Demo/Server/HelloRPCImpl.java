package RPC_Demo.Server;

public class HelloRPCImpl implements HelloRPC {
    @Override
    public String hello(String name) {
        return "hello " + name;
    }
}
