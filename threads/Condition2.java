package nachos.threads;

import nachos.machine.*;
/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see	nachos.threads.Condition
 */
public class Condition2 {
    /**
     * Allocate a new condition variable.
     *
     * @param	conditionLock	the lock associated with this condition
     *				variable. The current thread must hold this
     *				lock whenever it uses <tt>sleep()</tt>,
     *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
     */
    public Condition2(Lock conditionLock) {
	this.conditionLock = conditionLock;
    }
    private static class PingTest implements Runnable {
	PingTest(int which) {
	    this.which = which;
	}
	public void run() {
	    lock.acquire();
	    System.out.println(which + "running");
	    CTest.wake();
	    CTest.sleep();
	    System.out.println(which + "running");
	    lock.release();
	    n--;
	}
	private int which;
    }
    private static class PingTest2 implements Runnable {
	PingTest2(int which) {
	    this.which = which;
	}
	public void run() {
	    lock.acquire();
	    System.out.println(which + "running");
	    CTest.sleep();
	    System.out.println(which + "running");
	    lock.release();
	    n--;
	}
	private int which;
    }
    public static void selfTest(){
	System.out.println("---Condition2---");
	System.out.println("---test1---");
	lock = new Lock();
	CTest = new Condition2(lock);
	n = 5;
	for (int i = 0; i < n; i++){
		new KThread(new PingTest(i)).fork();
	}
	while (n > 1) KThread.currentThread().yield();
	lock.acquire();
	CTest.wake();
	lock.release();
	while (n > 0) KThread.currentThread().yield();
	System.out.println("---test2---");
	lock = new Lock();
	CTest = new Condition2(lock);
	n = 5;
	for (int i = 0; i < n; i++){
		new KThread(new PingTest2(i)).fork();
	}
	System.out.println("wakeAll");
	lock.acquire();
	CTest.wakeAll();
	lock.release();
	while (n > 0) KThread.currentThread().yield();
    }
    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically reacquire the lock before <tt>sleep()</tt> returns.
     */
    public void sleep() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	boolean status = Machine.interrupt().disable();
	conditionLock.release();
	waitQueue.waitForAccess(KThread.currentThread());
	KThread.currentThread().sleep();
	conditionLock.acquire();
	Machine.interrupt().restore(status);
    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	boolean status = Machine.interrupt().disable();
	KThread thread = waitQueue.nextThread();
	if (thread != null)
	{
		thread.ready();
	}
	Machine.interrupt().restore(status);
    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	boolean status = Machine.interrupt().disable();
	KThread thread = waitQueue.nextThread();
	while (thread != null)
	{
		thread.ready();
		thread = waitQueue.nextThread();
	}
	Machine.interrupt().restore(status);
    }
    private static int n;
    private static Lock lock;
    private static Condition2 CTest;
    private Lock conditionLock;
    private ThreadQueue waitQueue = ThreadedKernel.scheduler.newThreadQueue(false);
}
