package personal.tianjie.rpc;


import personal.tianjie.rpc.util.SystemUtil;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by tianjie on 4/4/15.
 */
public class Request extends Message {

    private static AtomicLong idGenerator = new AtomicLong(0);
    /**
     * id
     */
    private long id = idGenerator.incrementAndGet();
    //目标接口
    private String targetInterface;
    //目标方法
    private String methodName;
    //目标接口参数类型
    private String[] parameterTypeNames;
    //参数
    private Object[] args;

    public long getId() {
        return id;
    }

    public String getTargetInterface() {
        return targetInterface;
    }

    public void setTargetInterface(String targetInterface) {
        this.targetInterface = targetInterface;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public String[] getParameterTypeNames() {
        return parameterTypeNames;
    }

    public void setParameterTypeNames(String[] parameterTypeNames) {
        this.parameterTypeNames = parameterTypeNames;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("request id:");
        stringBuilder.append(getId());
        stringBuilder.append(",targetInterface:");
        stringBuilder.append(targetInterface);
        stringBuilder.append(",methodName:")
                .append(methodName)
                .append(",args:")
                .append(Arrays.toString(args));

        if (args == null || args.length == 0) {
            return stringBuilder.toString();
        }

        for (Object arg : args) {
            stringBuilder
                    .append(",")
                    .append(Arrays.toString(
                            SystemUtil.getClassFullName(arg.getClass())));
        }

        return stringBuilder.toString();
    }
}
