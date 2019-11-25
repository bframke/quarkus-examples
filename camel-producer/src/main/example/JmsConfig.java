package example;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.quarkus.runtime.annotations.RegisterForReflection;

@ConfigRoot(
    name = "ibmmq.connectionFactory",
    phase = ConfigPhase.RUN_TIME
)
@RegisterForReflection
public class JmsConfig {
    @ConfigItem(
        name = "hostName",
        defaultValue = "localhost"
    )
    String hostName;
    @ConfigItem(
        name = "port",
        defaultValue = "7654"
    )
    int port;
    @ConfigItem(
        name = "userName",
        defaultValue = ""
    )
    String userName;
    @ConfigItem(
        name = "password",
        defaultValue = ""
    )
    String password;
    @ConfigItem(
        name = "queueManager",
        defaultValue = "qmgr1"
    )
    String queueManager;
    @ConfigItem(
        name = "channel",
        defaultValue = "CAL"
    )
    String channel;
    @ConfigItem(
        name = "ssl",
        defaultValue = "false"
    )
    boolean ssl;
    @ConfigItem(
        name = "ciphersuite",
        defaultValue = ""
    )
    String ciphersuite;
    @ConfigItem(
        name = "sslPeerName",
        defaultValue = ""
    )
    String sslPeerName;
    @ConfigItem(
        name = "trustStorePath",
        defaultValue = ""
    )
    String trustStorePath;
    @ConfigItem(
        name = "trustStorePassword",
        defaultValue = ""
    )
    String trustStorePassword;
    @ConfigItem(
        name = "keyStorePath",
        defaultValue = ""
    )
    String keyStorePath;
    @ConfigItem(
        name = "keyStorePassword",
        defaultValue = ""
    )
    String keyStorePassword;

    public JmsConfig() {
    }

    public String getHostName() {
        return this.hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getQueueManager() {
        return this.queueManager;
    }

    public void setQueueManager(String queueManager) {
        this.queueManager = queueManager;
    }

    public String getChannel() {
        return this.channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public boolean isSsl() {
        return this.ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public String getCiphersuite() {
        return this.ciphersuite;
    }

    public void setCiphersuite(String ciphersuite) {
        this.ciphersuite = ciphersuite;
    }

    public String getSslPeerName() {
        return this.sslPeerName;
    }

    public void setSslPeerName(String sslPeerName) {
        this.sslPeerName = sslPeerName;
    }

    public String getTrustStorePath() {
        return this.trustStorePath;
    }

    public void setTrustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
    }

    public String getTrustStorePassword() {
        return this.trustStorePassword;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    public String getKeyStorePath() {
        return this.keyStorePath;
    }

    public void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
    }

    public String getKeyStorePassword() {
        return this.keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
