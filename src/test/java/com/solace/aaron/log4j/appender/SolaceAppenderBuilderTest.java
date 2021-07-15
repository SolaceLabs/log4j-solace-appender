
package com.solace.aaron.log4j.appender;

import com.solace.aaron.log4j.appender.SolaceAppender.Builder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SolaceAppenderBuilderTest {

    // default tests
    @Test public void testSolaceAppenderBuilderDefaultHost() {
        Builder<?> saBuilder = SolaceAppender.newBuilder();
        assertEquals("SolaceAppender.Builder should have localhost has default host","localhost",saBuilder.getHost());
    }
    
        @Test public void testSolaceAppenderBuilderDefaultVpn() {
        Builder<?> saBuilder = SolaceAppender.newBuilder();
        assertEquals("SolaceAppender.Builder should have default has default message vpn","default",saBuilder.getVpn());
    }

    
    @Test public void testSolaceAppenderBuilderDefaultUsername() {
        Builder<?> saBuilder = SolaceAppender.newBuilder();
        assertEquals("SolaceAppender.Builder should have default has default username","default",saBuilder.getUsername());
    }

    @Test public void testSolaceAppenderBuilderDefaultPassword() {
        Builder<?> saBuilder = SolaceAppender.newBuilder();
        assertEquals("SolaceAppender.Builder should have default has default password","",saBuilder.getPassword());
    }


    // set/get tests
    @Test public void testSolaceAppenderBuilderSetHost() {
        Builder<?> saBuilder = SolaceAppender.newBuilder();
        String newHost = "abc123";
        saBuilder.setHost(newHost);
        assertEquals("SolaceAppender.Builder should have "+newHost+" has new host",newHost,saBuilder.getHost());
    }
    
    @Test public void testSolaceAppenderBuilderSetVpn() {
        Builder<?> saBuilder = SolaceAppender.newBuilder();
        String newVpn = "abc123";
        saBuilder.setVpn(newVpn);
        assertEquals("SolaceAppender.Builder should have "+newVpn+" has new vpn",newVpn,saBuilder.getVpn());
    }

    @Test public void testSolaceAppenderBuilderSetUsername() {
        Builder<?> saBuilder = SolaceAppender.newBuilder();
        String newUsername = "abc123";
        saBuilder.setUsername(newUsername);
        assertEquals("SolaceAppender.Builder should have "+newUsername+" has new username",newUsername,saBuilder.getUsername());
    }

    @Test public void testSolaceAppenderBuilderSetPassword() {
        Builder<?> saBuilder = SolaceAppender.newBuilder();
        String newPassword = "abc123";
        saBuilder.setPassword(newPassword);
        assertEquals("SolaceAppender.Builder should have "+newPassword+" has new password",newPassword,saBuilder.getPassword());
    }
    

}
