# Solace Appender for log4j 2.14+

Hello!  This is the first cut at an Appender for log4j2.  It can use either Direct messaging or Guaranteed (but does not attempt redelivery).  And the published **topic is completely dynamic!**  E.g.:

```
log4j-log/AaronsThinkPad3-12540/INFO/pub-thread/com/solacesystems/jcsmp/protocol/impl/TcpClientChannel
                     |      |    |      |         |-->
                  hostname-PID/level/thread-name/class......
                 (or app name)
```                  

So much better than crappy JMS and Kafka publishers that just publish to a fixed static topic!

I should make an MQTT version as well, since MQTT also supports awesome dynamic hierarchical topics.



## Options

For example, in your log4j2.xml file, specify a `Solace` Appender, something like:

```
...
  <Appenders>
    <Solace name="solaceLogger" host="192.168.42.35" vpn="logs" username="test" password="secret">
      <PatternLayout>
        <Pattern>%d %p %c{1.} [%t] %m%n</Pattern>
      </PatternLayout>
      <!--JSONLayout/-->
    </Solace>
  </Appenders>
  <Loggers>
    <Root level="info">
      <AppenderRef ref="solaceLogger"/>
    </Root>
  </Loggers>
...
```

- `host`: hostname or IP address of the Solace broker (default="`localhost`")
- `vpn`: Message VPN to connect to  (default="`default`")
- `username`: Client username to connect with  (default="`default`")
- `password`: Password for client connection, if required (default="")
- `direct`: ["true"|"false"] Whether to use Direct or Guaranteed messages (default="`false`")
- `appName`: Custom string to use instead of "hostname+pid" in topic

