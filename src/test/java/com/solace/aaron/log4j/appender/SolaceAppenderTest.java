
package com.solace.aaron.log4j.appender;

import com.solace.aaron.log4j.appender.SolaceAppender;
import com.solace.aaron.log4j.appender.SolaceAppender.Builder;
import org.junit.Test;


public class SolaceAppenderTest {

    // post-build tests
    @Test public void testSolaceAppenderName() {
        Builder<?> saBuilder = SolaceAppender.newBuilder();
//        SolaceAppender sa = saBuilder.build();  // throws NPE
    }


}
