package nachos.threads;

import nachos.machine.*;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.SortedSet;
/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    public Alarm() {
    WaitingThread=new TreeSet<Tuple>();
	Machine.timer().setInterruptHandler(new Runnable() {
		public void run() { timerInterrupt(); }
	    });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
        long time=Machine.timer().getTime();
        if(WaitingThread.isEmpty()||time<=WaitingThread.first().time)return;
        while(!WaitingThread.isEmpty()&&WaitingThread.first().time<time){
            Tuple w=WaitingThread.first();
            w.t.ready();
            WaitingThread.remove(w);
        }
//        KThread.currentThread().yield();
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
	// for now, cheat just to get something working (busy waiting is bad)
    if(x==0) return;
	long wakeTime = Machine.timer().getTime() + x;
	boolean intStatus=Machine.interrupt().disable();
    Tuple t=new Tuple(wakeTime, KThread.currentThread());
    WaitingThread.add(t);
    KThread.sleep();
    Machine.interrupt().restore(intStatus);
    }
    private TreeSet<Tuple> WaitingThread;
    private class Tuple implements Comparable{
    long time;
    KThread t;
    Tuple(long time, KThread t){
        this.time=time;
        this.t=t;
    }
    public int compareTo(Object o){
        Tuple tuple=(Tuple) o;
        if(time<tuple.time)return -1;
        if(time>tuple.time)return 1;
        return t.compareTo(tuple.t);
    }
}
    }

