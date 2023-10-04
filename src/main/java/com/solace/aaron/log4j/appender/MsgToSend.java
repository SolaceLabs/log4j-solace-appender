package com.solace.aaron.log4j.appender;

import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.Destination;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPSendMultipleEntry;
import com.solacesystems.jcsmp.XMLMessage;

final class MsgToSend implements JCSMPSendMultipleEntry {

	private XMLMessage msg;
	private Destination topic;
	
	MsgToSend(BytesXMLMessage msg, String topic) {
		this.msg = msg;
		this.topic = JCSMPFactory.onlyInstance().createTopic(topic);
	}
	
	@Override
	public Destination getDestination() {
		return topic;
	}
	
	@Override
	public XMLMessage getMessage() {
		return msg;
	}
	
	@Override
	public JCSMPSendMultipleEntry setDestination(Destination dest) {
		topic = dest;
		return this;
	}
	
	@Override
	public JCSMPSendMultipleEntry setMessage(XMLMessage msg) {
		this.msg = msg;
		return this;
	}
}