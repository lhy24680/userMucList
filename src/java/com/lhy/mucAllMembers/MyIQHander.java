package com.lhy.mucAllMembers;

import org.jivesoftware.openfire.IQHandlerInfo;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.handler.IQHandler;
import org.xmpp.packet.IQ;

public class MyIQHander extends IQHandler {
	
	private static final String MODULE_NAME = "group tree handler";
	private static final String NAME_SPACE = "com:im:group";
	private IQHandlerInfo info;
	
	public MyIQHander() {
		super(MODULE_NAME);
		info = new IQHandlerInfo("gruops", NAME_SPACE);
	}

	@Override
	public IQHandlerInfo getInfo() {
		return info;
	}

	@Override
	public IQ handleIQ(IQ packet) throws UnauthorizedException {
		IQ reply = IQ.createResultIQ(packet);
		System.out.println("XML " + reply.toXML());
		return reply;
	}
}
