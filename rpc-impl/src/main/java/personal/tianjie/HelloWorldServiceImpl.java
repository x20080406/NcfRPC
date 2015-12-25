package personal.tianjie;

import org.springframework.stereotype.Component;

/**
 * Created by tianjie on 7/19/15.
 */
@Component
public class HelloWorldServiceImpl implements HelloWorldService {
    public String sayHello(String name) {
        return String.format("helloworld,%s", name);
    }

    public String sayHello(String name,byte[] data) {
        return String.format("helloworld,%s,%s", name,new String(data));
    }
}
