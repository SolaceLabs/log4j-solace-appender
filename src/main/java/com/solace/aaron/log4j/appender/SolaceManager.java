/*
 * Copyright 2021 Solace Corporation. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.solace.aaron.log4j.appender;

import com.solacesystems.jcsmp.DeliveryMode;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.JCSMPStreamingPublishCorrelatingEventHandler;
import com.solacesystems.jcsmp.SDTMap;
import com.solacesystems.jcsmp.TextMessage;
import com.solacesystems.jcsmp.XMLMessageProducer;
import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractManager;
import org.apache.logging.log4j.core.appender.ManagerFactory;

public class SolaceManager extends AbstractManager {

    public static class SolaceManagerConfig {
        
        String host = "localhost";
        String vpn = "default";
        String username = "default";
        String password = "";
//        String topicFormat = "";
        Boolean direct = false;
        LoggerContext context = null;
        
        public void setHost(String host) {
            this.host = host;
        }
        
        public String getHost() {
            return host;
        }

        public String getVpn() {
            return vpn;
        }

        public void setVpn(String vpn) {
            this.vpn = vpn;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
        
//        public String getTopicFormat() {
//            return topicFormat;
//        }
//        
//        public void setTopicFormat(String topicFormat) {
//            this.topicFormat = topicFormat;
//        }

        public boolean getDirect() {
            return direct;
        }
        
        public void setDirect(Boolean direct) {
            this.direct = direct;
        }
        
        public LoggerContext getContext() {
            return context;
        }
        
        public void setContext(LoggerContext context) {
            this.context = context;
        }
        
        public JCSMPProperties getJcsmpProperties() {
            System.out.println("default context: "+JCSMPFactory.onlyInstance().getDefaultContext().toString());
            System.out.println(Arrays.toString(com.solacesystems.jcsmp.secure.SecureProperties.SupportedJSSECipherNamesArray));
            
            for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
                System.out.printf("%s %s %d%n",e.getClassName(),e.getMethodName(),e.getLineNumber());
            }
            
            JCSMPProperties properties = new JCSMPProperties();
            System.out.println(properties.getProperty(JCSMPProperties.SSL_CIPHER_SUITES));
            JCSMPFactory.onlyInstance().getDefaultContext();
            properties = new JCSMPProperties();
            System.out.println(properties.getProperty(JCSMPProperties.SSL_CIPHER_SUITES));
            
            properties.setProperty(JCSMPProperties.HOST,host);          // host:port
            properties.setProperty(JCSMPProperties.VPN_NAME,vpn);     // message-vpn
            properties.setProperty(JCSMPProperties.USERNAME,username);      // client-username
            properties.setProperty(JCSMPProperties.PASSWORD,password);  // client-password
            LOGGER.debug("Solace properties:"+properties.toString());
            return properties;
        }

        @Override
        public String toString() {
            return "SolaceMnaagerConfig: "+host+", "+vpn+", "+username+"; useDirect="+direct;
        }
    }

    
    
    
    // ManagerFactory for the SolaceManager, helper class to create managers
    private static class SolaceManagerFactory implements ManagerFactory<SolaceManager, SolaceManagerConfig> {

        @Override
        public SolaceManager createManager(final String name, final SolaceManagerConfig config) {
            System.out.println("SolaceManagerFactory.createManager() called");
            try {
                return new SolaceManager(name, config);
            } catch (final Exception e) {
                e.printStackTrace();
                System.err.println("AAron was here");
                //System.err.println()
                //LOGGER.error("Error creating JmsManager using JmsManagerConfiguration [{}]");//, data, e);
                logger().error("Error creating SolaceManager using SolaceManagerConfig [{}]", config, e);
                return null;
            }
        }
    };
    
    
    
    // BEGIN SolaceManager class ///////////////////////////////////////////////////////////////////
    
    // singleton factory for building SolaceManagers
    static final SolaceManagerFactory FACTORY = new SolaceManagerFactory();
    
    // static accessor method
//    public static SolaceManager getManager(final String name, final JCSMPProperties properties) {
//        System.out.println("************** SOMEONE CALLED THI SMOTHODHODHFOS DFHOSDFHOSHDFOHSD");
//        final SolaceManagerConfig config = new SolaceManagerConfig();
//        return getManager(name,FACTORY,config);
//    }

    // static accessor method
    public static SolaceManager getManager(final String name, final SolaceManagerConfig config) {
        System.out.println("******* MANAGER.GETMANAGER() static");
        //final SolaceManagerConfig config = new SolaceManagerConfig();
        return getManager(name,FACTORY,config);  // this is the super one?
    }

    private final SolaceManagerConfig config;
    private final JCSMPSession session;
    private final XMLMessageProducer producer;
    final String hostnameOrIp;
    final String pid;
    
    protected SolaceManager(String name, final SolaceManagerConfig config) throws JCSMPException {
        super(config.getContext(),name);
        System.err.println("STARTING SOLACE<MANAGER CONSTRUCTOR");
        this.config = config;
        System.out.println(this.config);
        
        session = JCSMPFactory.onlyInstance().createSession(this.config.getJcsmpProperties());
        System.out.println("I HAVE SUCCESSFULLY CREATED A SESSION");
        String[] clientNameLevels = ((String)session.getProperty(JCSMPProperties.CLIENT_NAME)).split("\\/");
        this.hostnameOrIp = clientNameLevels[0];
        this.pid = clientNameLevels[1];
        session.setProperty(JCSMPProperties.APPLICATION_DESCRIPTION, "log4j Solace appender publisher");
        session.setProperty(JCSMPProperties.CLIENT_NAME, "log4j_"+session.getProperty(JCSMPProperties.CLIENT_NAME));
        session.connect();
        producer = session.getMessageProducer(new JCSMPStreamingPublishCorrelatingEventHandler() {  // need to implement something here if we want to do Guaranteed
            
            @Override
            public void responseReceivedEx(Object key) {
            }
            
            @Override
            public void handleErrorEx(Object key, JCSMPException cause, long timestamp) {
                // silent discard of NACKs and ACL violations

            
            }
        });
        
    }
    
    public void send(final LogEvent event, final Serializable serializable) throws JCSMPException {
        //System.out.println("SENDING::>> "+serializable.toString() + "\n"+event.getSource().toString()+"\n"+event.getThreadName());
        TextMessage msg = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
        msg.setText(serializable != null ? serializable.toString() : event.getMessage().getFormattedMessage());
        if (!this.config.direct) msg.setDeliveryMode(DeliveryMode.PERSISTENT);
        // topic will look like 'log4j-log/hostname/pid/[INFO|WARN|etc.]/thread-name/com/whatever/blah/classname'
        msg.setSenderTimestamp(event.getTimeMillis());
        String threadNameNoSlash = event.getThreadName().replaceAll("/","|");
        // for the topic of this message, if it's an exception being thrown, let's use the name of the exception in the topic instead
        String classNameTopic = (event.getThrownProxy() != null ? event.getThrownProxy().getName() : event.getLoggerName()).replaceAll("\\.","/");
        String topic = String.format("log4j-%s/%s/%s/%s/%s/%s",
                (event.getThrownProxy() != null ? "error" : "log"),
                hostnameOrIp,
                pid,
                event.getLevel(),
                threadNameNoSlash,
                classNameTopic);  // this last one could have multiple topic levels
        if ("test" == "test") {
            topic += "/"+event.getMessage().getFormattedMessage();
            topic = topic.substring(0, Math.min(topic.length(), 250));
        }
        SDTMap map = JCSMPFactory.onlyInstance().createMap();
        map.putString("threadName", event.getThreadName());
        map.putString("className", event.getLoggerFqcn());
        map.putString("level", event.getLevel().name());
        map.putString("message", event.getMessage().getFormattedMessage());
        msg.setProperties(map);
        System.out.println("SENDING::>> "+topic);
        producer.send(msg,JCSMPFactory.onlyInstance().createTopic(topic));
    }
    
    @Override
    protected boolean releaseSub(final long timeout, final TimeUnit timeUnit) {
        session.closeSession();
        return true;
    }


}
