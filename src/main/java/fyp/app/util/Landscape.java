package fyp.app.util;

import java.util.ArrayList;

public class Landscape {
	private InfluenceMatrix im; 
	private int cases; // no need if we just use im.numCases()
	private double[] fitnessContribs;
	private double[] fitness;
	private double landscapeMaxFitness;
	private double landscapeMinFitness;
	private long landscapeID; 
	private String[] commonResourceConfig = new String[Globals.getN()];
	
	public Landscape(int lndscpID, InfluenceMatrix inf) {
		landscapeID = (long)lndscpID;
		MersenneTwisterFast rnd = new MersenneTwisterFast(landscapeID);
		im = inf;
		// im.print();
		cases = im.numCases(); // NEED?
//		System.out.println("cases: " + cases);
		fitnessContribs = new double[cases];
		for (int i = 0; i < cases; i++) {
			double d = rnd.nextDouble();
//			System.out.println(d);
			fitnessContribs[i] = d;
 		}
		setFitnessLandscape(); // sets max and min fitness levels
	}
	public long getLandscapeID() {
		return landscapeID;
	}
	
	public int numCases() {
		return im.numCases();
	}

	public void setCommonResourceConfig(int idx, String value) {
		commonResourceConfig[idx] = value;
	}

	public String getCommonResourceConfig(int idx) {
		return commonResourceConfig[idx];
	}



	public String commonConfigToString() {
		String retString = "";
		for (int i = 0; i < commonResourceConfig.length; i++) {
			retString += commonResourceConfig[i];
		}
		return retString;
	}

	private void setFitnessLandscape() {
		double maxFitness = 0d;
		double minFitness = 1d;
		double fitvalue;
		fitness = new double[(int)(Math.pow(2, Globals.getN()))];
		for (int i = 0; i < (int)(Math.pow(2, Globals.getN())); i++) {
			Location l = Location.getLocationFromInt(i);
			// fitvalue = getFitness(l);
			// Globals.out.println(l.toString() + "\t" + fitvalue);
			fitvalue = getFitness(Location.getLocationFromInt(i));
			if (fitvalue > maxFitness) {
				maxFitness = fitvalue;
			}
			if (fitvalue < minFitness) {
				minFitness = fitvalue;
			}
		}
		landscapeMinFitness = minFitness;
		landscapeMaxFitness = maxFitness;
		// System.out.println("Min: " + landscapeMinFitness);
		// System.out.println("Max: " + landscapeMaxFitness);
	}

	// private void setFitnessLandscape() {
	// 	// set min and max of landscape for normalized fitness
	// 	double maxFitness = 0d;
	// 	double minFitness = 1d;
	// 	fitness = new double[(int)(Math.pow(2, Globals.N))];
	// 	for (int i = 0; i < (int)(Math.pow(2, Globals.N)); i++) {
	// 		double fitvalue = getFitness(getConfigFromInt(i));
	// 		if (fitvalue > maxFitness) {
	// 			maxFitness = fitvalue;
	// 		}
	// 		if (fitvalue < minFitness) {
	// 			minFitness = fitvalue;
	// 		}
	// 	}
	// 	landscapeMaxFitness = maxFitness;
	// 	landscapeMinFitness = minFitness;
	// 	System.out.println("Min: " + landscapeMinFitness);
	// 	System.out.println("Max: " + landscapeMaxFitness);

	// }

	private String[] getConfigFromInt(int num) {
		String loc = Integer.toBinaryString(num);
		String config[] = new String[Globals.getN()];
		int zeros = Globals.getN() - loc.length();
		for (int j = 0; j < zeros; j++) {
			loc = "0" + loc;
		}
		char[] locArray = loc.toCharArray();
		for (int i = 0; i < config.length; i++) {
			config[i] = String.valueOf(locArray[i]);
		}
		return config;
	}

	public double getLandscapeFitness() {
		return this.getFitness(commonResourceConfig);
	}

	/**
	 * returns fitness for a position with cognitive simplification
	 * @param l
	 * @return
	 */
	public double getFitness(Location l) {
		double fitness = 0d;
		for (int i = 0; i < Globals.getN(); i++) {
			String s = l.getLocationAt(i, im);
			double d = getFitnessContribution(s, i);
			fitness += d;
		}
		fitness = fitness / Globals.getN();
		return fitness;
	}
	
