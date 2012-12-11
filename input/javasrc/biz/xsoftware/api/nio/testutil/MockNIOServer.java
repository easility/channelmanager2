package biz.xsoftware.api.nio.testutil;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.DatagramChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import biz.xsoftware.api.nio.ChannelService;
import biz.xsoftware.api.nio.Settings;
import biz.xsoftware.api.nio.channels.Channel;
import biz.xsoftware.api.nio.channels.RegisterableChannel;
import biz.xsoftware.api.nio.channels.TCPChannel;
import biz.xsoftware.api.nio.channels.TCPServerChannel;
import biz.xsoftware.api.nio.handlers.ConnectionListener;
import biz.xsoftware.api.nio.handlers.DataListener;


/**
 * @author Dean Hiller
 */
public class MockNIOServer extends MockDataHandler implements ConnectionListener {

	public static final String CONNECTED = "connected";
	public static final String CONN_FAILED = "connectFailed";
	
	private static final Logger log = Logger.getLogger(MockNIOServer.class.getName());

	private ChannelService chanMgr;
	private TCPServerChannel srvrChannel;
	private List<TCPChannel> sockets = new LinkedList<TCPChannel>();
	private Settings factoryHolder;
	private DatagramChannel udp;
	private DatagramReceiver recThread;
	
	public MockNIOServer(ChannelService svr, Settings h) throws UnknownHostException {
		super();
		this.chanMgr = svr;
		this.factoryHolder = h;
	}
	
	public InetSocketAddress start() throws IOException, InterruptedException {
		int port = 0;
	
		chanMgr.start();
		
		InetAddress loopBack = InetAddress.getByName("127.0.0.1");
		InetSocketAddress svrAddr = new InetSocketAddress(loopBack, port);		
		srvrChannel = chanMgr.createTCPServerChannel("TCPServerChannel", factoryHolder);
		srvrChannel.setReuseAddress(true);
		srvrChannel.bind(svrAddr);	
		srvrChannel.registerServerSocketChannel(this);
		
		udp = DatagramChannel.open();
		udp.configureBlocking(true);
		log.info("port="+srvrChannel.getLocalAddress().getPort());
		InetSocketAddress udpAddr = new InetSocketAddress(loopBack, srvrChannel.getLocalAddress().getPort()+1);
		udp.socket().bind(udpAddr);
		
		recThread = new DatagramReceiver(udp, this);
		recThread.start();
		
		return srvrChannel.getLocalAddress();
	}
	
	public void stop() throws IOException, InterruptedException {	
		recThread.stopThread();
		udp.close();
		
		srvrChannel.close();
		for(int i = 0; i < sockets.size(); i++) {
			Channel channel = sockets.get(i);
			channel.close();
		}
		//NOTE: Keep this stop here after closing the channels at sometimes
		//there are bugs there and they will show up this way.
		chanMgr.stop();		
	}
		
	/* (non-Javadoc)
	 * @see biz.xsoftware.testlib.MockSuperclass#clone(java.lang.Object)
	 */
	protected Object clone(Object o) {
		return o;
	}

	public void connected(TCPChannel channel) throws IOException {
		try {
			log.fine(channel+"mockserver accepted connection");
			sockets.add(channel);
			channel.registerForReads(MockNIOServer.this);
			methodCalled(CONNECTED, channel);
		} catch (InterruptedException e) {
			//puts exception on test thread.
			methodCalled(CONNECTED, e);
		}		
	}

	public void connectFailed(RegisterableChannel channel, Throwable e) {
		methodCalled(CONN_FAILED, e);
	}
	
	public String toString() {
		return chanMgr.toString();
	}

	public DatagramChannel getUDPServerChannel() {
		return udp;
	}
	
	private static class DatagramReceiver extends Thread {

		private ByteBuffer buffer;
		private DatagramChannel udp;
		private boolean stop = false;
		private DataListener handler;
		public DatagramReceiver(DatagramChannel udp, DataListener handler) {
			this.udp = udp;
			this.handler = handler;
			buffer = ByteBuffer.allocate(6000);
		}
		
		public void stopThread() {
			stop = true;
			this.interrupt();
		}
		
		@Override
		public void run() {
			while(!stop) {
				try {
					buffer.clear();
					udp.receive(buffer);
					buffer.flip();
					handler.incomingData(null, buffer);
				} catch(ClosedByInterruptException e) {
					//occurs when thread is interrupted
				} catch(Exception e) {
					log.log(Level.WARNING, "Failure", e);
					handler.failure(null, buffer, e);
				}
			}
		}
		
	}
}
