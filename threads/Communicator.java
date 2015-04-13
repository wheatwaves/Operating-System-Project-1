package nachos.threads;

import nachos.machine.*;
import java.util.Random;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
        lock=new Lock();
        semaphore=new Semaphore(0);
        SpeakerCondition=new Condition(lock);
        ListenerCondition= new Condition(lock);
    }
    public static void selfTest(){
	System.out.println("---Communicator---");
	System.out.println("---test1---");
	n = 2;
	C = new Communicator();
	new KThread(new Runnable(){
		public void run(){
			int a = random.nextInt();
			System.out.println("speaker: "+a);
			C.speak(a);
			n--;
		}
	}).fork();
	new KThread(new Runnable(){
		public void run(){
			System.out.println("listener: "+C.listen());
			n--;
		}
	}).fork();
	while (n > 0) KThread.currentThread().yield();
	System.out.println("---test2---");
	n = 10;
	C = new Communicator();
	for (int i = 0; i < 5; i++)
		new KThread(new Runnable(){
		public void run(){
			int a = random.nextInt();
			System.out.println("speaker: "+a);
			C.speak(a);
			n--;
		}
		}).fork();
	for (int i = 0; i < 5; i++)
		new KThread(new Runnable(){
		public void run(){
			System.out.println("listener: "+C.listen());
			n--;
		}
		}).fork();
	while (n > 0) KThread.currentThread().yield();
	System.out.println("---test3---");
	n = 10;
	C = new Communicator();
	for (int i = 0; i < 5; i++){
		new KThread(new Runnable(){
		public void run(){
			int a = random.nextInt();
			System.out.println("speaker: "+a);
			C.speak(a);
			n--;
		}
		}).fork();
		new KThread(new Runnable(){
		public void run(){
			System.out.println("listener: "+C.listen());
			n--;
		}
		}).fork();

	}
	while (n > 0) KThread.currentThread().yield();
	System.out.println("---test4---");
	n = 2;
	C = new Communicator();
	new KThread(new Runnable(){
		public void run(){
			System.out.println("listener: "+C.listen());
			n--;
		}
	}).fork();
	new KThread(new Runnable(){
		public void run(){
			int a = random.nextInt();
			System.out.println("speaker: "+a);
			C.speak(a);
			n--;
		}
	}).fork();
	while (n > 0) KThread.currentThread().yield();
	System.out.println("---test5---");
	n = 10;
	C = new Communicator();
	for (int i = 0; i < 5; i++)
		new KThread(new Runnable(){
		public void run(){
			System.out.println("listener: "+C.listen());
			n--;
		}
		}).fork();
	for (int i = 0; i < 5; i++)
		new KThread(new Runnable(){
		public void run(){
			int a = random.nextInt();
			System.out.println("speaker: "+a);
			C.speak(a);
			n--;
		}
		}).fork();
	while (n > 0) KThread.currentThread().yield();
	System.out.println("---test6---");
	n = 10;
	C = new Communicator();
	for (int i = 0; i < 5; i++){
		new KThread(new Runnable(){
		public void run(){
			System.out.println("listener: "+C.listen());
			n--;
		}
		}).fork();
		new KThread(new Runnable(){
		public void run(){
			int a = random.nextInt();
			System.out.println("speaker: "+a);
			C.speak(a);
			n--;
		}
		}).fork();
	}
	while (n > 0) KThread.currentThread().yield();
    }
    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int word) {
        lock.acquire();
        speaker+=1;
        while(flag||listener==0){
            SpeakerCondition.sleep();
        }
        flag=true;
        this.word=word;
        ListenerCondition.wakeAll();
        speaker-=1;
        lock.release();
        semaphore.P();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
        lock.acquire();
        listener++;
        while(flag==false){
            SpeakerCondition.wakeAll();
            ListenerCondition.sleep();
        }
        int word=this.word;
        flag=false;
        listener--;
        semaphore.V();
        lock.release();
	    return word;
    }
    int listener=0;
    int speaker=0;
    int word=0;
    boolean flag=false;
    Lock lock;
    Semaphore semaphore;
    Condition SpeakerCondition;
    Condition ListenerCondition;
    public static Communicator C;
    public static Random random = new Random();
    public static int n;
}