	public double getFitness(String[] resConfig) {
		double fitness = 0.0d;
		//CALCULATE FITNESS
		String[] config = new String[Globals.getN()];
		// replace empty settings with commonResourceConfig
		for (int i = 0; i < resConfig.length; i++) {
			if (resConfig[i].equals(" ")) {
				config[i] = commonResourceConfig[i];
			} else {
				config[i] = resConfig[i];
			}
			// System.out.print(config[i]);
		}

		Location loc = new Location(config);

		/* 
		 * the case where all resources (even those that aren't used by the firm) contribute to fitness
		 */
		// for (int i = 0; i < Globals.getN(); i++) {
		// 	String s = loc.getLocationAt(i, im);
		// 	fitness += getFitnessContribution(s, i);
		// }
		// fitness = fitness / Globals.getN();

		/* 
		 * the case where only resources used by the firm contribute to fitness
		 */

		for (int i = 0; i < Globals.getN(); i++) {
			String s = loc.getLocationAt(i, im);
			if (!resConfig[i].equals(" ")) {
				fitness += getFitnessContribution(s, i);
			} 
		}
		fitness = fitness / Globals.getN();

		// return fitness;
		// Return Normalized fitness (fitness - landscapeMinFitness) / (landscapeMaxFitness - landscapeMinFitness)
		// return (fitness - landscapeMinFitness) / (landscapeMaxFitness - landscapeMinFitness);
		return (fitness / landscapeMaxFitness);
	}
	
	/**
	 * return fitness for a position as per almirall and casadesus-masanell
	 * @param l
	 * @param know boolean array of which elements the DMU has knowledge e.g., [true, true, true, true, false, true, true, false] for Business
	 * @return fitness value 
	 */
	public double getFitness(Location l, boolean[] know) {
		double fitness = 0d;
		for (int i = 0; i < Globals.getN(); i++) {
			if (know[i]) {
				String s = l.getLocationAt(i, im);
				double d = getFitnessContribution(s, i);
				fitness += d;
			}
		}
		fitness = fitness / Globals.getN();
		return fitness;
	}

	// NO NEED TO SET FITNESS -> MUST BE CALCULATED ON THE FLY WITH CONSUMER WEIGHTS
//	private void setFitnessLandscape() {
//		double maxFitness = 0d;
//		fitness = new double[(int)(Math.pow(2, Globals.N))];
//		for (int i = 0; i < fitness.length; i++) {
//			fitness[i] = getFitness(Location.getLocationFromInt(i));
//			if (fitness[i] > maxFitness) {
//				maxFitness = fitness[i];
//			}
//		}
//		landscapeMaxFitness = maxFitness;
//	}
	
	/**
	 * returns the maximum fitness value for the landscape
	 */
	public double getMaxFitness() {
		return landscapeMaxFitness;
	}

    /**
     * used by 
     * private method that sets the fitness contribution for a particular 
     * location (string representation)   
     */
	private double getFitnessContribution(String locationString, int place) {
		// since locationString may contain a " ", we need to fill those spaces with 1s and 0s
		// i.e., if space exists, remove entry and replace with combinations of 0/1 replacing the space
		ArrayList<String> list = new ArrayList<String>();
		list.add(locationString);
		
		boolean done = false;
		while (!done) {
			boolean found = false;
			for (int i = 0; i < list.size(); i++) {
				String listItem = list.get(i);
				int idx = listItem.indexOf(' '); // position of ' ' in string listItem
				if (idx > -1) { // found ' '
					found = true;
					list.remove(i);
					char[] stringArray = listItem.toCharArray();
					String s0 = ""; String s1 = "";
					for (int j = 0; j < stringArray.length; j++) {
						if (idx == j) {
							s0 += "0"; s1 += "1";
						} else {
							s0 += stringArray[j]; s1 += stringArray[j];
						}
					}
					list.add(s0); list.add(s1);
					break;
				} 
			} 
			if (!found) { done = true; }
		}
		// now list should contain all combinations of matching policy choices (e.g., "  1" -> "001", "011", "101", "111")
		
		double retVal = 0d; int cnt = 0;
		for (int i = 0; i < list.size(); i++) {
			String listItem = list.get(i);
			int fitnessContribIndex = im.getStartPosition(place) + Integer.parseInt(listItem, 2);
			double d = fitnessContribs[fitnessContribIndex];
//			System.out.println(listItem + "\t" + fitnessContribIndex + "\t" + d);
			retVal += d;
			cnt++;
		}
		retVal = retVal / cnt; // get average
		return retVal;
	}
	
