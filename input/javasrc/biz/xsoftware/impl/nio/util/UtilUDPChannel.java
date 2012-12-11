package biz.xsoftware.impl.nio.util;

import java.io.IOException;

import biz.xsoftware.api.nio.channels.UDPChannel;

public class UtilUDPChannel extends UtilChannel implements UDPChannel {

	private UDPChannel realChannel;

	public UtilUDPChannel(UDPChannel realChannel) {
		super(realChannel);
		this.realChannel = realChannel;
	}

    /**
     * @see biz.xsoftware.api.nio.channels.UDPChannel#disconnect()
     */
    public void disconnect() throws IOException
    {
        realChannel.disconnect();
    }
}