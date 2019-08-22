package RPC_Demo.Server;

public class HelloNettyImpl implements HelloNetty {
    @Override
    public String hello() {
        return "hello netty";
    }
}
