package test;

import org.springframework.shantaomvc.type.ControllerMethod;
import org.springframework.shantaomvc.type.RequestPathInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Test {

    @org.junit.jupiter.api.Test
    public void test() {
        Map<RequestPathInfo, ControllerMethod> map = new ConcurrentHashMap<>();
        map.put(new RequestPathInfo("get","/1/1"),new ControllerMethod(null,null,null));
        System.out.println(map.containsKey(new RequestPathInfo("get","/1/1")));
    }

}