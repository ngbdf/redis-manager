package com.newegg.ec.redis.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Everly.J.Ju
 * @date 2020/1/22
 */
public class SentinelConfigUtil {
    private static final Map<String, String> CONFIG_DESC_MAP = new ConcurrentHashMap<>();
    static {
        CONFIG_DESC_MAP.put("sentinel monitor mymaster", "Protected mode is a layer of security protection, in order to avoid that Redis instances left open on the internet are accessed and exploited");
        CONFIG_DESC_MAP.put("sentinel auth-pass mymaster", "TCP listen() backlog");
        CONFIG_DESC_MAP.put("sentinel down-after-milliseconds", "Close the connection after a client is idle for N seconds (0 to disable)");
        CONFIG_DESC_MAP.put("sentinel parallel-syncs mymaste", "A reasonable value for this option is N seconds.");
        CONFIG_DESC_MAP.put("sentinel failover-timeout", "");
        CONFIG_DESC_MAP.put("sentinel failover-timeout mymaster", "Creating a pid file is best effort: if Redis is not able to create it nothing bad happens, the server will start and run normally.");
        CONFIG_DESC_MAP.put("sentinel notification-script mymaster", "Specify the server verbosity level (debug, verbose, notice, warning).");
        CONFIG_DESC_MAP.put("logfile", "Specify the log file name.");
        CONFIG_DESC_MAP.put("sentinel client-reconfig-script mymaster", "To enable logging to the system logger, just set 'syslog-enabled' to yes, and optionally update the other syslog parameters to suit your needs.");
    }


    public static class SentinelConfig {

        private boolean enable;

        private String configKey;

        private String configValue;

        private String desc;

        public SentinelConfig() {
        }

        public SentinelConfig(String configKey, String configValue, int mode) {
            this(true, configKey, configValue, null);
        }

        public SentinelConfig(boolean enable, String configKey, String configValue, int mode) {
            this(enable, configKey, configValue, null);
        }

        public SentinelConfig(boolean enable, String configKey, String configValue, String desc) {
            this.enable = enable;
            this.configKey = configKey;
            this.configValue = configValue;
            this.desc = desc;
        }

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public String getConfigKey() {
            return configKey;
        }

        public void setConfigKey(String configKey) {
            this.configKey = configKey;
        }

        public String getConfigValue() {
            return configValue;
        }

        public void setConfigValue(String configValue) {
            this.configValue = configValue;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        @Override
        public String toString() {
            return "SentinelConfig{" +
                    "enable=" + enable +
                    ", configKey='" + configKey + '\'' +
                    ", configValue='" + configValue + '\'' +
                    ", desc='" + desc + '\'' +
                    '}';
        }
    }

}
