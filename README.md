# Solace Appender for log4j 2.14+

Hello!  This is the first cut at an Appender for log4j2.  It uses direct messaging only right now.  And the published topic is completely dynamic!  E.g.:

```
#sol-api-log/AaronsThinkPad3/12540/INFO/main/com/solacesystems/jcsmp/protocol/impl/TcpClientChannel
                       |      |    |      |    |
                  hostname/ PID /level/thread/class......
                                         name
```

So much better than crappy JMS and Kafka publishers that just publish to a fixed static topic!

I should make an MQTT version as well, since MQTT also supports awesome dynamic hierarchical topics.




