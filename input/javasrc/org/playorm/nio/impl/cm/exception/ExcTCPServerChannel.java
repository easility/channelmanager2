package org.playorm.nio.impl.cm.exception;

import java.io.IOException;

import org.playorm.nio.api.channels.TCPServerChannel;
import org.playorm.nio.api.handlers.ConnectionListener;
import org.playorm.nio.impl.util.UtilRegisterable;


class ExcTCPServerChannel extends UtilRegisterable implements TCPServerChannel {

	private TCPServerChannel realChannel;
	
	public ExcTCPServerChannel(TCPServerChannel c) {
		super(c);
		realChannel = c;
	}

	public TCPServerChannel getRealChannel() {
		return realChannel;
	}
	
	public void oldClose() {
		realChannel.oldClose();
	}
	
	public void registerServerSocketChannel(ConnectionListener cb) throws IOException, InterruptedException {
		ExcProxyAcceptCb proxy = new ExcProxyAcceptCb(this, cb);
		realChannel.registerServerSocketChannel(proxy);
	}
}