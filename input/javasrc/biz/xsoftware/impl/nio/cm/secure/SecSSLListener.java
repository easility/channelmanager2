package biz.xsoftware.impl.nio.cm.secure;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

import biz.xsoftware.api.nio.channels.Channel;
import biz.xsoftware.api.nio.handlers.ConnectionListener;
import biz.xsoftware.api.nio.handlers.DataListener;
import biz.xsoftware.api.nio.handlers.NullWriteCallback;
import biz.xsoftware.api.nio.handlers.WriteCloseCallback;
import biz.xsoftware.api.nio.libs.SSLListener;

class SecSSLListener implements SSLListener {

	private static final Logger log = Logger.getLogger(SecSSLListener.class.getName());
	
	private SecTCPChannel channel;
	private ConnectionListener cb;
	private DataListener client;
	private int tempId = 0;
	private boolean isConnected = false;
	
	public SecSSLListener(SecTCPChannel impl) {
		this.channel = impl;
	}
	
	public void encryptedLinkEstablished() throws IOException {
		try {
			channel.resetRegisterForReadState();
		} catch (InterruptedException e) {
			throw new RuntimeException(channel+"Exception occured", e);
		}
		cb.connected(channel);
		isConnected = true;
	}
	


	public void packetEncrypted(ByteBuffer toSocket, Object passThrough) throws IOException {
		WriteCloseCallback h;
		int id = -2;
		if(passThrough == null){
			h = NullWriteCallback.singleton();
			id = tempId--;
		} else {
			SecProxyWriteHandler handler = (SecProxyWriteHandler)passThrough;
			id = handler.getId();
			h = handler;
		}
		try {
			channel.getRealChannel().write(toSocket, h, id);
		} catch (InterruptedException e) {
			throw new RuntimeException(channel+e.getMessage(), e);
		}
	}
	
	public void packetUnencrypted(ByteBuffer out) throws IOException {
		client.incomingData(channel, out);
	}
	
	public void setClientHandler(DataListener client) {
		this.client = client;
	}
	public boolean isClientRegistered() {
		return client != null;
	}

	public void setConnectCallback(ConnectionListener cb) {
		this.cb = cb;
	}

	public void farEndClosed() {
		if(client != null) //if the client did not register for reads, we can't fire to anyone(thought that would be mighty odd)
			client.farEndClosed(channel);
		else if(!isConnected) {
			log.info("The far end connected and did NOT establish security session and then closed his socket.  " +
					"This is normal behavior if a telnet socket connects to your secure socket and exits " +
					"because the socket was never officially 'connected' as we only fire " +
					"connected AFTER the SSL handshake is done.  You may want to check if" +
					" someone is trying to hack your server though");
		} else
			log.warning("When we called ConnectionListener.connected on YOUR ConnectionListener, " +
					"you forot to call registerForReads so we have not callback handler to call " +
					"to tell you this socket is closed from far end");
	}

	//TODO: should be passed into ChannelManager so it is configurable!!!
//	private ExecutorService svc = Executors.newFixedThreadPool(1);
	public void runTask(Runnable r, boolean isInitialHandshake) {
		//TODO: AsynchSSLEngine needs to handle a very tough situation...letting
		//data backup on a rehandshake while this task runs as data cannot be fed
		//until the task is done running, or don't do a rehandshake....
//		if(isInitialHandshake)
//			svc.execute(r);
//		else
			r.run(); //run on this thread to avoid problems with incoming packets
	}

	public void closed(boolean clientInitiated) {
//		if(fromEncryptedPacket && !closedAlready)
//			client.farEndClosed(channel);
		//can just drop this...we are using close, not initiateClose
		//which is effective immediately.
	}

	public void feedProblemThrough(Channel c, ByteBuffer b, Object problem) throws IOException {
		client.incomingData(c, b);
	}

}
