package it.uniroma2.dicii.progettopmcsn;

import java.util.ArrayList;
import java.util.Collections;

/* EventList object encapsulates instants of events of next-event simulation */

public class EventList {
	
	public double t;  						// Current instant
	public double tNext; 					// Next scheduled event instant
	public double a1;						// Next class 1 arrival instant		
	public double a2;						// Next class 2 arrival instant
	public ArrayList<Double> c1;			// Next class 1 completion on cloudlet instant
	public ArrayList<Double> c2;			// Next class 2 completion on cloudlet instant			
	public ArrayList<Double> C1;			// Next class 1 completion on cloud instant 
	public ArrayList<Double> C2;			// Next class 2 completion on cloud instant 
	public ArrayList<Double> c2Arrivals; 	// List of class 2 running task on cloudlet arrival instants (only for algorithm 2)
	
	public double getMinCompletions(ArrayList<Double> completions) {
		
		// If the list of completions is empty the the the next completion is 
		// an impossible event so the corresponding instant is set to infinity
		if (completions.isEmpty()) {
			return Double.MAX_VALUE;
		} else {
			return Collections.min(completions);
		}
	}

	/* getNextEvent schedules next event generating corresponding instant */
	public double getNextEvent() {

		ArrayList<Double> events = new ArrayList<Double>();
		events.add(this.a1);
		events.add(this.a2);
		events.add(this.getMinCompletions(c1));
		events.add(this.getMinCompletions(c2));
		events.add(this.getMinCompletions(C1));
		events.add(this.getMinCompletions(C2));
		return Collections.min(events);
	}
	
}
