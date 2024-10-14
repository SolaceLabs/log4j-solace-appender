# Solace Appender for log4j 2.14+

This is a Solace PubSub+ Appender for log4j2.  It can use either Direct messaging, or Guaranteed (but does not attempt redelivery if a NACK is received).  The published **topic is completely dynamic!**  E.g.:

```
log4j-log/AaronsThinkPad3-12540/INFO/pub-thread/com/solacesystems/jcsmp/protocol/impl/TcpClientChannel
                     |      |    |      |         |-->
                 hostname-PID/level/thread-name/class......
                 (or app name)
```                  

So much better than crappy JMS and Kafka publishers that just publish to a fixed static topic, like "logs" or "systemXYZ.logs"!  Same with most of the MQTT ones I've found online.

This multi-level topic structure allows you to do things like:

- Subscribe to all WARNs: `log4j*/*/WARN/>`
- Subscribe to logs from Aaron's machines: `log4j*/Aaron*/>`
- Subscribe to any log from the main thread: `log4j*/*/*/main/>`
- Subscribe to any exception: `log4j-exc/>`

I should make a better MQTT version as well, since MQTT also supports awesome dynamic hierarchical topics. Although MQTT doesn't support prefix-wildcards.



## Options

As an example, in your log4j2.xml file, specify a `Solace` Appender, something like:

```
...
  <Appenders>
    <Solace name="solaceLogger" host="tcp://192.168.42.35" vpn="logs" username="test" password="secret" appName="${sys:appName:DefaultAppName}">
      <PatternLayout>
        <Pattern>%d %p %c{1.} [%t] %m%n</Pattern>
      </PatternLayout>
    </Solace>
  </Appenders>
  <Loggers>
    <Root level="info">
      <AppenderRef ref="solaceLogger"/>
    </Root>
  </Loggers>
...
```

- `host`: hostname or IP address of the Solace broker (default="`tcp://localhost:55555`")
- `vpn`: Message VPN to connect to  (default="`default`")
- `username`: Client username to connect with  (default="`default`")
- `password`: Password for client connection, if required (default="")
- `direct`: ["true"|"false"] Whether to use Direct or Guaranteed messages (default="`false`")
- `appName`: Custom string to use instead of "hostname+pid" in topic



## Shadow MegaJAR bundle

Has JCSMP 10.24.1 bundled inside, which includes a ton of netty files.
