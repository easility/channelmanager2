package biz.xsoftware.impl.nio.cm.exception;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import biz.xsoftware.api.nio.channels.Channel;
import biz.xsoftware.api.nio.channels.RegisterableChannel;
import biz.xsoftware.api.nio.channels.TCPChannel;
import biz.xsoftware.api.nio.channels.TCPServerChannel;
import biz.xsoftware.api.nio.handlers.ConnectionListener;

class ExcProxyAcceptCb implements ConnectionListener {

	private static final Logger log = Logger.getLogger(ExcProxyAcceptCb.class.getName());
	
	private TCPServerChannel svrChannel;
	private ConnectionListener cb;

	public ExcProxyAcceptCb(TCPServerChannel svrChannel, ConnectionListener cb) {
		this.svrChannel = svrChannel;
		this.cb = cb;
	}
	
	public void finished(Channel channel) throws IOException {
		try {
			TCPChannel newChannel = new ExcTCPChannel((TCPChannel) channel);
			cb.finished(newChannel);
		} catch(Exception e) {
			log.log(Level.WARNING, channel+"Exception", e);
		}
	}

	public void failed(RegisterableChannel channel, Throwable e) {
		try {
			cb.failed(svrChannel, e);
		} catch(Exception ee) {
			log.log(Level.WARNING, channel+"Exception", ee);
		}
	}
}
