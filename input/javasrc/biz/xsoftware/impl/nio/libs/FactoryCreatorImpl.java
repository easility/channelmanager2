package biz.xsoftware.impl.nio.libs;

import java.util.Map;

import javax.net.ssl.SSLEngine;

import biz.xsoftware.api.nio.channels.RegisterableChannel;
import biz.xsoftware.api.nio.libs.AsynchSSLEngine;
import biz.xsoftware.api.nio.libs.BufferFactory;
import biz.xsoftware.api.nio.libs.ChannelSession;
import biz.xsoftware.api.nio.libs.FactoryCreator;
import biz.xsoftware.api.nio.libs.PacketProcessorFactory;
import biz.xsoftware.api.nio.libs.StartableExecutorService;
import biz.xsoftware.api.nio.libs.StartableRouterExecutor;

public class FactoryCreatorImpl extends FactoryCreator {

	private static final byte[] DEFAULT_SEPARATOR = new byte[] { Byte.MAX_VALUE, Byte.MIN_VALUE, Byte.MAX_VALUE, Byte.MIN_VALUE, Byte.MAX_VALUE };

	@Override
	public void configure(Map<String, Object> map) {
	}

	@Override
	public BufferFactory createBufferFactory(Map<String, Object> map) {
		if(map == null)
			throw new IllegalArgumentException("map cannot be null");

		Object direct = map.get(FactoryCreator.KEY_IS_DIRECT);
		boolean isDirect = false;
		if(direct != null && direct instanceof Boolean)
			isDirect = (Boolean)direct;
		
		DefaultByteBufferFactory bufFactory = new DefaultByteBufferFactory();
		bufFactory.setDirect(isDirect);
		return bufFactory;
	}

	@Override
	public PacketProcessorFactory createPacketProcFactory(Map<String, Object> map) {
		byte[] separator;
		if(map == null) {
			separator = DEFAULT_SEPARATOR;
		} else {
			Object sep = map.get(FactoryCreator.KEY_PACKET_SEPARATOR);
			
			if(sep == null)
				separator = DEFAULT_SEPARATOR;
			else if(!(sep instanceof byte[]))
				throw new IllegalArgumentException("key=FactoryCreator.KEY_PACKET_SEPARATOR must contain a byte array");
			else
				separator = (byte[])sep;
		}
		
		DefaultPackProcessorFactory factory = new DefaultPackProcessorFactory();
		factory.setSeparator(separator);
		return factory;
	}

	@Override
	public StartableExecutorService createExecSvcFactory(Map<String, Object> map) {
		int numThreads = 10;
		if(map != null) {
			Object num = map.get(FactoryCreator.KEY_NUM_THREADS);
			if(num == null) {
			} else if(!(num instanceof Integer))
				throw new IllegalArgumentException("key=FactoryCreator.KEY_NUM_THREADS must contain an Integer");
			else
				numThreads = (Integer)num;
		}
		return new ExecutorServiceProxy(numThreads);
	}

    /**
     * @see biz.xsoftware.api.nio.libs.FactoryCreator#createAdvancedExecSvc(java.util.Map)
     */
    @Override
    public StartableExecutorService createAdvancedExecSvc(Map<String, Object> map)
    {
        int numThreads = 10;
        if(map != null) {
            Object num = map.get(FactoryCreator.KEY_NUM_THREADS);
            if(num == null) {
            } else if(!(num instanceof Integer))
                throw new IllegalArgumentException("key=FactoryCreator.KEY_NUM_THREADS must contain an Integer");
            else
                numThreads = (Integer)num;
        }
        return new AdvancedExecutorService(numThreads);
    }
    
	@Override
	public AsynchSSLEngine createSSLEngine(Object id, SSLEngine engine, Map<String, Object> map) {
		return new AsynchSSLEngineImpl(id+"", engine);
	}

	@Override
	public ChannelSession createSession(RegisterableChannel channel) {
		return new ChannelSessionImpl(channel);
	}

	@Override
	public StartableRouterExecutor createRoutingExecutor(String name, int numThreads) {
		return new RoutingExecutor(name, numThreads);
	}

}
