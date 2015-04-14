package nachos.threads;

import nachos.machine.*;
import java.util.*;
/**
 * A scheduler that chooses threads based on their priorities.
 *
 * <p>
 * A priority scheduler associates a priority with each thread. The next thread
 * to be dequeued is always a thread with priority no less than any other
 * waiting thread's priority. Like a round-robin scheduler, the thread that is
 * dequeued is, among all the threads of the same (highest) priority, the
 * thread that has been waiting longest.
 *
 * <p>
 * Essentially, a priority scheduler gives access in a round-robin fassion to
 * all the highest-priority threads, and ignores all other threads. This has
 * the potential to
 * starve a thread if there's always a thread waiting with higher priority.
 *
 * <p>
 * A priority scheduler must partially solve the priority inversion problem; in
 * particular, priority must be donated through locks, and through joins.
 */
public class PriorityScheduler extends Scheduler {
    /**
     * Allocate a new priority scheduler.
     */
    public PriorityScheduler() {
    }

    /**
     * Allocate a new priority thread queue.
     *
     * @param	transferPriority	<tt>true</tt> if this queue should
     *					transfer priority from waiting threads
     *					to the owning thread.
     * @return	a new priority thread queue.
     */
    public ThreadQueue newThreadQueue(boolean transferPriority) {
	return new PriorityQueue(transferPriority);
    }

    public int getPriority(KThread thread) {
	Lib.assertTrue(Machine.interrupt().disabled());

	return getThreadState(thread).getPriority();
    }

    public int getEffectivePriority(KThread thread) {
	Lib.assertTrue(Machine.interrupt().disabled());

	return getThreadState(thread).getEffectivePriority();
    }

    public void setPriority(KThread thread, int priority) {
	Lib.assertTrue(Machine.interrupt().disabled());

	Lib.assertTrue(priority >= priorityMinimum &&
		   priority <= priorityMaximum);

	getThreadState(thread).setPriority(priority);
    }

    public boolean increasePriority() {
	boolean intStatus = Machine.interrupt().disable();

	KThread thread = KThread.currentThread();

	int priority = getPriority(thread);
	if (priority == priorityMaximum)
	    return false;

	setPriority(thread, priority+1);

	Machine.interrupt().restore(intStatus);
	return true;
    }

    public boolean decreasePriority() {
	boolean intStatus = Machine.interrupt().disable();

	KThread thread = KThread.currentThread();

	int priority = getPriority(thread);
	if (priority == priorityMinimum)
	    return false;

	setPriority(thread, priority-1);

	Machine.interrupt().restore(intStatus);
	return true;
    }

    /**
     * The default priority for a new thread. Do not change this value.
     */
    public static final int priorityDefault = 1;
    /**
     * The minimum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMinimum = 0;
    /**
     * The maximum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMaximum = 7;

    /**
     * Return the scheduling state of the specified thread.
     *
     * @param	thread	the thread whose scheduling state to return.
     * @return	the scheduling state of the specified thread.
     */


    protected ThreadState getThreadState(KThread thread) {
	if (thread.schedulingState == null)
	    thread.schedulingState = new ThreadState(thread);

	return (ThreadState) thread.schedulingState;
    }

    /**
     * A <tt>ThreadQueue</tt> that sorts threads by priority.
     */




    protected class PriorityQueue extends ThreadQueue {
	PriorityQueue(boolean transferPriority) {
	    this.transferPriority = transferPriority;
	}

	public void waitForAccess(KThread thread) {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    getThreadState(thread).waitForAccess(this);
	}

	public void acquire(KThread thread) {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    ThreadState t=getThreadState(thread);
        if(this.holder!=null&&this.transferPriority){
            this.holder.Holding.remove(this);
            this.holder.setChange();
        }
        this.holder=t;
        t.acquire(this);
	}

	public KThread nextThread() {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    if(waitingQueue.isEmpty())return null;
        if(this.holder!=null&&this.transferPriority){
            this.holder.Holding.remove(this);
            this.holder.setChange();
            this.holder=null;
        }
        KThread t=pickNextThread().thread;
        if(t!=null){
            waitingQueue.remove(getThreadState(t));
            this.holder=getThreadState(t);
            getThreadState(t).acquire(this);
            changed=true;
        }
        return t;
	}

