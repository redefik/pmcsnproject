package it.uniroma2.dicii.progettopmcsn;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

public class Algorithm2 {

	public static long THRESHOLD1 = 20; 				// Max number of task in cloudlet (N)
	public static long THRESHOLD2 = 20; 				// Threshold for class 2 tasks preemption on cloudlet (S)
	public static double START = 0.0;					// Initial instant of simulation
	public static double STOP = 500000;					// Terminal instant of simulation
	public static double INFINITY = Double.MAX_VALUE;
	public static double LAMBDA1 = 6.0;					// Class 1 arrival rate
	public static double LAMBDA2 = 6.25;				// Class 2 arrival rate
	public static double MU1CLOUD = 0.25;				// Class 1 service rate on cloud	
	public static double MU2CLOUD = 0.22;				// Class 2 service rate on cloud
	public static double MU1CLOUDLET = 0.45;			// Class 1 service rate on cloudlet
	public static double MU2CLOUDLET = 0.27;			// Class 2 service rate on cloudlet
	public static double HYPEREXPONENTIALPHASE = 0.2; 	// Parameter p of hyperexponential service
	public static double MEANSETUPTIME = 0.8;			// Mean setup time for tasks restarted on cloud
	
	public static void main (String[] args) {
		
		SystemState s = new SystemState();
		EventList e = new EventList();
		Statistics stat = new Statistics();

		// Pseudo-Random number generator initialization
		Rngs r = new Rngs();
		r.plantSeeds(123456789);
		Rvgs v = new Rvgs(r);

		// EventList initialization
		e.t = START;
		e.c1 = new ArrayList<Double>(); 
		e.c2 = new ArrayList<Double>();
		e.C1 = new ArrayList<Double>();
		e.C2 = new ArrayList<Double>();
		e.c2Arrivals = new ArrayList<Double>();
		e.a1 = e.t + getArrival(r, v, 1);
		e.a2 = e.t + getArrival(r, v, 2);
		
		// Simulation terminates when the next arrivals lie beyond STOP and all remaining tasks are completed
		while((e.a1 < STOP || e.a2 < STOP) || (s.n1 + s.n2 + s.N1 + s.N2 > 0)) {
			
			e.tNext = e.getNextEvent();
			
			// Statistics update
			stat.n1Area += (e.tNext - e.t)*(s.n1);
			stat.n2AreaEff += (e.tNext - e.t)*(s.n2);
			stat.N1Area += (e.tNext - e.t)*(s.N1);
			stat.N2Area += (e.tNext - e.t)*(s.N2);


			e.t = e.tNext;
			
			// Class 1 arrival
			if (e.t == e.a1) {
				if (s.n1 == THRESHOLD1) {
					s.N1 += 1; 										// Update cloud population
					e.C1.add(e.t + getService(r, v, 1, "cloud"));	// Schedule completion of arrived task
				} else {
					if (s.n1 + s.n2 < THRESHOLD2) {
						s.n1 += 1;										 // Update cloudlet population
						e.c1.add(e.t + getService(r, v, 1, "cloudlet")); // Schedule completion of arrived task
					} else {
						if (s.n2 > 0) {
							s.n1 += 1; 										 // Update cloudlet population
							e.c1.add(e.t + getService(r, v, 1, "cloudlet")); // Schedule completion of arrived task
							
							double arrival;
							double completion;
							double residualServiceTimeCloudlet;
							double residualServiceTimeCloud;
							double setup;
							double responseTime;
							
							arrival = Collections.min(e.c2Arrivals);
							// Get associated completion of interrupted task arrival
							completion = e.c2.get(e.c2Arrivals.indexOf(arrival)); 		
							residualServiceTimeCloudlet = completion - e.t;
							
							// Completion and arrival instant of interrupted task are removed from the corresponding list
							e.c2.remove(e.c2Arrivals.indexOf(arrival));
							e.c2Arrivals.remove(arrival);

							stat.n2AreaEff -= (e.t - arrival); // Statistic update
							
							s.n2 -= 1; 						   // Update cloudlet population
							s.N2 += 1; 						   // Update cloud population
							setup = getSetup(r,v);
							
							// Heuristic for generation of interrupted tasks completion is applied
							residualServiceTimeCloud = residualServiceTimeCloudlet * (MU2CLOUDLET/MU2CLOUD);
							e.C2.add(e.t + setup + residualServiceTimeCloud); 	// Schedule completion of interrupted task
							
							// Statistics update
							responseTime = ((completion - arrival) - residualServiceTimeCloudlet) + setup + residualServiceTimeCloud;
							stat.c2Interrupted += 1;
							stat.totalResponseTimeClass2Interrupted += responseTime;
							
						} else {
							s.n1 += 1;											// Update cloudlet population
							e.c1.add(e.t  + getService(r, v, 1, "cloudlet"));	// Schedule completion of arrived task
						}
					}					
				}
				
				e.a1 = e.t + getArrival(r, v, 1); 	// Schedule next class 1 arrival
				// If next class 1 arrival lies beyond STOP the corresponding event is marked as impossible
				if (e.a1 > STOP) {
					e.a1 = INFINITY;
				}	
			}
			
			// Class 2 arrival
			if (e.t == e.a2) {
				if (s.n1 + s.n2 >= THRESHOLD2) {
					s.N2 += 1;  										// Update cloud population
					e.C2.add(e.t + getService(r, v, 2, "cloud"));		// Schedule completion of arrived task
				} else {
					s.n2 += 1;											// Update cloudlet population
					e.c2.add(e.t + getService(r, v, 2, "cloudlet"));	// Schedule completion of arrived task
					e.c2Arrivals.add(e.a2);								// Update arrivals list of preemptible class 2 tasks 
				}
				
				e.a2 = e.t + getArrival(r, v, 2);	// Schedule next class 2 arrival
				// If next class 2 arrival lies beyond STOP the corresponding event is marked as impossible
				if (e.a2 > STOP) {
					e.a2 = INFINITY;
				}
			}
			
			// Class 1 completion on cloudlet
			if (e.t == e.getMinCompletions(e.c1)) {
				s.n1 -= 1;			// Update cloudlet population
				e.c1.remove(e.t);
				stat.c1Count += 1;	// Statistic update 
			}
			
			// Class 2 completion on cloudlet
			if (e.t == e.getMinCompletions(e.c2)) {
				s.n2 -= 1;			// Update cloudlet population
				// Arrival instant of preemptible task is removed from the corresponding list
				e.c2Arrivals.remove(e.c2.indexOf(e.t));
				e.c2.remove(e.t);
				stat.c2Count += 1;	// Statistic update
			}
			
			// Class 1 completion on cloud
			if (e.t == e.getMinCompletions(e.C1)) {
				s.N1 -= 1;			// Update cloud population
				e.C1.remove(e.t);
				stat.C1Count += 1;	// Statistic update
			}
			
			// Class 2 completion on cloud
			if (e.t == e.getMinCompletions(e.C2)) {
				s.N2 -= 1;			// Update cloud population
				e.C2.remove(e.t);
				stat.C2Count += 1;	// Statistic update
			}
		}
		
		// Printing of simulation results
		DecimalFormat f = new DecimalFormat("###0.00000");
		long totalTasks = stat.c1Count + stat.C1Count + stat.c2Count + stat.C2Count;

		System.out.println("\nFor " + totalTasks + " jobs");
		System.out.println("   Throughput Cloud Class 1 ................... = " + f.format(stat.C1Count/e.t));
		System.out.println("   Throughput Cloud Class 2 ................... = " + f.format(stat.C2Count/e.t));
		System.out.println("   Throughput Cloudlet Class 1 ................ = " + f.format(stat.c1Count/e.t));
		System.out.println("   Throughput Cloudlet Class 2 ................ = " + f.format(stat.c2Count/e.t));
		System.out.println("   Throughput Cloudlet ........................ = " + f.format(stat.c1Count/e.t + stat.c2Count/e.t));
		System.out.println("   Throughput Cloud ........................... = " + f.format(stat.C1Count/e.t + stat.C2Count/e.t));
		System.out.println("   Throughput Class 1 ......................... = " + f.format(stat.c1Count/e.t + stat.C1Count/e.t));
		System.out.println("   Throughput Class 2 ......................... = " + f.format(stat.c2Count/e.t + stat.C2Count/e.t));
		System.out.println("   Throughput Global .......................... = " + f.format(totalTasks/e.t));
		System.out.println("   Average Response Time Cloud Class 1 ........ = " + f.format(stat.N1Area/stat.C1Count));
		System.out.println("   Average Response Time Cloud Class 2 ........ = " + f.format(stat.N2Area/stat.C2Count));
		System.out.println("   Average Response Time Cloudlet Class 1 ..... = " + f.format(stat.n1Area/stat.c1Count));
		System.out.println("   Average Response Time Cloudlet Class 2 ..... = " + f.format(stat.n2AreaEff /stat.c2Count));
		System.out.println("   Average Response Time Cloudlet ............. = " + f.format((stat.n1Area + stat.n2AreaEff)/(stat.c1Count + stat.c2Count)));
		System.out.println("   Average Response Time Cloud ................ = " + f.format((stat.N1Area+stat.N2Area)/(stat.C1Count + stat.C2Count)));
		System.out.println("   Average Response Time Class 1 .............. = " + f.format((stat.N1Area + stat.n1Area)/(stat.C1Count + stat.c1Count)));
		System.out.println("   Average Response Time Class 2 .............. = " + f.format((stat.n2AreaEff +stat.N2Area)/(stat.C2Count + stat.c2Count)));
		System.out.println("   Average Response Time Global ............... = " + f.format((stat.n1Area+stat.N2Area+stat.n2AreaEff +stat.N1Area)/totalTasks));
		System.out.println("   Average Population Cloud Class 1 ........... = " + f.format(stat.N1Area/e.t));
		System.out.println("   Average Population Cloud Class 2 ........... = " + f.format(stat.N2Area/e.t));
		System.out.println("   Average Population Cloudlet Class 1 ........ = " + f.format(stat.n1Area/e.t));
		System.out.println("   Average Population Cloudlet Class 2 ........ = " + f.format(stat.n2AreaEff /e.t));
		System.out.println("   Average Population Class 1 ................. = " + f.format((stat.N1Area + stat.n1Area)/e.t));
		System.out.println("   Average Population Class 2 ................. = " + f.format((stat.n2AreaEff +stat.N2Area)/e.t));
		System.out.println("   Average Population Cloud ................... = " + f.format((stat.N1Area+stat.N2Area)/e.t));
		System.out.println("   Average Population Cloudlet ................ = " + f.format((stat.n1Area + stat.n2AreaEff)/e.t));
		System.out.println("   Average Population Global .................. = " + f.format((stat.n1Area+stat.N2Area+stat.n2AreaEff +stat.N1Area)/e.t));
		System.out.println("   Fraction of Class 2 Interrupted Tasks ...... = " + f.format((double)stat.c2Interrupted/(stat.c2Count + stat.C2Count)));
		System.out.println("   Mean Response Time Class 2 Interrupted Tasks = " + f.format(stat.totalResponseTimeClass2Interrupted/stat.c2Interrupted));
		
	}
	/* getSetup generates next setup time for tasks restarted on cloud */
	private static double getSetup(Rngs r, Rvgs v) {
		
		r.selectStream(6);
		return v.exponential(MEANSETUPTIME);
	}

