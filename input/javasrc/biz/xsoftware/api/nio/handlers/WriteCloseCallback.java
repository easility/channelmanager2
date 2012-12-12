package biz.xsoftware.api.nio.handlers;


import biz.xsoftware.api.nio.channels.Channel;

public interface WriteCloseCallback {

	public void finished(Channel c);
	
	public void failed(Channel c, Throwable e);
	
}