	/**
	 * Return the next thread that <tt>nextThread()</tt> would return,
	 * without modifying the state of this queue.
	 *
	 * @return	the next thread that <tt>nextThread()</tt> would
	 *		return.
	 */
	protected ThreadState pickNextThread() {
	    ThreadState t=null;
        for(Iterator<ThreadState> it=waitingQueue.iterator();it.hasNext();){
            ThreadState next=it.next();
            if(t==null||next.getEffectivePriority()>t.getEffectivePriority())t=next;
        }
        return t;
	}
    public int getEffectivePriority(){
            if(!transferPriority)return 0;
            if(changed){
                int m=0;
                for(Iterator<ThreadState> it=waitingQueue.iterator();it.hasNext();){
                    ThreadState t=it.next();
                    int n=t.getEffectivePriority();
                    if(n>m)m=n;
                }
                this.effectivePriority=m;
                changed=false;
            }
            return effectivePriority;
        }
	public void print() {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    // implement me (if you want)
	}
    public void setChange(){
        if(changed||transferPriority==false)return;
        changed=true;
        if(holder!=null)holder.setChange();
    }

	/**
	 * <tt>true</tt> if this queue should transfer priority from waiting
	 * threads to the owning thread.
	 */
	public boolean transferPriority;
    public LinkedList<ThreadState> waitingQueue = new LinkedList<ThreadState>();
    public ThreadState holder=null;
    public int effectivePriority=0;
    public boolean changed=false;
    }

    /**
     * The scheduling state of a thread. This should include the thread's
     * priority, its effective priority, any objects it owns, and the queue
     * it's waiting for, if any.
     *
     * @see	nachos.threads.KThread#schedulingState
     */











    protected class ThreadState {
	/**
	 * Allocate a new <tt>ThreadState</tt> object and associate it with the
	 * specified thread.
	 *
	 * @param	thread	the thread this state belongs to.
	 */
	public ThreadState(KThread thread) {
	    this.thread = thread;
	    this.Holding=new LinkedList<ThreadQueue>();
        this.Waiting=null;
	    setPriority(priorityDefault);
        this.effectPriority=priorityDefault;
	}

	/**
	 * Return the priority of the associated thread.
	 *
	 * @return	the priority of the associated thread.
	 */
	public int getPriority() {
	    return priority;
	}

	/**
	 * Return the effective priority of the associated thread.
	 *
	 * @return	the effective priority of the associated thread.
	 */
	public int getEffectivePriority() {
        int max=priority;
	    if(changed){
            for(Iterator<ThreadQueue> it=Holding.iterator();it.hasNext();){
                PriorityQueue q=(PriorityQueue)it.next();
                int m=q.getEffectivePriority();
                if(m>max)max=m;
            }
            this.effectPriority=max;
            changed=false;
        }
        return effectPriority;
	}

	/**
	 * Set the priority of the associated thread to the specified value.
	 *
	 * @param	priority	the new priority.
	 */
	public void setPriority(int priority) {
	    if (this.priority == priority)
		return;

	    this.priority = priority;
        if(priority>effectPriority)effectPriority=priority;
	    this.setChange();

	}

	/**
	 * Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
	 * the associated thread) is invoked on the specified priority queue.
	 * The associated thread is therefore waiting for access to the
	 * resource guarded by <tt>waitQueue</tt>. This method is only called
	 * if the associated thread cannot immediately obtain access.
	 *
	 * @param	waitQueue	the queue that the associated thread is
	 *				now waiting on.
	 *
	 * @see	nachos.threads.ThreadQueue#waitForAccess
	 */
	public void waitForAccess(PriorityQueue waitQueue) {
	    waitQueue.waitingQueue.add(this);
        waitQueue.setChange();
        Waiting=waitQueue;
        if(Holding.indexOf(waitQueue)!=-1){
            Holding.remove(waitQueue);
            this.setChange();
            waitQueue.holder=null;
        }
	}

