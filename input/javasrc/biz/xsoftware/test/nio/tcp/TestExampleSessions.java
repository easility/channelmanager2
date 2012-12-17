package biz.xsoftware.test.nio.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;
import biz.xsoftware.api.nio.BufferHelper;
import biz.xsoftware.api.nio.ChannelService;
import biz.xsoftware.api.nio.ChannelServiceFactory;
import biz.xsoftware.api.nio.channels.Channel;
import biz.xsoftware.api.nio.channels.RegisterableChannel;
import biz.xsoftware.api.nio.channels.TCPChannel;
import biz.xsoftware.api.nio.channels.TCPServerChannel;
import biz.xsoftware.api.nio.handlers.ConnectionListener;
import biz.xsoftware.api.nio.handlers.DataListener;
import biz.xsoftware.api.nio.libs.ChannelSession;

/**
 */
public class TestExampleSessions extends TestCase
{
    private static final Logger log = Logger.getLogger(TestExampleSessions.class.getName());
    private static final BufferHelper HELPER = ChannelServiceFactory.bufferHelper(null);
    
    private ChannelService svc;
    private TCPChannel svrTCPChannel;
    
    public TestExampleSessions(String name) {
        super(name);
    }
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        svc = ChannelServiceFactory.createDefaultChannelMgr("theOne");

        svc.start();
    }


    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        //make sure we sleep long enough for the listener to receive their data
        //after all this is asynchronous and this thread can go off and do other work while waiting
        //but in our example, instead of doing work, we will sleep.
        Thread.sleep(4000);
        svc.stop();
    }
    
    public void testExample() throws Exception {
        InetSocketAddress addr = startServer();

        InetAddress host = InetAddress.getLocalHost();
        InetSocketAddress fullSvrAddr = new InetSocketAddress(host, addr.getPort());
        runClient(fullSvrAddr);
    }

    private void runClient(SocketAddress svrAddr) throws Exception {
        TCPChannel client = svc.createTCPChannel("client", null);
        client.bind(new InetSocketAddress(0));        
        
        //we will just go with synchronous connect today...(could have used the asynch one though)
        client.oldConnect(svrAddr);
        client.registerForReads(new MyDataListener("client"));
        
        //because this is not a real example and the server and client are both here, we
        //will sleep and wait for the svrTCPChannel to be cached!!  Normally, this is
        //not needed
        Thread.sleep(2000);
        
        String msg = "hello world";
        ByteBuffer b = ByteBuffer.allocate(100);
        HELPER.putString(b, msg);
        b.flip();
        client.oldWrite(b);

        b.rewind();
        client.oldWrite(b);
        
        b.rewind();
        svrTCPChannel.oldWrite(b);
        
        b.rewind();
        client.oldWrite(b);
    }
    
    private InetSocketAddress startServer() throws Exception {
        TCPServerChannel server = svc.createTCPServerChannel("server", null);      
        
        //bind to 0 allows it to bind to any port...
        server.bind(new InetSocketAddress(0));        
        server.registerServerSocketChannel(new MyServerSocketListener());
        
        return  server.getLocalAddress();
    }
    
    private class MyServerSocketListener implements ConnectionListener {

        /**
         * @see biz.xsoftware.api.nio.handlers.ConnectionListener#finished(Channel)
         */
        public void finished(Channel channel) throws IOException
        {
            try
            {
                //cache the server channel
                TestExampleSessions.this.svrTCPChannel = (TCPChannel) channel;
                channel.registerForReads(new MyDataListener("server"));
            }
            catch(InterruptedException e)
            {
                log.log(Level.WARNING, "", e);
            }
        }

        /**
         * @see biz.xsoftware.api.nio.handlers.ConnectionListener#failed
         * (RegisterableChannel, java.lang.Throwable)
         */
        public void failed(RegisterableChannel channel, Throwable e)
        {
            log.log(Level.WARNING, "", e);
        }
        
    }
    
    private static class MyDataListener implements DataListener {

        private String name;
        
        public MyDataListener(String name) {
            this.name= name;
        }
        
        /**
         * @see biz.xsoftware.api.nio.handlers.DataListener#incomingData(Channel, java.nio.ByteBuffer)
         */
        public synchronized void incomingData(Channel channel, ByteBuffer b) throws IOException
        {
        	//A session is associated with a channel which should make sense as
        	//if the channel goes away, the session goes away.
        	ChannelSession session = channel.getSession();
        	Integer sessionCount = (Integer)session.get("count");
        	if(sessionCount == null) {
        		sessionCount = Integer.valueOf(0);
        	}
        	
            String msg = HELPER.readString(b, b.remaining());   
            log.info(name+" count="+(sessionCount++)+" says '"+msg+"'    s="+session+"  t="+Thread.currentThread());

        	session.put("count", sessionCount);
        }

        /**
         * @see biz.xsoftware.api.nio.handlers.DataListener#farEndClosed(Channel)
         */
        public void farEndClosed(Channel channel)
        {
        }

        /**
         * @see biz.xsoftware.api.nio.handlers.DataListener#failure(Channel, java.nio.ByteBuffer, java.lang.Exception)
         */
        public void failure(Channel channel, ByteBuffer data, Exception e)
        {
        }
        
    }
}
