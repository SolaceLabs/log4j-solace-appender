package com.solace.aaron.log4j2.appender;

import com.solace.aaron.log4j2.appender.SolaceManager.SolaceManagerConfig;
import com.solacesystems.jcsmp.JCSMPException;
import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;

@Plugin(name = "SolaceAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)

public class SolaceAppender extends AbstractAppender {
    
    
    
    public static class Builder<B extends Builder<B>> extends AbstractAppender.Builder<B> implements org.apache.logging.log4j.core.util.Builder<SolaceAppender> {

        public static final int DEFAULT_RECONNECT_INTERVAL_MILLIS = 5000;
        
        @PluginBuilderAttribute
//        @Required
        private String host = "localhost";
        
        @PluginBuilderAttribute
        private String vpn = "aaron";
        
        @PluginBuilderAttribute
        private String username = "aaron";
        
        
        @PluginBuilderAttribute(sensitive = true)
        private String password = "aaron";
        
        @PluginBuilderAttribute
        private String topicFormat = "host/%s";
        
        @PluginBuilderAttribute
        private boolean immediateFail = true;
        
        // Programmatic access only for now.   ---- //  what does that mean???  copied from JMS appender
        private SolaceManager solaceManager = null;
        
        private Builder() {
        }
        
        @SuppressWarnings("resource") // actualJmsManager and jndiManager are managed by the JmsAppender
        @Override
        public SolaceAppender build() {
            final LoggerContext loggerContext = getConfiguration().getLoggerContext();

            //final Layout<? extends Serializable> layout = getLayout();
            //if (layout == null) {
            //    AbstractLifeCycle.LOGGER.error("No layout provided for SolaceAppender");
            //    return null;
            //}
            SolaceManager actualSolaceManager = solaceManager;
            SolaceManagerConfig configuration = null;
            if (actualSolaceManager == null) {
//                actualSolaceManager = AbstractManager.getManager(getName(), SolaceManager.FACTORY, configuration);
                //actualSolaceManager = SolaceManager.getManager(loggerContext, getName(), SolaceManager.FACTORY, configuration);
                actualSolaceManager = SolaceManager.getManager(loggerContext, getName(), configuration);
            }
            if (actualSolaceManager == null) {
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
        

        public B setHost(final String host) {
            this.host = host;
            return asBuilder();
        }
        
        public B setVpn(final String vpn) {
            this.vpn = vpn;
            return asBuilder();
        }
        
        public B setUsername(final String username) {
            this.username = username;
            return asBuilder();
        }
        
        public B setPassword(final String password) {
            this.password = password;
            return asBuilder();
       
        }
    
        public B setTopicFormatter(final String topicFormat) {
            this.topicFormat = topicFormat;
            return asBuilder();
       
        }
    
        
        public B setImmediateFail(final boolean immediateFail) {
            this.immediateFail = immediateFail;
            return asBuilder();
        }
        
        public B setSolaceManager(final SolaceManager solaceManager) {
            this.solaceManager = solaceManager;
            return asBuilder();
        }
        
    }     
        
    
    @PluginBuilderFactory
    public static <B extends Builder<B>> B newBuilder() {
        return new Builder<B>().asBuilder();
    }
    
    
    
    SolaceManager manager = null;  // who is my manager?  Should only be one..?
    

    // the actual constructor!
    protected SolaceAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions, Property[] properties, SolaceManager manager) {
        super(name, filter, layout, ignoreExceptions, properties);
        //this.manager = manager;
        this.manager = Objects.requireNonNull(manager, "manager");
        // anything else here???
        System.out.println("STDOUT SOLACE APPENDER STARTIMNG");
        //LOGGER.fatal("SOLACE APPENDER STARTIMNG");
        
        //LOGGER.fatal(layout.toString());

    }

    



//          @PluginFactory
//          public static SolaceAppender createAppender(
//            @PluginAttribute("name") String name, 
//            @PluginElement("Filter") Filter filter) {
//              return new SolaceAppender(name, filter);
//          }

    @Override
    public void append(final LogEvent event) {
        try {
            this.manager.send(event, toSerializable(event));
        } catch (JCSMPException e) {
            e.printStackTrace();
        }
    }
      
    public SolaceManager getManager() {
        return manager;
    }



    @Override
    public boolean stop(final long timeout, final TimeUnit timeUnit) {
        //setStopping();
        boolean stopped = super.stop(timeout, timeUnit, false);
        stopped &= this.manager.stop(timeout, timeUnit);
        //setStopped();
        return stopped;
    }



}

