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

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;

import com.solace.aaron.log4j.appender.SolaceManager.SolaceManagerConfig;

@Plugin(name = "Solace", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)

public class SolaceAppender extends AbstractAppender {
    
    enum PublishMode {
            DIRECT,
            GUAR_NO_REPUB,
//            GUAR_REPUB_NO_ORDER,  // not supported yet
//            GUAR_STRICT_ORDER,  // not supported yet
            ;
    }
    
    public static class Builder<B extends Builder<B>> extends AbstractAppender.Builder<B> implements org.apache.logging.log4j.core.util.Builder<SolaceAppender> {

        @PluginBuilderAttribute
        @Required(message = "No host provided for Solace PubSub+ appender")
        private String host = "localhost";
        
        @PluginBuilderAttribute
        private String vpn = "default";
        
        @PluginBuilderAttribute
        private String username = "default";
        
        @PluginBuilderAttribute(sensitive = true)
        private String password = "";
        
//        @PluginBuilderAttribute
//        private String topicFormat = "host/%s";

        @PluginBuilderAttribute
        private Boolean direct = false;
        
        @PluginBuilderAttribute
        private String appName = "";
//        {
//        	try {
//				appName = InetAddress.getLocalHost().getHostName() + "-";
//			} catch (UnknownHostException e) {
//				appName = "DefaultApp";
//			}
//        }
        

        // Programmatic access only for now.   ---- //  what does that mean???  copied from JMS appender
        private SolaceManager solaceManager = null;
        
        // secret constructor
        private Builder() {
            super();
        }
        
        @Override
        public SolaceAppender build() {
        	if (SolaceManager.DEBUG) System.out.println("SOLACE APPENDER BUILDER BUILD() has been called");
            Configuration config = getConfiguration();  // should get initialized by log4j framework, might not for Tests
            if (config != null) {
            	if (SolaceManager.DEBUG) System.out.println("config properties: "+config.getProperties());
            	if (SolaceManager.DEBUG) System.out.println("config context: "+config.getLoggerContext());
            } else {
            	if (SolaceManager.DEBUG) System.out.println("config is null");
            }
            if (SolaceManager.DEBUG) System.out.println("host is : "+host);
            if (SolaceManager.DEBUG) System.out.println("vpn is : "+vpn);
            if (SolaceManager.DEBUG) System.out.println("username is : "+username);
            if (SolaceManager.DEBUG) System.out.println("password is "+ (password==null ? "null": password.isEmpty() ? "unset" : "set"));
//            System.out.println("topicFormat is : "+topicFormat);
            if (SolaceManager.DEBUG) System.out.println("direct is : "+direct);
            
            SolaceManagerConfig solaceConfig = new SolaceManagerConfig();
            solaceConfig.setHost(host);
            solaceConfig.setVpn(vpn);
            solaceConfig.setUsername(username);
            solaceConfig.setPassword(password);
//            solaceConfig.setTopicFormat(topicFormat);
            solaceConfig.setSendMode(direct ? PublishMode.DIRECT : PublishMode.GUAR_NO_REPUB);
            solaceConfig.setAppName(appName);
            if (config != null) solaceConfig.setContext(config.getLoggerContext());
            if (SolaceManager.DEBUG) System.out.println(solaceConfig.toString());

            //final Layout<? extends Serializable> layout = getLayout();
            //if (layout == null) {
            //    AbstractLifeCycle.LOGGER.error("No layout provided for SolaceAppender");
            //    return null;
            //}
            SolaceManager actualSolaceManager = solaceManager;
            //SolaceManagerConfig configuration = null;
            if (actualSolaceManager == null) {
//                actualSolaceManager = AbstractManager.getManager(getName(), SolaceManager.FACTORY, configuration);
                //actualSolaceManager = SolaceManager.getManager(loggerContext, getName(), SolaceManager.FACTORY, configuration);
                actualSolaceManager = SolaceManager.getManager(getName(),SolaceManager.FACTORY,solaceConfig);
            }
            if (actualSolaceManager == null) {
                // is it possible for getManager() to return null??
                // JmsManagerFactory has already logged an ERROR.
                return null;
            }
//            if (getLayout() == null) {
//                LOGGER.error("No layout provided for SolaceAppender");
//                return null;
//            }
            try {
                return new SolaceAppender(getName(), getFilter(), getLayout(), isIgnoreExceptions(), getPropertyArray(), actualSolaceManager);
            } catch (final Throwable e) {
                //  Never happens since the ctor no longer actually throws a JMSException.
                throw new IllegalStateException(e);
            }
        }
        
        // these are not actually required by the appender, only for unit tests
        public String getHost() {
            return host;
        }
        
        public B setHost(final String host) {
            this.host = host;
            return asBuilder();
        }
        
        public String getVpn() {
            return vpn;
        }
        
        public B setVpn(final String vpn) {
            this.vpn = vpn;
            return asBuilder();
        }
        
        public String getUsername() {
            return username;
        }
        
        public B setUsername(final String username) {
            this.username = username;
            return asBuilder();
        }
        
        public String getPassword() {
            return password;
        }

        public B setPassword(final String password) {
            this.password = password;
            return asBuilder();
        }
    
//        public B setTopicFormatter(final String topicFormat) {
//            this.topicFormat = topicFormat;
//            return asBuilder();
//       
//        }
    
        
//        public B setImmediateFail(final boolean immediateFail) {
//            this.immediateFail = immediateFail;
//            return asBuilder();
//        }
        
//        public B setSolaceManager(final SolaceManager solaceManager) {
//            this.solaceManager = solaceManager;
//            return asBuilder();
//        }
        
    }
    
    
    
    // MAIN CLASS //////////////////////////////////////////////////////////////
    
    @PluginBuilderFactory
    public static <B extends Builder<B>> B newBuilder() {
        if (SolaceManager.DEBUG) System.out.println("******* SolaceAppdneder.newBuilder() called");
        return new SolaceAppender.Builder<B>().asBuilder();
    }
    
    SolaceManager manager = null;  // who is my manager?  Should only be one..?

    // the actual constructor!  Called by Builder.build() above
    protected SolaceAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions, Property[] properties, SolaceManager manager) {
        super(name, filter, layout, ignoreExceptions, properties);
        if (SolaceManager.DEBUG) System.out.println("STDOUT SOLACE APPENDER constructor");
        //this.manager = manager;
        this.manager = Objects.requireNonNull(manager, "manager");
        // anything else here???
    }

    @Override
    public void append(final LogEvent event) {
    	if (SolaceManager.DEBUG) System.out.println("APPEND CALLED");
    	boolean success = this.manager.enqueueForBatchSend(event, toSerializable(event));
    	if (!success) {
    		if (SolaceManager.DEBUG) System.out.println("Could not enqueue log msg to send on Solace, max send buffer capacity reached");
    	}
//        try {
//            // if layout is null, toSerializable() will return null
//            this.manager.send(event, toSerializable(event));
//        } catch (JCSMPException e) {
//            e.printStackTrace();
//        }
    }
      
    public SolaceManager getManager() {
        if (SolaceManager.DEBUG) System.out.println("SolaceAPpender.getManager() called");
        return manager;
    }

    @Override
    public boolean stop(final long timeout, final TimeUnit timeUnit) {
        setStopping();
        boolean stopped = super.stop(timeout, timeUnit, false);
        stopped &= this.manager.stop(timeout, timeUnit);
        setStopped();
        return stopped;
    }



}

