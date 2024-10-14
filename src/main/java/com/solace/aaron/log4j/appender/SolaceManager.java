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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractManager;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.impl.ThrowableProxy;

import com.solace.aaron.log4j.appender.SolaceAppender.PublishMode;
import com.solacesystems.common.util.ByteArray;
import com.solacesystems.jcsmp.BytesMessage;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.DeliveryMode;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.JCSMPStreamingPublishCorrelatingEventHandler;
import com.solacesystems.jcsmp.MapMessage;
import com.solacesystems.jcsmp.SDTException;
import com.solacesystems.jcsmp.SDTMap;
import com.solacesystems.jcsmp.TextMessage;
import com.solacesystems.jcsmp.XMLMessageProducer;

public class SolaceManager extends AbstractManager {
	
	static final boolean DEBUG = false;

    public static class SolaceManagerConfig {
        
        String host = "localhost";
        String vpn = "default";
        String username = "default";
        String password = "";
//        String topicFormat = "";
//        boolean sendDirect = false;
        PublishMode sendMode = PublishMode.DIRECT;
        String appName = "";
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

        public PublishMode getSendMode() {
            return sendMode;
        }
        
        public void setSendMode(PublishMode sendMode) {
            this.sendMode = sendMode;
        }
        
        public void setAppName(String appName) {
        	this.appName = appName;
        }
        
        public LoggerContext getContext() {
            return context;
        }
        
        public void setContext(LoggerContext context) {
            this.context = context;
        }
        
        public JCSMPProperties getJcsmpProperties() {
//            if (DEBUG) System.out.println("default context: "+JCSMPFactory.onlyInstance().getDefaultContext().toString());
//            if (DEBUG) System.out.println(Arrays.toString(com.solacesystems.jcsmp.secure.SecureProperties.SupportedJSSECipherNamesArray));
        	if (DEBUG) System.out.println("HEllo inside getJcsmpProperties()");
            // this is fixed now, in new JCSMP
//            for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
//                System.out.printf("%s %s %d%n",e.getClassName(),e.getMethodName(),e.getLineNumber());
//            }
            
            JCSMPProperties properties = new JCSMPProperties();
//            if (DEBUG) System.out.println(properties.getProperty(JCSMPProperties.SSL_CIPHER_SUITES));
//            JCSMPFactory.onlyInstance().getDefaultContext();
//            properties = new JCSMPProperties();
//            if (DEBUG) System.out.println(properties.getProperty(JCSMPProperties.SSL_CIPHER_SUITES));
            
            properties.setProperty(JCSMPProperties.HOST,host);          // host:port
            properties.setProperty(JCSMPProperties.VPN_NAME,vpn);     // message-vpn
            properties.setProperty(JCSMPProperties.USERNAME,username);      // client-username
            properties.setProperty(JCSMPProperties.PASSWORD,password);  // client-password
            properties.setProperty(JCSMPProperties.PUB_ACK_WINDOW_SIZE, 255);  // client-password
            if (DEBUG) System.out.println("Solace properties:"+properties.toString());
            return properties;
        }

        @Override
        public String toString() {
            return "SolaceManagerConfig: "+host+", "+vpn+", "+username+"; sendMode="+ sendMode;
        }
    }  // end of inner SolaceManagerConfig class 

    
    
    
    // ManagerFactory for the SolaceManager, helper class to create managers
    private static class SolaceManagerFactory implements ManagerFactory<SolaceManager, SolaceManagerConfig> {