	/* getArrival generates next inter-arrival time for a classType task */
	public static double getArrival(Rngs r, Rvgs v,  int classType) {

		if (classType == 1) {
			r.selectStream(0);
			return v.exponential(1/LAMBDA1);
		} else {
			r.selectStream(1);
			return v.exponential(1/LAMBDA2);
		}
	}
	
	/* getService generates next service time for a classType task running on server */
	private static double getService(Rngs r, Rvgs v, int classType, String server) {
		
		if (server.equals("cloud")) {
			if (classType == 1) {
				r.selectStream(2);
				return v.exponential(1/MU1CLOUD);
			} else {
				r.selectStream(3);
				return v.exponential(1/MU2CLOUD);
			}
		} else {

			// For exponential service 
		    /*if (classType == 1) {
		    	r.selectStream(4);
		    	return v.exponential(1/MU1CLOUDLET);
		    } else {
		    	r.selectStream(5);
		    	return v.exponential(1/MU2CLOUDLET);
		    }*/

			
			if (classType == 1) {
				r.selectStream(4);
				long phase = v.bernoulli(HYPEREXPONENTIALPHASE);
				if (phase == 1) {
					return v.exponential(1/(2*HYPEREXPONENTIALPHASE*MU1CLOUDLET));
				} else {
					return v.exponential(1/(2*(1-HYPEREXPONENTIALPHASE)*MU1CLOUDLET));
				}
			} else {
				r.selectStream(5);
				long phase = v.bernoulli(HYPEREXPONENTIALPHASE);
				if (phase == 1) {
					return v.exponential(1/(2*HYPEREXPONENTIALPHASE*MU2CLOUDLET));
				} else {
					return v.exponential(1/(2*(1-HYPEREXPONENTIALPHASE)*MU2CLOUDLET));
				}
			}
		}
	}
	
}
