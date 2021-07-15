
package com.solace.aaron.log4j2.appender;

import com.solace.aaron.log4j2.appender.SolaceAppender.Builder;
import org.junit.Test;


public class SolaceAppenderTest {

    // post-build tests
    @Test public void testSolaceAppenderName() {
        Builder<?> saBuilder = SolaceAppender.newBuilder();
//        SolaceAppender sa = saBuilder.build();  // throws NPE
    }


}