	/**
	 * Called when the associated thread has acquired access to whatever is
	 * guarded by <tt>waitQueue</tt>. This can occur either as a result of
	 * <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
	 * <tt>thread</tt> is the associated thread), or as a result of
	 * <tt>nextThread()</tt> being invoked on <tt>waitQueue</tt>.
	 *
	 * @see	nachos.threads.ThreadQueue#acquire
	 * @see	nachos.threads.ThreadQueue#nextThread
	 */
	public void acquire(PriorityQueue waitQueue) {
	    Holding.add(waitQueue);
        if(waitQueue== Waiting){
            waitQueue.waitingQueue.remove(this);
            Waiting=null;
        }
        setChange();
	}
    public void setChange(){
        if(changed)return;
        changed=true;
        PriorityQueue w=(PriorityQueue)Waiting;
        if(w!=null) w.setChange();
    }
	    /** The thread with which this object is associated. */
	    protected KThread thread;
	    /** The priority of the associated thread. */
	    protected int priority;
        protected int effectPriority;
        public LinkedList<ThreadQueue> Holding;
        public ThreadQueue Waiting;
        public boolean changed=false;
    }
    private static class NamedLock extends Lock {
      
	public NamedLock(String name) {
	  super();
 	  this.name = name;
        }

	public String getName() {
          return this.name;
        }

	public void setName(String name) {
          this.name = name;
        }

        private String name;
    }

    private static class PriorityDonationWorker implements Runnable {

	PriorityDonationWorker(String name, boolean once, 
                        int priority, NamedLock locks[]) {
	    this.name = name;
	    this.once = once;
            this.priority = priority;
            this.locks = locks;
            this.amIDone = false;
	}

	public void terminate() {
            this.amIDone = true;
        }

	public String getName() {
	    return this.name;
	}

	public void run() {

          System.out.println("** "+name+" begins");
	  boolean intStatus = Machine.interrupt().disable();
	  ThreadedKernel.scheduler.setPriority(KThread.currentThread(),this.priority); 
	  Machine.interrupt().restore(intStatus);
          while(amIDone == false) {
/*
            for (int i=0; i < locks.length; i++) {
              System.out.println(this.name+" trying to acquire "+
                                 locks[i].getName());
              System.out.println(this.name+" priority="+ this.priority);
              locks[i].acquire();
              System.out.println(this.name+" has acquired "+
                                    locks[i].getName());
	      KThread.yield();
            }
  
            long wakeTime = Machine.timer().getTime() + 20000;
            while (wakeTime > Machine.timer().getTime()) { 
              KThread.yield();
            }

            for (int i=locks.length-1; i >= 0; i--) {
              System.out.println(this.name+" about to release "+
                                 locks[i].getName());
              locks[i].release();
              System.out.println(this.name+" has released "+
                                 locks[i].getName());
	      KThread.yield();
            }*/

		KThread.yield();
            if (once) {
              break;
            }
          }

          System.out.println("** "+name+" exits");
        }

	private String name;		            
        private boolean once;		 
        private int priority;		
        private NamedLock locks[];      
	private boolean amIDone;	
    }
    public static void selfTest(){
	System.out.println("#### Priority Donation test #1 ####");
        System.out.println("    This test succeeds if there is no deadlock. Note that due\n"+
       		           "    to randomness, the test may succeed many times and then fails.\n"+
       		           "    So you want to run it many, many times\n");
        NamedLock[] locks = new NamedLock[1];
	locks[0] = new NamedLock("lock0");
        PriorityDonationWorker workerMi = 
		new PriorityDonationWorker("M-Priority",
		   			   false,6,new NamedLock[0]);
        PriorityDonationWorker workerLo = 
                new PriorityDonationWorker("L-Priority",
                                           false,2,locks);
        PriorityDonationWorker workerHi = 
                new PriorityDonationWorker("H-Priority",
                                           true,7,locks);

        KThread threadMi = new KThread(workerMi);
        threadMi.setName(workerMi.getName());
        KThread threadLo = new KThread(workerLo);
        threadLo.setName(workerLo.getName());
        KThread threadHi = new KThread(workerHi);
        threadHi.setName(workerHi.getName());
	threadLo.fork();
        new Alarm().waitUntil(500);
	threadMi.fork();
        new Alarm().waitUntil(500);
	threadHi.fork();
        threadHi.join();
        workerMi.terminate();
        threadMi.join();

        workerLo.terminate();
        threadLo.join();

	System.out.println("#### Priority Donation test #1 ends ####\n");
    }

}
