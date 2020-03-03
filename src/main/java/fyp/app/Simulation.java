package fyp.app;

import fyp.app.entities.Component;
import fyp.app.entities.Count;
import fyp.app.entities.Fitness;
import fyp.app.objects.*;
import fyp.app.util.*;

import java.util.*;


public class Simulation {
	private static Vector<Firm> firms;
	public static Landscape landscape;
	private static String[] commonResourceConfig;

	private static void printLandsacpe(){
		System.out.println("\n***** Simulation.java: Landscape ******");
		 for (int i = 0; i < (int)(Math.pow(2, Globals.getN())); i++) {
			 System.out.                                                     println(Location.getLocationStringFromInt(i) + "\t" + landscape.getFitness(Location.getLocationFromInt(i)));
		 }
	}

	private static void printComponentRelationship(){
		System.out.println("\n<<<< LENDING FIRMS >>>>>");
		for(int i = 0; i < Globals.getComponents().size(); i ++){
			System.out.print("Component "+i+": ");
			if(Globals.getSharingFirmsForComponent(i) == null || Globals.getSharingFirmsForComponent(i).size() == 0) continue;
			for (Firm f: Globals.getSharingFirmsForComponent(i)){
				System.out.print(f.getFirmID()+", ");
			}
			System.out.println();
		}
		System.out.println("<<<< BORROWING FIRMS >>>>>");
		for(Firm f: Globals.getFirms()){
			System.out.print("Firm "+f.getFirmID()+" ");
			for (Map.Entry<Integer, Integer> entry : f.getBorrowedComponents().entrySet()) {
				System.out.print("component: "+ entry.getKey() + ": Firm ID = " + entry.getValue().toString()+"; ");
			}
			System.out.println();
		}
	}
	
	public static void main(String[] args) {
		DBConnector dbConnector = new DBConnector();
		// LANDSCAPE INITIALIZATION
		String inputFile, outputFile, inf;
		int iterations, time;
		if(args.length == 0) inputFile = "in/in1.conf";
		else inputFile = args[0];
		if(args.length < 1) outputFile = "out/out0_1_1.txt";
		else outputFile = args[1];
		if(args.length < 2) iterations = 5;
		else iterations = Integer.parseInt(args[2]);
		if(args.length < 3) time = 0;
		else time = Integer.parseInt(args[3]);
		if(args.length < 4) inf = "12";
		else inf = args[4];

		FileIO.loadParameters(inputFile, outputFile, iterations, "matrix"+inf);
		commonResourceConfig = new String[Globals.getN()];
		landscape  = new Landscape(0, new InfluenceMatrix(Globals.getInfluenceMatrix()));

		// COMPONENT INITIALIZATION
//		Globals.out.println("T\t"+time+"\t"+iterations+"\t"+inf+"\t"+inputFile+"\t"+outputFile);
		Globals.out.println("T\t"+inf);
		Globals.setComponents();
		Globals.out.print("C\t"+ Globals.getComponents().size()+"\t");
		int index = 0;
		for(List<Integer> c: Globals.getComponents()){
			Globals.out.print(c.size() + "\t");
//			dbConnector.saveComponent(new Component(time, inputFile, c.size(), index));
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
		Globals.setFirms(firms);
//		Globals.out.println("F\t"+firms.size());
		summarizeCommonResourceConfig();
		Globals.initializeSharingFirm();
		System.out.println("\nPeriod: "+"-1"+" ====== Lending"); // output
		for(Firm f: firms){
			f.componentOperations(2);
		}

		/**
		 *  RUN ITERATIONS
		 */
		for (int t = 0; t < Globals.getIterations(); t++) {
			// keep record of previous round resource configuration (used in switching/borrowing)
			for (Firm f : firms) {
				f.keepRecord();
			}

			System.out.println("\nPeriod: "+t+" ====== Changing/Adding/Dropping Resources"); // output
			for (Firm f : firms) {
				f.makeDecision();
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
			// modify global sharing firm list
			System.out.println("\nPeriod: "+t+" ====== Lending"); // output
			for(Firm f: firms){
				f.componentOperations(2);
			}

			System.out.println("Syncing Components >>>");
			// sync components: ceases to lend, update changed resources in borrowed component
			for(Firm f: firms){
				f.syncComponent();
			}

			printComponentRelationship();

			summarizeCommonResourceConfig();

			Collections.sort(firms);

			// assign rankings
			int currentRank = 1;
			double currentFitness = Double.MIN_VALUE;
			for (int i = 0; i < firms.size(); i++) {
				Firm f = (Firm)firms.get(i);
				double focalFitness = f.getFitnessRanking();
				if (currentFitness == focalFitness) {
					f.setRank(currentRank);
				} else {
					currentRank = i + 1; 
					f.setRank(currentRank);
					currentFitness = focalFitness;
				}
			}

			Globals.out.println("L\t"+t+"\t"+landscape.commonConfigToString()+"\t"+landscape.getLandscapeFitness());
//			dbConnector.saveFitness(new Fitness(time, inputFile, t, landscape.getLandscapeFitness(), -1));
			System.out.println(">>>> Firm size: ");
			// output results
			for (Firm f : firms) {
				// Globals.out.println(t + "\t" + f.toStringWithFitness(landscape));
				System.out.println(f.getFirmID()+": "+f.getSize()+" "+f.printResConfig(f.getResourceConfig()));
				Globals.out.println(t + "\t" + f.getFirmID()+ "\t" + f.getRank() + "\t" + f.printResConfig(f.getResourceConfig()) + "\t" + f.getFitness()+"\t"+f.getFitnessRanking()+"\t"+f.getSize());
//				dbConnector.saveFitness(new Fitness(time, inputFile, t, f.getFitness(), f.getFirmID()));
			}
		}

		for(Map.Entry<Integer, Set<Firm>> entry: Globals.getSharingFirms().entrySet()){
			StringBuilder sb = new StringBuilder();
			sb.append("R\t"+entry.getKey());
			for(Firm f: entry.getValue()) {
				sb.append("\t"+f.getFirmID());
			}
			Globals.out.println(sb.toString());
		}


		for(Firm f: firms){
			for(Map.Entry<Integer, Integer> entry: f.getBorrowedComponents().entrySet()){
				Globals.out.println("B\t"+f.getFirmID()+"\t"+entry.getKey()+"\t"+entry.getValue());
			}
		}

		for (Firm f : firms) {
			// Globals.out.println(t + "\t" + f.toStringWithFitness(landscape));
			Globals.out.println("N\t"+f.getFirmID()+"\t"+f.printCounts()+"\t"+f.getFitness());
//			dbConnector.saveCount(new Count(time, inputFile, f.getCountExp(), f.getCountAdd(), f.getCountDrop(), f.getCountBorrow(), f.getCountSwitch(), f.getFirmID(), f.getFitness()));
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