    /**
     * Utility for printing all possible fitness values for given landscape   
     */
	// public void printLandscapeFitness() {
	// 	for (int i = 0; i < fitness.length; i++) {
	// 		System.out.println(Location.getLocationStringFromInt(i) + "\t" + fitness[i]);
	// 	}
	// }
	
	public void printFitnessContributions() {
		for (int i = 0; i < fitnessContribs.length; i++) {
			Globals.out.println(i + "\t" + fitnessContribs[i]);
		}
	}
    /**
     * main method for testing purposes only.  
     */

	public static void main(String args[]) {
		InfluenceMatrix infmat = new InfluenceMatrix("inf/n8k1.txt");
		Landscape l = new Landscape(0, infmat);
//		String[] locationStringArray = {" ", "0", "1", "1", "0", " ", "1", "1", "0", "0"}; // 0011001100
//		String[] locationStringArray = {" ", "0", "1", "1", "0", " ", " ", "0", "1", "1", "0", " ", " ", "0", "1", "1"}; // 0011001100
//		String[] locationStringArray = {"0", "1", "0", "1", "0", "0", "0", "0", "0", "1", "0", "1", "0", "0", "0", "1"};
//		String[] locationStringArray = {"0", "1", "0", "1", "0", "0", "0", "0", " ", " ", " ", " ", " ", " ", " ", " "};
//		String[] n1 = {"1", "1", "0", "1", "0", "0", "0", "0", " ", " ", " ", " ", " ", " ", " ", " "}; //11010000
//		String[] n2 = {"0", "0", "0", "1", "0", "0", "0", "0", " ", " ", " ", " ", " ", " ", " ", " "}; //00010000
//		String[] n3 = {"0", "1", "1", "1", "0", "0", "0", "0", " ", " ", " ", " ", " ", " ", " ", " "}; //01110000
//		String[] n4 = {"0", "1", "0", "0", "0", "0", "0", "0", " ", " ", " ", " ", " ", " ", " ", " "}; //01000000
//		String[] n5 = {"0", "1", "0", "1", "1", "0", "0", "0", " ", " ", " ", " ", " ", " ", " ", " "}; //01011000
//		String[] n6 = {"0", "1", "0", "1", "0", "1", "0", "0", " ", " ", " ", " ", " ", " ", " ", " "}; //01010100
//		String[] n7 = {"0", "1", "0", "1", "0", "0", "1", "0", " ", " ", " ", " ", " ", " ", " ", " "}; //01010010
//		String[] n8 = {"0", "1", "0", "1", "0", "0", "0", "1", " ", " ", " ", " ", " ", " ", " ", " "}; //01010001
//		String[] n1 = {"1", " ", " ", "1", " ", "0", "0", "0"}; //11010000
//		String[] n2 = {"0", "0", "0", "1", "0", "0", "0", "0"}; //00010000
//		String[] n3 = {"0", "1", "1", "1", "0", "0", "0", "0"}; //01110000
//		String[] n4 = {"0", "1", "0", "0", "0", "0", "0", "0"}; //01000000
//		String[] n5 = {"0", "1", "0", "1", "1", "0", "0", "0"}; //01011000
//		String[] n6 = {"0", "1", "0", "1", "0", "1", "0", "0"}; //01010100
//		String[] n7 = {"0", "1", "0", "1", "0", "0", "1", "0"}; //01010010
//		String[] n8 = {"0", "1", "0", "1", "0", "0", "0", "1"}; //01010001
//		Location loc = new Location(locationStringArray);
//		Location n1loc = new Location(n1);
//		Location n2loc = new Location(n2);
//		Location n3loc = new Location(n3);
//		Location n4loc = new Location(n4);
//		Location n5loc = new Location(n5);
//		Location n6loc = new Location(n6);
//		Location n7loc = new Location(n7);
//		Location n8loc = new Location(n8);
//		System.out.println(l.getFitness(loc));
//		System.out.println("finding out test fitness");
//		System.out.println(l.getFitness(n1loc));
//		System.out.println(l.getFitness(n2loc));
//		System.out.println(l.getFitness(n3loc));
//		System.out.println(l.getFitness(n4loc));
//		System.out.println(l.getFitness(n5loc));
//		System.out.println(l.getFitness(n6loc));
//		System.out.println(l.getFitness(n7loc));
//		System.out.println(l.getFitness(n8loc));
//		l.printLandscapeFitness();
//		System.out.println();
		l.printFitnessContributions();
	}
}