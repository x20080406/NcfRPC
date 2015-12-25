package personal.tianjie.rpc;

/**
 * Created by tianjie on 4/15/15.
 */

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 标注接口是否为Rpc服务接口.<br>
 * 关于cache，建议不要将cache标注在RpcService实现上。让数据粒度尽量细一点。<br>
 * Created by tianjie on 4/9/15.
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface RpcService {
}
