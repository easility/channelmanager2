package biz.xsoftware.impl.nio.cm.threaded;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;

import biz.xsoftware.api.nio.channels.TCPChannel;
import biz.xsoftware.api.nio.handlers.ConnectionCallback;
import biz.xsoftware.api.nio.handlers.DataListener;
import biz.xsoftware.api.nio.handlers.OperationCallback;
import biz.xsoftware.api.nio.libs.BufferFactory;
import biz.xsoftware.impl.nio.util.UtilTCPChannel;

class ThdTCPChannel extends UtilTCPChannel implements TCPChannel {

//	private static final Logger log = Logger.getLogger(TCPChannelImpl.class.getName());
//	private BufferHelper helper = ChannelManagerFactory.bufferHelper(null);
	
	private TCPChannel realChannel;
	private Executor svc;
	private BufferFactory bufFactory;
	
	public ThdTCPChannel(TCPChannel channel, Executor svc, BufferFactory bufFactory) {
		super(channel);
		this.realChannel = channel;
		this.svc = svc;
		this.bufFactory = bufFactory;
	}
	
	public void registerForReads(DataListener listener) throws IOException, InterruptedException {
		ThdProxyDataHandler handler = new ThdProxyDataHandler(this, listener, svc, bufFactory);
		realChannel.registerForReads(handler);
	}

	@Override
	public int oldWrite(ByteBuffer b) throws IOException {
		return realChannel.oldWrite(b);
	}
	
	public void write(ByteBuffer b, OperationCallback h) throws IOException, InterruptedException {
		realChannel.write(b, new ThdProxyWriteHandler(this, h, svc));
	}
	
	public void connect(SocketAddress addr, ConnectionCallback c) throws IOException, InterruptedException {
		if(c == null)
			throw new IllegalArgumentException("ConnectCallback cannot be null");
		
		ThdProxyConnectCb proxy = new ThdProxyConnectCb(this, c, svc);
		realChannel.connect(addr, proxy);
	}

	public void close(OperationCallback h) {
		realChannel.close(new ThdProxyWriteHandler(this, h, svc));
	}    
}
