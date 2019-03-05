package it.uniroma2.dicii.progettopmcsn;

/* Statistics object encapsulates the statistic evaluated for the system under simulation */
public class Statistics {

	public long c1Count;								// Number of completion of class 1 tasks on cloudlet
	public long c2Count;								// Number of completion of class 2 tasks on cloudlet
	public long C1Count;								// Number of completion of class 1 tasks on cloud
	public long C2Count;								// Number of completion of class 2 tasks on cloud
	public long c2Interrupted;							// Number of interruption of class 2 tasks on cloudlet
	public double totalResponseTimeClass2Interrupted;	// Sum of response time of class 2 interrupted tasks
	public double n1Area;								// Time-integrated number of class 1 tasks on cloudlet
	public double n2Area;								// Time-integrated number of class 2 tasks on cloudlet
	public double N1Area;								// Time-integrated number of class 1 tasks on cloud
	public double N2Area;								// Time-integrated number of class 2 tasks on cloud
	public double n2AreaEff;							// Time-integrated number of class 2 tasks that completes on cloudlet 
	
	public Statistics() {
		
		this.c1Count = 0;
		this.c2Count = 0;
		this.C1Count = 0;
		this.C2Count = 0;
		this.c2Interrupted = 0;
		this.totalResponseTimeClass2Interrupted = 0;
		this.n1Area = 0;
		this.n2Area = 0;
		this.N1Area = 0;
		this.N2Area = 0;
		this.n2AreaEff = 0;
	}
	
	
}
