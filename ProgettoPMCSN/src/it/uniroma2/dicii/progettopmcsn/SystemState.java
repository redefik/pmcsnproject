package it.uniroma2.dicii.progettopmcsn;

/* SystemState object encapsulates state variables of system under simulation */

public class SystemState {

	public long n1; // Number of class 1 tasks on cloudlet
	public long n2; // Number of class 2 tasks on cloudlet
	public long N1; // Number of class 1 tasks on cloud
	public long N2; // Number of class 2 tasks on cloud
	
	public SystemState() {
		this.n1 = 0;
		this.n2 = 0;
		this.N1 = 0;
		this.N2 = 0;
	}
	
}
