package biz.xsoftware.impl.nio.cm.basic;

import java.util.logging.Level;
import java.util.logging.Logger;

import biz.xsoftware.api.nio.handlers.WriteCloseCallback;

public class CloseRunnable implements DelayedWritesCloses {

	private static final Logger log = Logger.getLogger(CloseRunnable.class.getName());
	private BasChannelImpl channel;
	private WriteCloseCallback handler;
    
	public CloseRunnable(BasChannelImpl c, WriteCloseCallback h) {
		channel = c;
		handler = h;
	}

	public boolean runDelayedAction(boolean isSelectorThread) {

        if(log.isLoggable(Level.FINER))
            log.finer(channel+"Closing channel.  isOnSelectThread="+isSelectorThread);
        
		try {
			channel.closeImpl();
            
            //must wake up selector or socket will not send the TCP FIN packet!!!!! 
            //The above only happens on the client thread...on selector thread, close works fine.
            channel.wakeupSelector();
            
			handler.finished(channel);
		} catch(Exception e) {
			log.log(Level.WARNING, channel+"Exception occurred", e);
			handler.failed(channel, e);
		}
		return true;
	}

}
