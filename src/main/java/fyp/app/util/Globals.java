package fyp.app.util;

import fyp.app.objects.Firm;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class Globals {
	private static final Logger logger = Logger.getLogger( Globals.class.getName());

	/*
	 * Default values 
	 */
	public static PrintWriter out;
	public static long runID = System.currentTimeMillis(); // need?
	public static MersenneTwisterFast rand = new MersenneTwisterFast(runID);

	/** SIMULATION PARAMETERS */
	private static int N = 16;
	private static String outfilename = "testing.txt";
	private static String influenceMatrixFile = "inf/matrix16-3.txt";
	private static int iterations = 3;
	private static int numFirmTypes;

	/** COMPONENT PARAMETERS */
	private static int maxCSize = 5;
	private static int minCSize = 3;
	private static List<List<Integer>> components;
	private static Map<Integer, Set<Firm>> sharingFirms;

	// not used
	private static int numNeeds = 10;
	private static int numConsumers = 10;

	/** FIRM PARAMETERS */
	// private static int initResources = 3;
	private static int[] numFirms; 
	private static int[] initResources;
	private static List<Firm> firms;
	// private static int numFirms = 1;
	//public static String adaptation = "resources";
	private static double[] innovation; // = 0.0d;
	private static int[] resourcesIncrement; // = 1;
	private static String[] search; // = "experiential";
	private static String[] resourceDecision; // = "absolute";
	private static double[] resourceThreshold; // = 0.05d;
	private static double[] searchThreshold; // = 0.02d;
	private static int[] searchScope; // = 1;
	private static double[] componentBorrowingInnovation;
	private static double[] componentBorrowingThreshold;
	private static double[] componentSwitchingInnovation;
	private static double[] componentSwitchingThreshold;
	private static double[] componentLendingInnovation;
	private static double[] componentLendingThreshold;

	/* setters */
	/** SIMULATION PARAMETERS */
	public static void setN(int n) {
		N = n;
	}

	public static void setInfluenceMatrix(String matrix) {
		influenceMatrixFile = "inf/" + matrix + ".txt";
	}

	public static void setIterations(int n) {
		iterations = n;
	}

	public static void setOutfile(String file) {
		outfilename = file;
		try {
			if (outfilename.equals("")) {
				// System.out.println("setting STDOUT");
				out = new PrintWriter(System.out, true);
			} else {
				out = new PrintWriter(new FileOutputStream(outfilename, true), true);
			}
		} catch (IOException io) {
			System.err.println(io.getMessage());
			io.printStackTrace();
		}

	}

	public static int getMaxCSize() {
		return maxCSize;
	}

	public static void setMaxCSize(int maxCSize) {
		Globals.maxCSize = maxCSize;
	}

	public static int getMinCSize() {
		return minCSize;
	}

	public static void setMinCSize(int minCSize) {
		Globals.minCSize = minCSize;
	}

	/** COMPONENT PARAMETERS */


	public static void setComponents() {
		System.out.println("\n**** Components *****"); // output
		Random rnd = new Random();
		List<Integer> list = new ArrayList<>();
		for(int i = 0; i < N; i ++) list.add(i);

		components = new ArrayList<List<Integer>>();
		Bag bag = new Bag(list); // save the indexes of the component. 1,3,5 -> resources at index 1,3,5 belongs to this component
		int j = 0;
		while(!bag.isEmpty()){
			int size = rnd.nextInt(maxCSize - minCSize + 1) + minCSize;
			System.out.println(j+" Component size = "+size+":"); // output
			List<Integer> component = new ArrayList<>();
			for(int i = 0; i < size && !bag.isEmpty(); i ++){
				int adding = (Integer) bag.randomPop();
				System.out.print(adding+" "); // output
				component.add(adding);
			}
			components.add(component);
			System.out.println(); // output
			j ++;
		}
	}

	public static int getComponentIndexByResIndex(int i){
		for(int j = 0; j < components.size(); j ++){
			for(int c: components.get(j)){
				if(c == i) return j;
			}
		}
		return -1;
	}

	public static List<List<Integer>> getComponents(){ return components; }

	public static List<Integer> getComponentByIndex(int i) {return components.get(i);};

	public static Firm getFirmById(int id){
		for(Firm f: firms){
			if(f.getFirmID() == id)
				return f;
		}
		return null;
	}

	public static void initializeSharingFirm(){
		sharingFirms = new HashMap<>();
	}

	public static void removeSharingFirm(int componentIndex, Firm f){
		if(sharingFirms.containsKey(componentIndex) && sharingFirms.get(componentIndex).contains(f)){
			sharingFirms.get(componentIndex).remove(f);
		}
	}

	public static void addSharingFirms(int componentIndex, Firm f) {
		if(sharingFirms.containsKey(componentIndex)){
			sharingFirms.get(componentIndex).add(f);
		} else{
			Set<Firm> list = new HashSet<>();
			list.add(f);
			sharingFirms.put(componentIndex, list);
		}
	}

	public static Set<Firm> getSharingFirmsForComponent(int i) {
		if (sharingFirms.containsKey(i)){
			return sharingFirms.get(i);
		}
		return null;
	}

	public static void printSharingFirms(){
		System.out.println("**** All sharing Firms ****");
		for(Map.Entry<Integer, Set<Firm>> entry: sharingFirms.entrySet()){
			System.out.println("Component Index:" + entry.getKey());
			Set<Firm> firms = entry.getValue();
			for(Firm f: firms) {
				System.out.print(f.getFirmID()+" ");
			}
			System.out.println();
		}
		if(sharingFirms.entrySet().size() == 0) System.out.println("null\n");
	}

	public static List<Firm> getFirms() {
		return firms;
	}

	public static void setFirms(List<Firm> firms) {
		Globals.firms = firms;
	}

	/** FIRM PARAMETERS */


	public static void setParameters(String fullParameterString, int checkNum) {
		//#firms=50,5,1,1,1,0,abs,0.2;50,15,1,1,1,0,abs,0.2
		//#firms=numFirms,initResources,innovation,resourcesIncrement,searchScope,searchThreshold,resourceDecision,resourceThreshold
		// 1.  Parse by type (delimiter = ;)
		StringTokenizer typeTokenizer = new StringTokenizer(fullParameterString, ";");
		numFirmTypes = typeTokenizer.countTokens();
		// initialize firm parameter arrays
		numFirms = new int[numFirmTypes]; 
		initResources = new int[numFirmTypes];
		innovation = new double[numFirmTypes]; // = 0.0d;
		resourcesIncrement = new int[numFirmTypes]; // = 1;
		searchScope = new int[numFirmTypes]; // = 1;
		searchThreshold = new double[numFirmTypes]; // = 0.02d;
		search = new String[numFirmTypes]; // = "experiential";
		resourceDecision = new String[numFirmTypes]; // = "absolute";
		resourceThreshold = new double[numFirmTypes]; // = 0.05d;
		componentBorrowingInnovation = new double[numFirmTypes];
		componentBorrowingThreshold = new double[numFirmTypes];
		componentSwitchingInnovation = new double[numFirmTypes];
		componentSwitchingThreshold = new double[numFirmTypes];
		componentLendingInnovation = new double[numFirmTypes];
		componentLendingThreshold = new double[numFirmTypes];

		int firmTypeNum = 0;
		while (typeTokenizer.hasMoreTokens()) {
			// 2. Parse by parameter (delimiter = ,)
			StringTokenizer firmParameterTokenizer = new StringTokenizer(typeTokenizer.nextToken().trim(), ",");
			if (firmParameterTokenizer.countTokens() == checkNum) { // HARD CODED 8 parameters numFirms,initResources,innovation,resourcesIncrement,searchScope,searchThreshold,resourceDecision,resourceThreshold; #9 is search (fixed at "experiential")
				numFirms[firmTypeNum] = Integer.parseInt(firmParameterTokenizer.nextToken().trim());
				initResources[firmTypeNum] = Integer.parseInt(firmParameterTokenizer.nextToken().trim());
				innovation[firmTypeNum] = Double.parseDouble(firmParameterTokenizer.nextToken().trim());
				resourcesIncrement[firmTypeNum] = Integer.parseInt(firmParameterTokenizer.nextToken().trim());
				searchScope[firmTypeNum] = Integer.parseInt(firmParameterTokenizer.nextToken().trim());
				searchThreshold[firmTypeNum] = Double.parseDouble(firmParameterTokenizer.nextToken().trim());
				search[firmTypeNum] = "experiential";
				resourceDecision[firmTypeNum] = firmParameterTokenizer.nextToken().trim();
				resourceThreshold[firmTypeNum] = Double.parseDouble(firmParameterTokenizer.nextToken().trim());
				componentBorrowingInnovation[firmTypeNum] = Double.parseDouble(firmParameterTokenizer.nextToken().trim());
				componentBorrowingThreshold[firmTypeNum] = Double.parseDouble(firmParameterTokenizer.nextToken().trim());
				componentSwitchingInnovation[firmTypeNum] = Double.parseDouble(firmParameterTokenizer.nextToken().trim());
				componentSwitchingThreshold[firmTypeNum] = Double.parseDouble(firmParameterTokenizer.nextToken().trim());
				componentLendingInnovation[firmTypeNum] = Double.parseDouble(firmParameterTokenizer.nextToken().trim());
				componentLendingThreshold[firmTypeNum] = Double.parseDouble(firmParameterTokenizer.nextToken().trim());
		    } else {
		    	System.err.println("INCORRECT PARAMETER COUNT ERROR: each firm type must have 8 comma-delimited parameters (numFirms,initResources,innovation,resourcesIncrement,searchScope,searchThreshold,resourceDecision,resourceThreshold)");
		    	System.exit(0);
			}
			firmTypeNum++;
		}
	}

	/* getters */
	public static int getN() {
		return N;
	}

	public static String getInfluenceMatrix() {
		return influenceMatrixFile;
	}

	public static int getIterations() {
		return iterations;
	}

	public static String getOutfilename() {
		return outfilename;
	}


	public static int getNumFirms() {
		int totalFirms = 0;
		for (int i = 0; i < numFirms.length; i++) {
			totalFirms += numFirms[i];
		}
		return totalFirms;
	}

	public static int getNumFirmTypes() {
		return numFirmTypes;
	}

	public static int getNumFirmsForType(int i) {
		return numFirms[i];
	}

	public static int getInitResourcesForType(int i) {
		return initResources[i];
	}

	public static int getNumNeeds() {
		return numNeeds;
	}

	public static int getNumConsumers() {
		return numConsumers;
	}

	// public static String getAdaptation() {
	// 	return adaptation;
	// }

	public static double getInnovationForType(int i) {
		return innovation[i];
	}

	public static int getResourcesIncrementForType(int i) {
		return resourcesIncrement[i];
	}

	public static String getSearchForType(int i) {
		return search[i];
	}

	public static int getSearchScopeForType(int i) {
		return searchScope[i];
	}

	public static String getResourceDecisionForType(int i) {
		return resourceDecision[i];
	}

	public static double getResourceThresholdForType(int i) {
		return resourceThreshold[i];
	}

	public static double getSearchThresholdForType(int i) {
		return searchThreshold[i];
	}

	public static double getComponentBorrowingInnovationForType(int i) {
		return componentBorrowingInnovation[i];
	}

	public static double getComponentBorrowingThresholdForType(int i) {
		return componentBorrowingThreshold[i];
	}

	public static double getComponentSwitchingInnovationForType(int i) {
		return componentSwitchingInnovation[i];
	}

	public static double getComponentSwitchingThresholdForType(int i) {
		return componentSwitchingThreshold[i];
	}

	public static double getComponentLendingInnovationForType(int i) {
		return componentLendingInnovation[i];
	}

	public static double getComponentLendingThresholdForType(int i) {
		return componentLendingThreshold[i];
	}

	/* END getters */

	public static void setInfMatFile(String filename) {
		influenceMatrixFile = "inf/" + filename + ".txt";
	}

	public static String arrayToString(String[] array) {
		String retString = "";
		for (int i = 0; i < array.length; i++) { 
			retString += array[i];
		}
		return retString;
	}
	public static String arrayToString(int[] array) {
		String retString = "";
		for (int i = 0; i < array.length; i++) {
			retString += array[i];
		}
		return retString;
	}

	public static String arrayToString(boolean[] array) {
		String retString = "";
		for (int i = 0; i < array.length; i++) {
			if (array[i]) {
				retString += "T";
			} else {
				retString += "F";
			}
		}
		return retString;
	}
	
}