        @Override
        public SolaceManager createManager(final String name, final SolaceManagerConfig config) {
        	if (DEBUG) System.out.println("SolaceManagerFactory.createManager() called");
            try {
                return new SolaceManager(name, config);
            } catch (final Exception e) {
                e.printStackTrace();
                if (DEBUG) System.err.println("AAron was here, this is an error! " + e.toString());
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
//        if (DEBUG) System.out.printlnSystem.out.println("************** SOMEONE CALLED THI SMOTHODHODHFOS DFHOSDFHOSHDFOHSD");
//        final SolaceManagerConfig config = new SolaceManagerConfig();
//        return getManager(name,FACTORY,config);
//    }

    // static accessor method
    public static SolaceManager getManager(final String name, final SolaceManagerConfig config) {
    	if (DEBUG) System.out.println("******* MANAGER.GETMANAGER() static");
        //final SolaceManagerConfig config = new SolaceManagerConfig();
        return getManager(name,FACTORY,config);  // this is the super one?
    }

    private final SolaceManagerConfig config;
    private final JCSMPSession session;
    private final XMLMessageProducer producer;
    final String hostnameOrIp;
    final String pid;
    final ScheduledExecutorService msgSendThreadPool = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory("SolaceSender"));
    final LinkedBlockingQueue<MsgToSend> msgsToSend = new LinkedBlockingQueue<>(10000);
    
    protected SolaceManager(String name, final SolaceManagerConfig config) throws JCSMPException {
        super(config.getContext(),name);
        if (DEBUG) System.out.println("STARTING SOLACE<MANAGER CONSTRUCTOR");
        this.config = config;
        if (DEBUG) System.out.println(this.config);
        JCSMPFactory f = JCSMPFactory.onlyInstance();
        if (DEBUG) System.out.println("Factory is " + f);
        JCSMPProperties props = this.config.getJcsmpProperties();
        if (DEBUG) System.out.println("props is " + props.toString().substring(0,200));
        session = JCSMPFactory.onlyInstance().createSession(this.config.getJcsmpProperties());
//        if (DEBUG) System.out.println("I HAVE SUCCESSFULLY CREATED A SESSION");
        String[] clientNameLevels = ((String)session.getProperty(JCSMPProperties.CLIENT_NAME)).split("/");
        this.hostnameOrIp = clientNameLevels[0];
        this.pid = clientNameLevels[1];
        session.setProperty(JCSMPProperties.APPLICATION_DESCRIPTION, "log4j Solace Appender publisher");
        session.setProperty(JCSMPProperties.CLIENT_NAME, "log4j_"+session.getProperty(JCSMPProperties.CLIENT_NAME));
        logger().info("Attempting to start connecting to Solace broker " + session.getProperty(JCSMPProperties.HOST));
        session.connect();
        if (DEBUG) System.out.println("I HAVE CONNECTED!");
//        if (DEBUG) System.out.println(LOGGER.hashCode());
//        if (DEBUG) System.out.println(logger().hashCode());  // these are the same!
        producer = session.getMessageProducer(new JCSMPStreamingPublishCorrelatingEventHandler() {  // need to implement something here if we want to do Guaranteed
            @Override
            public void responseReceivedEx(Object key) {
            	// good ACK, correct
            }
            
            @Override
            public void handleErrorEx(Object key, JCSMPException cause, long timestamp) {
                // silent discard of NACKs and ACL violations
            	// for now
            }
        });
        msgSendThreadPool.submit(() -> {  // this is the sender loop thread!
			List<MsgToSend> msgs = new ArrayList<>();
			MsgToSend[] array = new MsgToSend[50];
			int count = 0;
        	while (true) {  // daemon thread, so will get shut down eventually
        		if (producer.isClosed()) return;
        		try {
        			msgs.clear();
        			count = msgsToSend.drainTo(msgs, 50);
        			if (count == 0) {  // nothing to send, so pause for 1ms
        				Thread.sleep(1);
        			} else {
        				if (DEBUG) System.out.println("ABOUT TO SEND BATCH OF " + count);
	        			array = msgs.toArray(array);
	        			producer.sendMultiple(array, 0, count, 0);
        			}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JCSMPException e) {
					e.printStackTrace();
					System.out.println("Solace log4j sender had a problem: " + e.toString());
				}
        	}
		});
    }

    private BytesXMLMessage buildMessage(final LogEvent event, final Serializable serializable) {
    	BytesXMLMessage msg;
    	if (serializable == null) msg = buildTextMessage(event, event.getMessage().getFormattedMessage());
    	else if (serializable instanceof CharSequence) msg = buildTextMessage(event, serializable.toString().trim());
    	else if (serializable instanceof org.apache.logging.log4j.message.MapMessage) msg = buildMapMessage(event, (org.apache.logging.log4j.message.MapMessage<?,?>)serializable);
    	else msg = buildBytesObjectMessage(event, serializable);
        if (this.config.sendMode != PublishMode.DIRECT) msg.setDeliveryMode(DeliveryMode.PERSISTENT);
    	addMessageMetadata(msg, event, serializable);
    	return msg;
        // topic will look like 'log4j-log/hostname/pid/[INFO|WARN|etc.]/thread-name/com/whatever/blah/classname'
    }
    
    private BytesXMLMessage addMessageMetadata(BytesXMLMessage msg, final LogEvent event, final Serializable serializable) {
        msg.setSenderTimestamp(event.getTimeMillis());
        SDTMap map = JCSMPFactory.onlyInstance().createMap();
        try {
			map.putString("threadName", event.getThreadName());
			map.putLong("threadId", event.getThreadId());
			map.putInteger("threadPriority", event.getThreadPriority());
			map.putLong("timestampMillis", event.getTimeMillis());
//			if (event.getSource() != null) {
//				map.putString("method", event.getSource().getMethodName());
//				map.putInteger("lineNumber", event.getSource().getLineNumber());
//				map.putString("stackTraceElement", event.getSource().toString());
//			}
////			map.putString("contextStack", event.getContextStack().asList().toString());
//			event.get
	        map.putString("loggerName", event.getLoggerName());
	        map.putString("level", event.getLevel().name());
	        map.putString("message", event.getMessage().getFormattedMessage());
	        if (event.getThrownProxy() != null) {
	            ThrowableProxy thrown = event.getThrownProxy();
	            map.putString("thrownName",thrown.getName());
	        }
	        msg.setProperties(map);
		} catch (SDTException e) {
			// shouldn't happen
		}
        return msg;
    }
    
    private TextMessage buildTextMessage(final LogEvent event, final String logEntry) {
    	if (DEBUG) System.out.println("MAKING A TEXT MESAGE");
        TextMessage msg = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
        msg.setText(logEntry);
        return msg;
    }

    private BytesMessage buildBytesObjectMessage(final LogEvent event, final Serializable serializable) {
        BytesMessage msg = JCSMPFactory.onlyInstance().createMessage(BytesMessage.class);
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(bos)) {
               out.writeObject(serializable);
               msg.setData(bos.toByteArray());
        } catch (IOException e) {
        	// handle it!
        }
        return msg;
    }

