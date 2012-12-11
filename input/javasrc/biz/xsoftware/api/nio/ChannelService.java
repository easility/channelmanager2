package biz.xsoftware.api.nio;

import java.io.IOException;

/**
 * @author Dean Hiller
 */
public interface ChannelService extends ChannelManager {

	public void start() throws IOException;

	public void stop() throws IOException, InterruptedException;

}