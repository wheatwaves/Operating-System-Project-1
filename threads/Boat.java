package nachos.threads;
import nachos.ag.BoatGrader;

public class Boat
{
    static BoatGrader bg;
    static public boolean Boat_in_Oahu;
    static public boolean child_in_boat;
    static public int child_number_Molokai;
    static public int adult_number_Oahu;
    static public int number;
    static public Lock lock;
    static public int cc;
    static Condition2 child_Molokai, child_Oahu, adult_Oahu;
	
    public static void selfTest()
    {
	BoatGrader b = new BoatGrader();
	
	//System.out.println("\n ***Testing Boats with only 2 children***");
	//begin(0, 2, b);

	System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
  	begin(50, 60, b);

  	//System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
  	//begin(3, 3, b);
    }

    public static void begin( int adults, int children, BoatGrader b )
    {

	// Store the externally generated autograder in a class
	// variable to be accessible by children.
	bg = b;
 	Boat_in_Oahu = true;
    	child_in_boat = false;
    	child_number_Molokai = 0;
    	adult_number_Oahu = adults;
    	number = adults+children;
	cc = children;
	// Instantiate global variables here
	
	// Create threads here. See section 3.4 of the Nachos for Java
	// Walkthrough linked from the projects page.

	Runnable a = new Runnable()
	{
		public void run()
		{
			AdultItinerary();
		}
	};
	Runnable c = new Runnable()
	{
		public void run()
		{
			ChildItinerary();
		}
	};
	lock = new Lock();
	child_Molokai = new Condition2(lock);
	child_Oahu = new Condition2(lock);
	adult_Oahu = new Condition2(lock);
	for (int i = 0; i < adults; i++)
		new KThread(a).fork();
	for (int i = 0; i < children; i++)
		new KThread(c).setName("Child"+i).fork();
	while (number > 0) KThread.currentThread().yield();
	//lock.acquire();
	//child_Molokai.wakeAll();
	//lock.release();
	//while (cc > 0) KThread.currentThread().yield();
    }

    static void AdultItinerary()
    {
	bg.initializeAdult(); 
	lock.acquire();
	if (!(Boat_in_Oahu && child_number_Molokai > 0))
		adult_Oahu.sleep();
	bg.AdultRowToMolokai();
	Boat_in_Oahu = false;
	number --;
	adult_number_Oahu--;
	child_Molokai.wake();
	lock.release();
    }

    static void ChildItinerary()
    {
	bg.initializeChild(); 
	boolean self_in_Oahu = true;
	lock.acquire();
	while (number>0)
	{
		
		if (!self_in_Oahu)
		{
			bg.ChildRowToOahu();
			Boat_in_Oahu = true;
			self_in_Oahu = true;
			child_number_Molokai --;
			number++;
			if (child_number_Molokai>0 && adult_number_Oahu>0)
				adult_Oahu.wake();
			else child_Oahu.wake();
			child_Oahu.sleep();
		}
		else
		{
			if (!Boat_in_Oahu)
			{
				child_Oahu.sleep();
			}
			if (!child_in_boat)
			{
				bg.ChildRowToMolokai();
				number--;
				child_number_Molokai++;
				child_in_boat=true;
				self_in_Oahu = false;
				child_Oahu.wake();
				child_Molokai.sleep();
				if (number == 0) break;
			}
			else
			{
				bg.ChildRowToMolokai();
				number--;
				child_number_Molokai++;
				child_in_boat=false;
				Boat_in_Oahu = false;
				self_in_Oahu = false;
				child_Molokai.wake();
				child_Molokai.sleep();
				if (number == 0) break;
			}
		}
	}
	cc--;
	lock.release();
    }

    static void SampleItinerary()
    {
	// Please note that this isn't a valid solution (you can't fit
	// all of them on the boat). Please also note that you may not
	// have a single thread calculate a solution and then just play
	// it back at the autograder -- you will be caught.
	System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
	bg.AdultRowToMolokai();
	bg.ChildRideToMolokai();
	bg.AdultRideToMolokai();
	bg.ChildRideToMolokai();
    }
    
}