    private MapMessage buildMapMessage(final LogEvent event, final org.apache.logging.log4j.message.MapMessage<?, ?> log4jMapMessage) {
        MapMessage msg = JCSMPFactory.onlyInstance().createMessage(MapMessage.class);
        SDTMap map = msg.getMap();
        // copied from https://github.com/apache/logging-log4j2/blob/2.x/log4j-core/src/main/java/org/apache/logging/log4j/core/appender/mom/JmsManager.java
        log4jMapMessage.forEach((key, value) -> {
        	try {
	        	if (value instanceof String) map.putString(key, (String)value);
	        	else if (value instanceof Double) map.putDouble(key, (Double)value);
	        	else if (value instanceof Float) map.putFloat(key, (Float)value);
	        	else if (value instanceof Long) map.putLong(key, (Long)value);
	        	else if (value instanceof Integer) map.putInteger(key, (Integer)value);
	        	else if (value instanceof Short) map.putShort(key, (Short)value);
	        	else if (value instanceof Byte) map.putByte(key, (Byte)value);
	        	else if (value instanceof Boolean) map.putBoolean(key, (Boolean)value);
	        	else if (value instanceof Character) map.putCharacter(key, (Character)value);
	        	else if (value instanceof ByteArray) map.putByteArray(key, (ByteArray)value);
	        	else map.putObject(key, value);
            } catch (final SDTException e) {
                throw new IllegalArgumentException(
                        String.format(
                                "%s mapping key '%s' to value '%s': %s",
                                e.getClass(), key, value, e.getLocalizedMessage()),
                        e);
            }
        });
        return msg;
    }

    
    private String buildTopic(final LogEvent event, final Serializable serializable) {
        // topic will look like 'log4j-log/hostname/pid/[INFO|WARN|etc.]/thread-name/com/whatever/blah/classname'
        String threadNameNoSlash = event.getThreadName().replaceAll("/","|");
        // for the topic of this message, if it's an exception being thrown, let's use the name of the exception in the topic instead
        String classNameTopic = (event.getThrownProxy() != null ? event.getThrownProxy().getName() : event.getLoggerName()).replaceAll("/","|").replaceAll("\\.","/");
        String topic = String.format("log4j%s/%s/%s/%s/%s",
                (event.getThrownProxy() != null ? "-e" : "-log"),
                config.appName.isEmpty() ? hostnameOrIp + "-" + pid : config.appName.replaceAll("/","|"),
                event.getLevel().toString(),
                threadNameNoSlash,
                classNameTopic);  // this last one could have multiple topic levels
        return topic;
    }
    
    boolean enqueueForBatchSend(final LogEvent event, final Serializable serializable) {
    	BytesXMLMessage msg = buildMessage(event, serializable);
    	String topic = buildTopic(event, serializable);
    	if (DEBUG) System.out.println("ENQUEUE FOR BATCH SEND");
    	boolean success = msgsToSend.offer(new MsgToSend(msg, topic));
    	return success;
    }
    
    void send(final LogEvent event, final Serializable serializable) throws JCSMPException {
        //if (DEBUG) System.out.println("SENDING::>> "+serializable.toString() + "\n"+event.getSource().toString()+"\n"+event.getThreadName());
    	BytesXMLMessage msg = buildMessage(event, serializable);
    	String topic = buildTopic(event, serializable);
    	
//        if ("test" != "test") {
//            topic += "/"+event.getMessage().getFormattedMessage();
//            topic = topic.substring(0, Math.min(topic.length(), 250));
//        }
//        System.out.println("SENDING::>> "+topic);
        producer.send(msg,JCSMPFactory.onlyInstance().createTopic(topic));
    }
    
    @Override
    protected boolean releaseSub(final long timeout, final TimeUnit timeUnit) {
        session.closeSession();
        return true;
    }


}
