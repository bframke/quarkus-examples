package example;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;

public class ArtemisConnectionFactory implements ConnectionFactory {
    private ConnectionFactory _wrapped;
    private String _url;

    @Override
    public Connection createConnection() throws JMSException {
        initFactoryIfNeeded();
        return _wrapped.createConnection();
    }

    @Override
    public Connection createConnection(String userName, String password) throws JMSException {
        initFactoryIfNeeded();
        return _wrapped.createConnection(userName,password);
    }

    @Override
    public JMSContext createContext() {
        initFactoryIfNeeded();
        return _wrapped.createContext();
    }

    @Override
    public JMSContext createContext(int sessionMode) {
        initFactoryIfNeeded();
        return _wrapped.createContext(sessionMode);
    }

    @Override
    public JMSContext createContext(String userName, String password) {
        initFactoryIfNeeded();
        return _wrapped.createContext(userName, password);
    }

    @Override
    public JMSContext createContext(String userName, String password, int sessionMode) {
        initFactoryIfNeeded();
        return _wrapped.createContext(userName, password, sessionMode);
    }

    public String getUrl() {
        initFactoryIfNeeded();
        return _url;
    }

    public void setUrl(String _url) {
        initFactoryIfNeeded();
        _url = _url;
    }





    private synchronized void initFactoryIfNeeded() {
        if (_wrapped == null) {
            _wrapped = new ActiveMQConnectionFactory(_url);
        }
    }
}

