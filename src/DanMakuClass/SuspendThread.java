package DanMakuClass;

/**
 * 可以暂停的线程
 * @author 晨晖
 *
 */
public abstract class SuspendThread extends Thread{
	private boolean isStop = false;
	private boolean isPause = false;
	private Object mLock = new Object();
	
	public void setPause(boolean isPause){
		this.isPause = isPause;
		if(!isPause){
			synchronized(mLock){
				mLock.notify();
			}
		}
	}
	
	public boolean isPause(){
		return this.isPause;
	}
	
	public void stopThread(){
		this.isStop = true;
	}
	
	@Override
	public void run() {
		this.runPre();
		while(!isStop){
			if(this.isPause){
				synchronized(mLock){
					try {
						mLock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			this.runLogic();
		}
		this.runAfter();
	}
	
	/**
	 * 线程循环运行之前
	 */
	public void runPre(){
		
	}
	
	/**
	 * 线程运行之后
	 */
	public void runAfter(){
		
	}
	
	/**
	 * 线程循环逻辑
	 */
	public abstract void runLogic();
}
