package biz.xsoftware.api.nio.handlers;

public interface FutureOperation {

	public void waitForOperation(long timeoutInMillis) throws InterruptedException;
	
	public void waitForOperation() throws InterruptedException;
	
	public void setListener(OperationCallback cb);
	
}
