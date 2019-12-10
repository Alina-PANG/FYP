package fyp.app;

import fyp.app.entities.Component;
import fyp.app.entities.Count;
import fyp.app.entities.Fitness;
import fyp.app.objects.*;
import fyp.app.util.*;

import java.util.Collections;
import java.util.List;
import java.util.Vector;


public class Simulation {
	private static Vector<Firm> firms;
	public static Landscape landscape;
	private static String[] commonResourceConfig;

	private static void printLandsacpe(){
		System.out.println("\n***** Simulation.java: Landscape ******");
		 for (int i = 0; i < (int)(Math.pow(2, Globals.getN())); i++) {
			 System.out.println(Location.getLocationStringFromInt(i) + "\t" + landscape.getFitness(Location.getLocationFromInt(i)));
		 }
	}
	
	public static void main(String[] args) {
		DBConnector dbConnector = new DBConnector();
		// LANDSCAPE INITIALIZATION
		String inputFile, outputFile, inf;
		int iterations, time;
		if(args.length == 0) inputFile = "in/in1.conf";
		else inputFile = args[0];
		if(args.length < 1) outputFile = "out/out1.txt";
		else outputFile = args[1];
		if(args.length < 2) iterations = 5;
		else iterations = Integer.parseInt(args[2]);
		if(args.length < 3) time = 0;
		else time = Integer.parseInt(args[3]);
		if(args.length < 4) inf = "matrix0";
		else inf = args[4];

		FileIO.loadParameters(inputFile, outputFile, iterations, inf);
		commonResourceConfig = new String[Globals.getN()];
		landscape  = new Landscape(0, new InfluenceMatrix(Globals.getInfluenceMatrix()));

		// COMPONENT INITIALIZATION
		Globals.out.println("T\t"+time+"\t"+iterations+"\t"+inf+"\t"+inputFile+"\t"+outputFile);
		Globals.setComponents();
		Globals.out.print("C\t"+ Globals.getComponents().size()+"\t");
		int index = 0;
		for(List<Integer> c: Globals.getComponents()){
			Globals.out.print(c.size() + "\t");
			dbConnector.saveComponent(new Component(time, inputFile, c.size(), index));
			index ++;
		}
		Globals.out.println();

		// FIRM INITIALIZATION
		int firmID = 0;
		firms = new Vector<Firm>();

		for (int i = 0; i < Globals.getNumFirmTypes(); i++) {
			for (int j = 0; j < Globals.getNumFirmsForType(i); j++) {
				firms.add(new Firm(i, firmID, Globals.getInitResourcesForType(i),
					Globals.getInnovationForType(i), Globals.getResourcesIncrementForType(i),
					Globals.getSearchScopeForType(i), Globals.getSearchThresholdForType(i),
					Globals.getSearchForType(i), Globals.getResourceDecisionForType(i),
					Globals.getResourceThresholdForType(i),

					Globals.getComponentBorrowingInnovationForType(i),
					Globals.getComponentBorrowingThresholdForType(i),

					Globals.getComponentSwitchingInnovationForType(i),
					Globals.getComponentSwitchingThresholdForType(i),

					Globals.getComponentLendingInnovationForType(i),
					Globals.getComponentLendingThresholdForType(i)));
				firmID++;
			}
		}
		Globals.out.println("F\t"+firms.size());
		summarizeCommonResourceConfig();

		/**
		 *  RUN ITERATIONS
		 */
		for (int t = 0; t < Globals.getIterations(); t++) {
			Globals.refreshLendingFirms(); // edited: clean up the lending firm list -> maintaining cost & benefit for each period - NPV & PV issue?
			System.out.println("\nPeriod: "+t+" ====== Changing/Adding/Dropping Resources"); // output
			for (Firm f : firms) {
				f.makeDecision();
			}
			System.out.println("\nPeriod: "+t+" ====== Lending"); // output
			for (Firm f : firms) {
				f.componentOperations(2);
			}
			Globals.printSharingFirms(); // output
			System.out.println("\nPeriod: "+t+" ====== Switching"); // output
			for (Firm f : firms) {
				f.componentOperations(1);
			}
			System.out.println("\nPeriod: "+t+" ====== Borrowing"); // output
			for (Firm f : firms) {
				f.componentOperations(0);
			}

			summarizeCommonResourceConfig();

			Collections.sort(firms);

			// assign rankings
			int currentRank = 1;
			double currentFitness = 1.0d;
			for (int i = 0; i < firms.size(); i++) {
				Firm f = (Firm)firms.get(i);
				double focalFitness = f.getFitness();
				if (currentFitness == focalFitness) {
					f.setRank(currentRank);
				} else {
					currentRank = i + 1; 
					f.setRank(currentRank);
					currentFitness = focalFitness;
				}
			}

			Globals.out.println("L\t"+landscape.commonConfigToString()+"\t"+landscape.getLandscapeFitness());
//			dbConnector.saveFitness(new Fitness(time, inputFile, t, landscape.getLandscapeFitness(), -1));
			// output results
			for (Firm f : firms) {
				// Globals.out.println(t + "\t" + f.toStringWithFitness(landscape));
				Globals.out.println(t + "\t" + f.toStringFull(landscape));
				dbConnector.saveFitness(new Fitness(time, inputFile, t, f.getFitness(), f.getFirmID()));
			}
		}

		for (Firm f : firms) {
			// Globals.out.println(t + "\t" + f.toStringWithFitness(landscape));
			Globals.out.println("N\t"+f.getFirmID()+"\t"+f.printCounts());
			dbConnector.saveCount(new Count(time, inputFile, f.getCountExp(), f.getCountAdd(), f.getCountDrop(), f.getCountBorrow(), f.getCountSwitch(), f.getFirmID(), f.getFitness()));
		}
	}
	
	private static void run(int t) {
		// first summarize common resource configurations
		summarizeCommonResourceConfig();


	}

	private static void summarizeCommonResourceConfig() {
		int[] configCounts = new int[Globals.getN()];
		for (Firm f : firms) {
			for (int i = 0; i < Globals.getN(); i++) {
		        switch (f.getResourceConfigAt(i)) {
		        	case "0" : 
		        		configCounts[i]--;
		        		break;
		        	case "1" :
		        		configCounts[i]++;
		        		break;
		        	default: 
		        		break;
		        }
			}
		}
		// configCounts[i] < 0 if 0 is most common,
		// 				   > 0 if 1 is most common, 
		//				   = 0 if 0 and 1 are equally likely
		for (int i = 0; i < Globals.getN(); i++) {
			if (configCounts[i] < 0) {
				commonResourceConfig[i] = "0";
			} else if (configCounts[i] > 0) {
				commonResourceConfig[i] = "1";
			} else {
				commonResourceConfig[i] = Integer.toString(Globals.rand.nextInt(2));
			}
			landscape.setCommonResourceConfig(i, commonResourceConfig[i]);
		}
		System.out.println("\n***** Simulation.java: SUMMARIZING COMMON RESOURCE CONFIG:\t" + landscape.commonConfigToString());
	}
	
	private static String commonConfigToString() {
		String retString = "";
		for (int i = 0; i < commonResourceConfig.length; i++) {
			retString += commonResourceConfig[i];
		}
		return retString;
	}
	
	
}
