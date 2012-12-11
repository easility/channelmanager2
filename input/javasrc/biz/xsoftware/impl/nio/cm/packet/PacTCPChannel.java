package biz.xsoftware.impl.nio.cm.packet;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

import biz.xsoftware.api.nio.channels.TCPChannel;
import biz.xsoftware.api.nio.handlers.ConnectionCallback;
import biz.xsoftware.api.nio.handlers.DataListener;
import biz.xsoftware.api.nio.handlers.WriteCloseCallback;
import biz.xsoftware.api.nio.libs.PacketProcessor;
import biz.xsoftware.impl.nio.util.UtilPassThroughWriteHandler;
import biz.xsoftware.impl.nio.util.UtilTCPChannel;

class PacTCPChannel extends UtilTCPChannel implements TCPChannel {

//	private static final Logger log = Logger.getLogger(TCPChannelImpl.class.getName());
//	private BufferHelper helper = ChannelManagerFactory.bufferHelper(null);
	
	private TCPChannel realChannel;
	private PacketProcessor packetProcessor;
	
	public PacTCPChannel(TCPChannel channel, PacketProcessor proc) {
		super(channel);
		this.realChannel = channel;
		this.packetProcessor = proc;
	}
	
	public void registerForReads(DataListener listener) throws IOException, InterruptedException {
		PacProxyDataHandler handler = new PacProxyDataHandler(this, packetProcessor, listener);
		packetProcessor.setPacketListener(handler);
		realChannel.registerForReads(handler);
	}


	@Override
	public int write(ByteBuffer b) throws IOException {
		int retVal = b.remaining();
		ByteBuffer out = packetProcessor.processOutgoing(b);
		realChannel.write(out);
		return retVal;
	}
	
	@Override
	public void write(ByteBuffer b, WriteCloseCallback h, int id) throws IOException, InterruptedException {
		ByteBuffer out = packetProcessor.processOutgoing(b);
		realChannel.write(out, new UtilPassThroughWriteHandler(this, h), id);
	}
	
	public void connect(SocketAddress addr, ConnectionCallback c) throws IOException, InterruptedException {
		if(c == null)
			throw new IllegalArgumentException("ConnectCallback cannot be null");
		
		PacProxyConnectCb proxy = new PacProxyConnectCb(this, c);
		realChannel.connect(addr, proxy);
	}	
}