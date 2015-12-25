package personal.tianjie.rpc.server;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by tianjie on 4/4/15.
 */
public class NcfServer {
    private static Logger LOGGER = LoggerFactory
            .getLogger(NcfServer.class);

    /**
     *
     * 启动方法.
     *
     * @param args 参数
     */
    public static void main(String... args) {
        NcfServerConfig config = new Cli(args).parse();
        if (config == null) {
            System.exit(-1);
        }
        new NcfServerBootstrap(config).start();
    }

    /**
     *
     * 处理命令行参数.
     * 处理启动参数.支持help,port,worker设置.
     */
    static class Cli {
        private String[] args = null;
        private Options options = new Options();

        /**
         *
         * 启动参数处.
         *
         * @param args
         */
        public Cli(String[] args) {
            this.args = args;
            options.addOption("h", "help", false,
                    "show help.");
            options.addOption("p", "port", true,
                    "specify the port for the server listen to");
            options.addOption("w", "worker", true,
                    "specify the count of thread to process client request");
        }

        /**
         *
         * 格式化.
         *
         * @return 处理结果
         */
        public NcfServerConfig parse() {
            CommandLineParser parser = new BasicParser();
            CommandLine cmd;
            NcfServerConfig config = new NcfServerConfig();
            try {
                cmd = parser.parse(options, args);
                if (cmd.hasOption("h")) {
                    help();
                    return null;
                }

                if (cmd.hasOption("p")) {
                    Object val = cmd.getOptionValue("p");
                    config.setPort(Integer.valueOf(val.toString()));
                    LOGGER.info("服务端监听端口为:{}", config.getPort());
                }

                if (cmd.hasOption("w")) {
                    Object val = cmd.getOptionValue("w");
                    config.setEvtExecutorSize(Integer.valueOf(val.toString()));
                    LOGGER.info("工作线程数将指定为:{}", config.getEvtExecutorSize());
                }

                return config;
            } catch (Exception e) {
                help();
                return null;
            }
        }

        /**
         *
         * 打印帮助信息.
         */
        private void help() {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("NcfServer", options);
        }
    }
}
