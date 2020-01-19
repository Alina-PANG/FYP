package fyp.app.objects;

import java.util.*;
import java.util.logging.Logger;

import fyp.app.Simulation;
import fyp.app.util.Bag;
import fyp.app.util.Globals;
import fyp.app.util.Landscape;


public class Firm implements Comparable<Firm> {
	private static final Logger logger = Logger.getLogger( Firm.class.getName());
	private int firmID;

	// firm parameters
	private int firmType;
	private int initResources;
	private double innovation;
	private int resourcesIncrement;
	private int searchScope;
	private double searchThreshold;
	private String search;
	private String resourceDecision;
	private double resourceThreshold;

	private double componentBorrowingInnovation;
	private double componentBorrowingThreshold;
	private double componentSwitchingInnovation;
	private double componentSwitchingThreshold;
	private double componentLendingInnovation;
	private double componentLendingThreshold;
	private Map<Integer, Integer> borrowedComponents; // componentIndex, firmID

	private int countExp;
	private int countAdd;
	private int countDrop;
	private int countBorrow;
	private int countSwitch;

	//private ArrayList<Product> products; // can we have overlapping resources for different products?  I think NO 
	private boolean[] resources;
	private String[] resourceConfig; // array of "0", "1" or " "
	private String[] previousResourceConfig;
	// private double fitness;
	private int rank;
	// NEED? dictionary of resource connections

	// random firm with of Globals.numResources resources
	public Firm() {
		setResources(Globals.getInitResourcesForType(0));
		setResourceConfig();
		// fitness = Simulation.landscape.getFitness(resourceConfig);
	}

	public Firm(int aType, int id, int anInitResources, double anInnovation,
				int anResourcesIncrement, int aSearchScope, double aSearchThreshold,
				String aSearch, String aResourceDecision, double aResourceThreshold,
				double acomponentBorrowingInnovation, double acomponentBorrowingThreshold,
				double acomponentSwitchingInnovation, double acomponentSwitchingThreshold,
				double acomponentLendingInnovation, double acomponentLendingThreshold) {
		firmType = aType;
		firmID = id;
		initResources = anInitResources;
		innovation = anInnovation;
		resourcesIncrement = anResourcesIncrement;
		searchScope = aSearchScope;
		searchThreshold = aSearchThreshold;
		search = aSearch;
		resourceDecision = aResourceDecision;
		resourceThreshold = aResourceThreshold;
		componentBorrowingInnovation = acomponentBorrowingInnovation;
		componentBorrowingThreshold = acomponentBorrowingThreshold;
		componentSwitchingInnovation = acomponentSwitchingInnovation;
		componentSwitchingThreshold = acomponentSwitchingThreshold;
		componentLendingInnovation = acomponentLendingInnovation;
		componentLendingThreshold = acomponentLendingThreshold;
		borrowedComponents = new HashMap<>();

		setResources(initResources);
		setResourceConfig();
		previousResourceConfig = new String[Globals.getN()];
	}

	public Firm(int id) {
		firmID = id;
		setResources(Globals.getInitResourcesForType(0));
		setResourceConfig();
		// fitness = Simulation.landscape.getFitness(resourceConfig);
	}

	// new firm with n resources
	public Firm(int id, int numResources) {
		firmID = id;
		setResources(numResources);
		setResourceConfig();
		// Simulation.landscape.getFitness(resourceConfig);
	}

	// new firm with specific resources
	public Firm(int id, int[] indices) {
		firmID = id;
		resources = new boolean[Globals.getN()];
		for (int i = 0; i < indices.length; i++) {
			resources[indices[i]] = true;
		}
		setResourceConfig();
		// Simulation.landscape.getFitness(resourceConfig);
	}

	// initialize firm resources
	private void setResources(int size) {
		resources = new boolean[Globals.getN()];
		int resourcesSet = 0;
		while (resourcesSet < size) {
			int r = Globals.rand.nextInt(Globals.getN());
			if (!resources[r]) {
				resourcesSet++;
				resources[r] = true;
			}
		}
	}

	public String printCounts(){
		return countExp + "\t" + countAdd + "\t" + countDrop + "\t" + countBorrow + "\t" + countSwitch;
	}

	public int getCountExp() {
		return countExp;
	}

	public int getCountAdd() {
		return countAdd;
	}

	public int getCountDrop() {
		return countDrop;
	}

	public int getCountBorrow() {
		return countBorrow;
	}

	public int getCountSwitch() {
		return countSwitch;
	}

	// NO NEED; FITNESS IS ALWAYS CALCULATED ON THE FLY
	// public void initFitness() {
	// 	fitness = Simulation.landscape.getFitness(resourceConfig);
	// 	System.out.println("init: " + firmID + "\t" + this.getResourceConfigString() + "\t" + fitness);
	// }

	// randomly set initial configuration of resources
	private void setResourceConfig() {
		resourceConfig = new String[Globals.getN()];
		for (int i = 0; i < resourceConfig.length; i++) {
			if (resources[i]) {
				resourceConfig[i] = String.valueOf(Globals.rand.nextInt(2));
			} else {
				resourceConfig[i] = " ";
			}
		}

	}


	public boolean isValidResources(int idx) {
		return resources[idx];
	}

	public void keepRecord(){
		System.arraycopy(resourceConfig, 0, previousResourceConfig, 0, resourceConfig.length);
	}

	public void makeDecision() { // with innovation
		System.out.println("*************** Firm "+this.firmID+" *******************");
		System.out.println("Current: "+printResConfig(resourceConfig));
		searchExperiential();
		addOrDrop();
	}

	private boolean absoluteOrNormalizedDecision(String[] newConfig, int numResources, double threshold, int type) {
		double currentFitness = Simulation.landscape.getFitness(resourceConfig);
		double newUtility = Simulation.landscape.getFitness(newConfig);

		System.out.println("Resource Decision: "+resourceDecision+" New Utility = "+newUtility+", curFit = "+currentFitness+", threshold = "+threshold+" Num Res = "+numResources);
		boolean absoluteDecision = resourceDecision.equals("abs") && (newUtility - currentFitness > threshold);
		boolean relativeDecision = resourceDecision.equals("rel") && (newUtility - currentFitness) / numResources > threshold;

		if (absoluteDecision || relativeDecision){
			System.out.println("Decision: Execute the new config: "+printResConfig(newConfig)+" (old config: "+printResConfig(resourceConfig)+")");
			System.arraycopy(newConfig, 0, resourceConfig, 0, newConfig.length);
			switch(type){
				case 0:
					countBorrow ++;
					break;
				case 1:
					countSwitch ++;
					break;
				case 2: break; // lend
				case 3:
					countExp ++;
					break;
				default:
					break;
			}
			syncResources(); // resets bool resources[]
			return true;
		} else {
			System.out.println("Decision: Stay the same config.");
			return false;
		}
	}

	public void componentOperations(int type){
		double innovation, threshold;
		switch (type){
			case 0:
				innovation = componentBorrowingInnovation;
				threshold = componentBorrowingThreshold;
				break;
			case 1:
				innovation = componentSwitchingInnovation;
				threshold = componentSwitchingThreshold;
				break;
			default:
				innovation = componentLendingInnovation;
				threshold = componentLendingThreshold;
		}

		if(innovation >= Globals.rand.nextDouble()) {
			String[] newConfig = new String[Globals.getN()];
			switch (type){
				case 0:
					System.out.println("*************** Firm "+this.firmID+" *******************"); // output
					considerBorrowing();
					break;
				case 1:
					System.out.println("*************** Firm "+this.firmID+" *******************"); // output
					considerSwitching();
					break;
				default:
					considerLending();
					return;
			}
		} else{
			System.out.println("Firm with ID "+this.firmID+" innovation too low!");
		}
	}

	private Set<Integer> borrowedRes(){
		Set<Integer> set = new HashSet<>();
		for(int k: borrowedComponents.keySet()){
			List<Integer> list = Globals.getComponentByIndex(k);
			set.addAll(list);
		}
		return set;
	}

	private void addOrDrop(){
		if (innovation >= Globals.rand.nextDouble()) {
			String[] addResourceConfig = new String[Globals.getN()];
			System.arraycopy(considerAddResource(), 0, addResourceConfig, 0, addResourceConfig.length);
			String[] dropResourceConfig = new String[Globals.getN()];
			System.arraycopy(considerDropResource(), 0, dropResourceConfig, 0, dropResourceConfig.length);

			double currentFitness = Simulation.landscape.getFitness(resourceConfig);
			double addResourceUtility = Simulation.landscape.getFitness(addResourceConfig);
			double dropResourceUtility = Simulation.landscape.getFitness(dropResourceConfig);

			int numCurrentResources = 0;
			for (int i = 0; i < resourceConfig.length; i++) {
				if (!resourceConfig[i].equals(" ")) {
					numCurrentResources++;
				}
			}

			int numResourcesToAdd = 0;
			for (int i = 0; i < addResourceConfig.length; i++) {
				if (!addResourceConfig[i].equals(" ")) {
					numResourcesToAdd++;
				}
			}
			numResourcesToAdd = numResourcesToAdd - numCurrentResources;

			int numResourcesToDrop = 0;
			for (int i = 0; i < dropResourceConfig.length; i++) {
				if (!dropResourceConfig[i].equals(" ")) {
					numResourcesToDrop++;
				}
			}
			numResourcesToDrop = numCurrentResources - numResourcesToDrop;

			boolean changed = false;
			Set<Integer> dropped = new HashSet<>();
			for(int i = 0; i < dropResourceConfig.length; i ++){
				if(!dropResourceConfig[i].equals(resourceConfig[i])) dropped.add(i);
			}

			System.out.println("Resource Decision: "+resourceDecision+" Add Utility = "+addResourceUtility+", Drop Utility = "+dropResourceUtility+", curFit = "+currentFitness+", threshold = "+resourceThreshold+", Num Add Res = "+numResourcesToAdd+", Num Drop Res = "+numResourcesToDrop);
			// ABSOLUTE VS. NORMALIZED DECISION MAKING
			if (resourceDecision.equals("abs")) {
				// first consider if threshold has been met	by either add or drop
				if ((addResourceUtility - currentFitness - resourceThreshold > 0) || (dropResourceUtility - currentFitness - resourceThreshold > 0)) {
					if (addResourceUtility > dropResourceUtility) {
						System.arraycopy(addResourceConfig, 0, resourceConfig, 0, addResourceConfig.length);
						countAdd ++;
						System.out.println("Decision: Execute the add config: "+printResConfig(addResourceConfig)+" (old config: "+printResConfig(resourceConfig)+")");
					} else {
						System.arraycopy(dropResourceConfig, 0, resourceConfig, 0, dropResourceConfig.length);
						countDrop ++;
						changed = true;
						System.out.println("Decision: Execute the drop config: "+printResConfig(dropResourceConfig)+" (old config: "+printResConfig(resourceConfig)+")");
					}
				} else { // now consider if dropResourceUtility is performance enhancing
					if (dropResourceUtility - currentFitness > 0) {
						System.arraycopy(dropResourceConfig, 0, resourceConfig, 0, dropResourceConfig.length);
						countDrop ++;
						changed = true;
						System.out.println("Decision: Execute the drop config: "+printResConfig(dropResourceConfig)+" (old config: "+printResConfig(resourceConfig)+")");
					}
					else{System.out.println("Decision: Stay the same config.");}
				}

			} else { // getResourceDecision() == "relative" **** ACTUALLY WE'RE NOT RUNNING THIS FOR NOW.  SO THIS PART HASN'T BEEN FULLY TESTED
				// TODO ????
				if ((addResourceUtility/numCurrentResources) - resourceThreshold > (dropResourceUtility/(numCurrentResources - 1)) + resourceThreshold) {
					// add is better
					// currentFitness is out of numResources whereas addResourceUtility is out of (numResources + 1)
					// if ((addResourceUtility/(numCurrentResources + 1)) > (currentFitness/numCurrentResources) + Globals.getResourceThreshold()) {
					if ((addResourceUtility/(numCurrentResources + numResourcesToAdd)) > (currentFitness/numCurrentResources) + resourceThreshold) {
						System.arraycopy(addResourceConfig, 0, resourceConfig, 0, addResourceConfig.length);
						countAdd ++;
					} // else  do nothing
				} else {
					// drop is better
					// currentFitness is out of numResources whereas addResourceUtility is out of (numResources + 1)
					if ((dropResourceUtility/(numCurrentResources - 1)) > (currentFitness/numCurrentResources) - resourceThreshold) {
						System.arraycopy(dropResourceConfig, 0, resourceConfig, 0, dropResourceConfig.length);
						countDrop ++;
						changed = true;
					} // else  do nothing
				}
			}

			// update firms that are borrowing components from this firm
//			if(changed){
//				for(Integer indexToChange: dropped){
//					int componentIndex = Globals.getComponentIndexByResIndex(indexToChange);
//					if(componentIndex != -1){
//						// check if I am lending it
//						Set<Firm> firms = Globals.getSharingFirmsForComponent(componentIndex);
//						if(firms != null && firms.contains(this)){
//							System.out.println("changed -- Firm "+this.firmID+" is lending the component.");
//							// I am lending it, then I need to check what are the firms that are borrowing from me
//							for (Firm firm : Globals.getFirms()) {
//								Map<Integer, Integer> borrowed = firm.getBorrowedComponents();
//								if (borrowed.keySet().contains(componentIndex)) {
//									if (borrowed.get((Integer) componentIndex) == this.firmID) {
//										// edited: the firm is borrowing this component from me
//										// the firm would abadon the entire component
//										List<Integer> component = Globals.getComponentByIndex(componentIndex);
//										for (int i : component) {
//											firm.changeConfig(i, " ");
//										}
//										firm.removeBorrowedComponentAndFirm(componentIndex);
//										System.out.println("changed -- Borrowing Firm " + firm.getFirmID() + " dropped component " + componentIndex + ": " + component+" fitness="+firm.getFitness());
//									}
//								}
//							}
//							Globals.removeSharingFirm(componentIndex, this);
//							System.out.print("changed -- lending firm for component: "+componentIndex);
//							for (Firm f: Globals.getSharingFirmsForComponent(componentIndex)){
//								System.out.print(f.firmID+", ");
//							}
//							System.out.println();
//							break;
//						}
//					}
//				}
//			}
		}
		syncResources(); // resets bool resources[]
	}

	public void removeBorrowedComponentAndFirm(int cIndex){
		this.borrowedComponents.remove(cIndex);
	}


	private void addResource() {
		//double addResourceUtility = 0.0d;
		double currentFitness = Simulation.landscape.getFitness(resourceConfig);
		// System.out.println(firmID + "\t" + getResourceConfigString() + "\t" + currentFitness + "\tmaking decision");

		// get current number of resources available to the firm
		int numCurrentResources = 0;
		for (int i = 0; i < resources.length; i++) {
			if (resources[i]) {
				numCurrentResources++;
			}
		}

		// add resource config: create copy of current resourceConfig
		String[] addResourceConfig = new String[Globals.getN()];
		System.arraycopy(resourceConfig, 0, addResourceConfig, 0, resourceConfig.length);

		// need to pick 
		int numResourcesToAdd = Globals.rand.nextInt(Math.min(Globals.getN() - numCurrentResources + 1, resourcesIncrement)) + 1;

		// create copy of resources so that we can update 
		boolean[] resourcesCopy = new boolean[Globals.getN()];
		System.arraycopy(resources, 0, resourcesCopy, 0, resources.length);

		for (int j = 0; j < numResourcesToAdd; j++) {
			try {
				int resourceToAdd = Globals.rand.nextInt(Globals.getN() - numCurrentResources - j);
				int count = 0;
				for (int i = 0; i < resourcesCopy.length; i++) {
					if (!resourcesCopy[i]) {
						if (count == resourceToAdd) {
							// ADD RESOURCE WITH RANDOM SETTING 
							// !! change to setting with higher utility?
							addResourceConfig[i] = Integer.toString(Globals.rand.nextInt(2));
							resourcesCopy[i] = true;
							break;
						}
						count++;
					}
				}
			} catch (java.lang.IllegalArgumentException ex) {
				// do nothing
			}
		}

		double addResourceUtility = Simulation.landscape.getFitness(addResourceConfig);

		// ABSOLUTE VS. NORMALIZED DECISION MAKING
		if (resourceDecision.equals("absolute")) {
			if (addResourceUtility > currentFitness + resourceThreshold) {
				System.arraycopy(addResourceConfig, 0, resourceConfig, 0, addResourceConfig.length);
			} // else do nothing
		} else {
			// currentFitness is out of numResources whereas addResourceUtility is out of (numResources + 1)
			// if ((addResourceUtility/(numCurrentResources + 1)) > (currentFitness/numCurrentResources) + Globals.getResourceThreshold()) {
			if ((addResourceUtility/(numCurrentResources + numResourcesToAdd)) > (currentFitness/numCurrentResources) + resourceThreshold) {
				System.arraycopy(addResourceConfig, 0, resourceConfig, 0, addResourceConfig.length);
			} // else  do nothing
		}
		syncResources(); // resets bool resources[]
	}

	private void printRes(){
		System.out.println("Firm: " + this.firmID);
		for(String str: resourceConfig) {
			if(str.equals(" ")) System.out.print(".");
			else System.out.print(str);
		}
		System.out.println();
	}

	public String[] getPreviousResourceConfig() {
		return previousResourceConfig;
	}

	private void considerBorrowing() {
		String[] newConfig = new String[Globals.getN()];
		System.arraycopy(resourceConfig, 0, newConfig, 0, resourceConfig.length);
		System.out.println("**** Current Firm: "+this.firmID+" with config "+printResConfig(this.resourceConfig));

		// decide the component indexes that I can borrow
		List<List<Integer>> components = Globals.getComponents();
		List<Integer> componentCanBeBorrowed = new ArrayList<Integer>();
		for(int i = 0; i < components.size(); i ++) {
			boolean have = false;
			for(int j = 0; j < components.get(i).size(); j ++) {
				if(!resourceConfig[components.get(i).get(j)].equals(" ")) {
					have = true; break;
				}
			}
			if(!have) componentCanBeBorrowed.add(i);
		}

		int cIndexToBorrow = -1, lendingFirmID = -1;
		if(componentCanBeBorrowed.size() >= 1) {
			Random rnd = new Random();
			int tempIndexToBorrow = rnd.nextInt(componentCanBeBorrowed.size());
			cIndexToBorrow = componentCanBeBorrowed.get(tempIndexToBorrow);
			Set<Firm> firms = Globals.getSharingFirmsForComponent(cIndexToBorrow);
			if(firms == null || firms.size() == 0) {
				System.out.println("Firm with ID "+this.firmID + " decides not to borrow");
			}else{
				int fIndexToBorrow = rnd.nextInt(firms.size());
				List<Firm> list = new ArrayList<>(firms);
				Firm f = list.get(fIndexToBorrow);

				System.out.println("> Component Index: " + cIndexToBorrow); // output
				System.out.println("> Borrowing from firm: "+f.getFirmID()+" with config "+printResConfig(f.resourceConfig)); // output
				for(int index: components.get(cIndexToBorrow)){
					newConfig[index] = f.getPreviousResourceConfig()[index];
				}
				lendingFirmID = f.getFirmID();
			}
		}
		else System.out.println("Firm "+this.firmID+" has no available firms to borrow.");

		System.out.print("> New config: "); // output
		System.out.println(printResConfig(newConfig)); // output

		int numResources = 0;
		for (int i = 0; i < newConfig.length; i++) {
			if (!resourceConfig[i].equals(" ")) {
				numResources++;
			}
		}

		boolean changed = absoluteOrNormalizedDecision(newConfig, numResources, componentBorrowingThreshold, 0);
		if(changed && cIndexToBorrow != -1 && lendingFirmID != -1) {
			borrowedComponents.put(cIndexToBorrow, lendingFirmID);
		}
	}

	private void considerSwitching() {
		String[] newConfig = new String[Globals.getN()];
		System.arraycopy(resourceConfig, 0, newConfig, 0, resourceConfig.length);

		// decide the component indexes that I can switch to (if I have all resources in this component)
		List<List<Integer>> components = Globals.getComponents();
		List<Integer> componentCanBeSwitched = new ArrayList<Integer>();
		for(int i = 0; i < components.size(); i ++) {
			boolean have = true;
			for(int j = 0; j < components.get(i).size(); j ++) {
				if(!resources[components.get(i).get(j)]) {
					have = false; break;
				}
			}
			boolean flag = Globals.getSharingFirmsForComponent(i) != null && Globals.getSharingFirmsForComponent(i).size() > 1;
			if(have && flag) componentCanBeSwitched.add(i);
		}
		int cIndexToBorrow = -1, lendingFirmID = -1;
		if(componentCanBeSwitched.size() >= 1) {
			Random rnd = new Random();
			int tempIndexToBorrow = rnd.nextInt(componentCanBeSwitched.size());
			cIndexToBorrow = componentCanBeSwitched.get(tempIndexToBorrow);
			Set<Firm> firms = Globals.getSharingFirmsForComponent(cIndexToBorrow);
			if(firms == null || firms.size() < 2) {
				System.out.println("Firm with ID "+this.firmID + " decides not to switch");
			} else{
				int fIndexToBorrow = rnd.nextInt(firms.size());
				List<Firm> list = new ArrayList<>(firms);
				Firm f = list.get(fIndexToBorrow);
				// to prevent the firm from swtiching to its own configuration
				while(f.getFirmID() == this.firmID) {
					fIndexToBorrow = rnd.nextInt(firms.size());
					f = list.get(fIndexToBorrow);
				}
				lendingFirmID = f.getFirmID();
				System.out.println("> Component Index: " + cIndexToBorrow); // output
				System.out.println("> Switching to firm: "+f.getFirmID()+" with config "+printResConfig(f.resourceConfig)); // output
				System.out.println("> Current Firm: "+this.firmID+" with config "+printResConfig(this.resourceConfig)); // output
				for(int index: components.get(cIndexToBorrow)){
					newConfig[index] = f.getPreviousResourceConfig()[index];
				}
			}
		}
		else System.out.println("Firm "+this.firmID+" has no available switches.");
		System.out.print("> New config: "); // output
		System.out.println(printResConfig(newConfig)); // output

		int numResources = 0;
		for (int i = 0; i < newConfig.length; i++) {
			if (!resourceConfig[i].equals(" ")) {
				numResources++;
			}
		}

		boolean changed = absoluteOrNormalizedDecision(newConfig, numResources, componentSwitchingThreshold, 1);
		if(changed && cIndexToBorrow != -1 && lendingFirmID != -1) {
			borrowedComponents.put(cIndexToBorrow, lendingFirmID);
		}
	}

	private String printResConfig(String[] config){
		StringBuilder sb = new StringBuilder();
		for(String s: config){
			if(s.equals(" ")) sb.append(".");
			else sb.append(s);
		}
		return sb.toString();
	}

	public void syncComponent(){
		for(Map.Entry<Integer, Integer> entry: borrowedComponents.entrySet()){
			// check if still sharing
			Set<Firm> f = Globals.getSharingFirmsForComponent(entry.getKey());
			List<Integer> resIndices = Globals.getComponentByIndex(entry.getKey());
			Firm lendingFirm = Globals.getFirmById(entry.getValue());
			if(!f.contains(lendingFirm)) {
				System.out.println("Firm "+this.firmID+" ceases to lend from firm "+lendingFirm.getFirmID()+" component "+entry.getKey());
				for(int index: resIndices) this.resourceConfig[index] = " ";
				System.out.println("New Config: "+printResConfig(this.resourceConfig)+" new fitness: "+this.getFitness());
				continue;
			}

			// check if changed or not
			for(int index: resIndices) this.resourceConfig[index] = lendingFirm.getResourceConfig()[index];
			System.out.println("Verify: Firm "+this.firmID+" lending from firm "+lendingFirm.getFirmID()+" component "+entry.getKey());
			System.out.println("New Config: "+printResConfig(this.resourceConfig)+" new fitness: "+this.getFitness());
		}
		syncResources();
	}

	public String[] getResourceConfig() {
		return resourceConfig;
	}

	private void considerLending() {
		// decide the component indexes that I can switch to (if I have all resources in this component)
		List<List<Integer>> components = Globals.getComponents();

		for(int i = 0; i < components.size(); i ++) {
			boolean have = true;
			for(int j = 0; j < components.get(i).size(); j ++) {
				if(!resources[components.get(i).get(j)]) {
					have = false;
					Globals.removeSharingFirm(i, this);
					break;
				}
			}
			if(have && !borrowedComponents.containsKey(i)) {
				System.out.println("Firm with ID "+this.getFirmID()+" decides to lend component "+i+" "+Globals.getComponentByIndex(i)+" with config "+printResConfig(resourceConfig));
				Globals.addSharingFirms(i, this);
			} else if(have && borrowedComponents.containsKey(i)){
				Globals.removeSharingFirm(i ,this);
			}
		}
		if(borrowedComponents.size() != 0) System.out.println("Firm with ID "+this.getFirmID()+" has borrowed components: "+borrowedComponents);//output
	}

	private String[] considerAddResource() {
		// get current number of resources available to the firm
		int numCurrentResources = 0;
		Bag bag = new Bag();
		for (int i = 0; i < resourceConfig.length; i++) {
			if (!resourceConfig[i].equals(" ")) {
				numCurrentResources++;
			} else bag.add(i);
		}

		// add resource config: create copy of current resourceConfig
		String[] addResourceConfig = new String[Globals.getN()];
		System.arraycopy(resourceConfig, 0, addResourceConfig, 0, resourceConfig.length);

		// need to pick 
		int numResourcesToAdd = Globals.rand.nextInt(Math.min(Globals.getN() - numCurrentResources + 1, resourcesIncrement)) + 1;
		System.out.println("Add：Number of resources to add: "+numResourcesToAdd);

		for (int j = 0; j < numResourcesToAdd && !bag.isEmpty(); j++) {
			int indexToAdd = (Integer)bag.randomPop();
			addResourceConfig[indexToAdd] = Integer.toString(Globals.rand.nextInt(2));
		}

		System.out.println("Add config: "+printResConfig(addResourceConfig));
		return addResourceConfig;
	}

	private void dropResource() {
		// FOR NOW WE'LL ONLY CONSIDER DROPPING 1 RESOURCE AT A TIME AND ONLY WHEN numCurrentResources > 2
		//double addResourceUtility = 0.0d;
		double currentFitness = Simulation.landscape.getFitness(resourceConfig);
		// System.out.println(firmID + "\t" + getResourceConfigString() + "\t" + currentFitness + "\tmaking decision");

		// get current number of resources available to the firm
		int numCurrentResources = 0;
		for (int i = 0; i < resources.length; i++) {
			if (resources[i]) {
				numCurrentResources++;
			}
		}
		// drop resource config: copy of current resourceConfig
		String[] dropResourceConfig = new String[Globals.getN()];
		System.arraycopy(resourceConfig, 0, dropResourceConfig, 0, resourceConfig.length);

		// if a firm has only 1 (last) resource, it cannot drop it.  
		if (numCurrentResources > 1) {

			// [TODO] CURRENTLY ONLY DROPPING 1 RESOURCE AT A TIME -- CHANGE TO UP TO ResourceThreshold?
			int resourceToDrop = Globals.rand.nextInt(numCurrentResources);
			int count = 0;
			for (int i = 0; i < resources.length; i++) {
				if (resources[i]) {
					if (count == resourceToDrop) {
						// ADD RESOURCE WITH RANDOM SETTING 
						// !! change to setting with higher utility?
						dropResourceConfig[i] = " ";
						break;
					}
					count++;
				}
			}

			double dropResourceUtility = Simulation.landscape.getFitness(dropResourceConfig);

			// ABSOLUTE VS. NORMALIZED DECISION MAKING
			if (resourceDecision.equals("absolute")) {
				if (dropResourceUtility < currentFitness - resourceThreshold) {
					System.arraycopy(dropResourceConfig, 0, resourceConfig, 0, dropResourceConfig.length);
				} // else do nothing
			} else {
				// currentFitness is out of numResources whereas addResourceUtility is out of (numResources + 1)
				if ((dropResourceUtility/(numCurrentResources - 1)) > (currentFitness/numCurrentResources) - resourceThreshold) {
					System.arraycopy(dropResourceConfig, 0, resourceConfig, 0, dropResourceConfig.length);
				} // else  do nothing
			}

			syncResources(); // resets bool resources[] 
		}

	}

	// now it's only droping one resource
	private String[] considerDropResource() {
		// get current number of resources available to the firm
		int numCurrentResources = 0;
		Bag bag = new Bag();
		Set<Integer> borrowedRes = this.borrowedRes();
		System.out.print("Drop - Borrowed resources: ");
		System.out.println(borrowedRes);
		for (int i = 0; i < resourceConfig.length; i++) {
			if (!resourceConfig[i].equals(" ") ) {
				numCurrentResources++;
				if(!borrowedRes.contains(i)) bag.add(i);
			}
		}
		// TODO: limited number of resources to drop
		int numResourcesToDrop = 1; // Globals.rand.nextInt(numCurrentResources > resourcesIncrement ? resourcesIncrement: numCurrentResources - 1) + 1;
		System.out.println("Drop：Number of resources to drop: "+numResourcesToDrop);

		// drop resource config: copy of current resourceConfig
		String[] dropResourceConfig = new String[Globals.getN()];
		System.arraycopy(resourceConfig, 0, dropResourceConfig, 0, resourceConfig.length);

		// if a firm has only 1 (last) resource, it cannot drop it.  
		if (numCurrentResources > 1 && !bag.isEmpty()) {
			int indexToDrop = (Integer) bag.randomPop();
			System.out.println("Dropping Resource: "+indexToDrop);
			dropResourceConfig[indexToDrop] = " ";
		}
		System.out.println("Drop config: "+printResConfig(dropResourceConfig));
		return dropResourceConfig;
	}

	private void searchExperiential() { // search one-off changes in existing resources
		//double addResourceUtility = 0.0d;
		double currentFitness = Simulation.landscape.getFitness(resourceConfig);
		// System.out.println(firmID + "\t" + getResourceConfigString() + "\t" + currentFitness + "\tmaking decision");

		Set<Integer> borrowedRes = this.borrowedRes();
		System.out.print("Search - Borrowed resources: ");
		System.out.println(borrowedRes);
		// get current number of resources available to the firm
		int numResources = 0;
		Bag bag = new Bag();
		for (int i = 0; i < resourceConfig.length; i++) {
			if (!resourceConfig[i].equals(" ")) {
				numResources++;
				if(!borrowedRes.contains(i)) bag.add(i);
			}
		}

		// search config
		String[] searchConfig = new String[Globals.getN()];
		System.arraycopy(resourceConfig, 0, searchConfig, 0, resourceConfig.length);

		// determine how many resrouces to change.
		int numResourcesToChange = Math.min(Globals.rand.nextInt(searchScope) + 1, numResources);
		System.out.println("Search：Number of resources to change: "+numResourcesToChange);

		Map<Integer, Boolean> map = new HashMap<>();

		for(int i = 0; i < numResourcesToChange && !bag.isEmpty(); i ++){
			int indexToChange = (Integer) bag.randomPop();
			boolean changeToOne = true;
			if(searchConfig[indexToChange].equals("1")){
				searchConfig[indexToChange] = "0";
				changeToOne = false;
			}
			else searchConfig[indexToChange] = "1";
			map.put(indexToChange, changeToOne);
		}

		System.out.println("Search config: "+printResConfig(searchConfig));
		boolean changed = absoluteOrNormalizedDecision(searchConfig, numResourcesToChange, searchThreshold, 3);

		// update firms that are borrowing components from this firm
//		if(changed){
//			for(Integer indexToChange: map.keySet()){
//				boolean changeToOne = map.get(indexToChange);
//				int componentIndex = Globals.getComponentIndexByResIndex(indexToChange);
//				if(componentIndex != -1){
//					// check if I am lending it
//					Set<Firm> firms = Globals.getSharingFirmsForComponent(componentIndex);
//					if(firms != null && firms.contains(this))
//					{
//						// I am lending it, then I need to check what are the firms that are borrowing from me
//						for(Firm firm: Globals.getFirms()){
//							Map<Integer, Integer> borrowed = firm.getBorrowedComponents();
//							if(borrowed.keySet().contains(componentIndex)){
//								if(borrowed.get((Integer)componentIndex) == this.firmID){
//									// the firm is borrowing this component from me
//									if(changeToOne) firm.changeConfig(indexToChange, "1");
//									else firm.changeConfig(indexToChange, "0");
//									System.out.println("changed -- Borrowing Firm "+firm.getFirmID()+" changed index "+indexToChange+" fitness="+firm.getFitness());
//								}
//							}
//						}
//						break;
//
//					}
//				}
//			}
//		}
	}

	public void changeConfig(int index, String value){
		resourceConfig[index] = value;
		syncResources();
	}

	public Map<Integer, Integer> getBorrowedComponents() {
		return borrowedComponents;
	}

	public void setBorrowedComponents(Map<Integer, Integer> borrowedComponents) {
		this.borrowedComponents = borrowedComponents;
	}

/*
		- implement searchScope so that long jumps are possible.  
		- For now, we'll implement searchScope = 1 or 2 but if we need >3 then we'll likely need a more general approach with recursion
		- Jan 17, 2019: We'll not implement searchExhaustive --> unrealistic
	 */
	/*
	private void searchExhaustive() { // search one-off changes in existing resources
		//double addResourceUtility = 0.0d;
		double currentFitness = Simulation.landscape.getFitness(resourceConfig);
		// System.out.println(firmID + "\t" + getResourceConfigString() + "\t" + currentFitness + "\tmaking decision");
		
		// get current number of resources available to the firm
		int numResources = 0;
		for (int i = 0; i < resources.length; i++) {
			if (resources[i]) { numResources++; }
		}
		//System.out.println("ResourceConfig: \n" + Globals.arrayToString(resourceConfig));

		// search config
		String[] bestSearchConfig = new String[Globals.getN()];
		double bestAlternative = 0.0d;
		
		if (Globals.getSearchScope() == 1) {
			for (int i = 0; i < resources.length; i++) {
				String[] searchConfig = new String[Globals.getN()];
				System.arraycopy(resourceConfig, 0, searchConfig, 0, resourceConfig.length);

				if (resources[i]) {
					if (resourceConfig[i].equals("0")) { 
						searchConfig[i] = "1";
					} else {
						searchConfig[i] = "0";
					}
					if (Simulation.landscape.getFitness(searchConfig) > bestAlternative) {
						System.arraycopy(searchConfig, 0, bestSearchConfig, 0, searchConfig.length);
						bestAlternative = Simulation.landscape.getFitness(searchConfig);
					}
				}
			}
		} else if (Globals.getSearchScope() == 2) {
			for (int i = 0; i > resources.length; i++) {
				String[] searchConfig = new String[Globals.getN()];
				System.arraycopy(resourceConfig, 0, searchConfig, 0, resourceConfig.length);
				if (resources[i]) {
					if (resourceConfig[i].equals("0")) { 
						searchConfig[i] = "1";
					} else {
						searchConfig[i] = "0";
					}
					for (int j = 0; j > resources.length; j++) {
						if (resources[j]) {
							if (resourceConfig[j].equals("0")) { 
								searchConfig[j] = "1";
							} else {
								searchConfig[j] = "0";
							}
						}
						if (Simulation.landscape.getFitness(searchConfig) > bestAlternative) {
							System.arraycopy(searchConfig, 0, bestSearchConfig, 0, searchConfig.length);
							bestAlternative = Simulation.landscape.getFitness(searchConfig);
						}
					}
				} 
			}

		} else {
			// this shouldn't happen 
	    	System.err.println("INCORRECT PARAMETER ERROR: searchScope must either be 1 or 2 (for now)");
	    	System.exit(0);
		}

	
		//System.out.println("SearchConfig: \n" + Globals.arrayToString(searchConfig));
		double bestSearchUtility = Simulation.landscape.getFitness(bestSearchConfig);

		if (bestSearchUtility > currentFitness) {
				System.arraycopy(bestSearchConfig, 0, resourceConfig, 0, bestSearchConfig.length);
		}  else {
			// do nothing
		}
	}
	*/

	public String getResourceConfigAt(int idx) {
		return resourceConfig[idx];
	}

	private String getResourcesString() {
		String retString = "";
		for (int i = 0; i < resources.length; i++) {
			if (resources[i]) {
				retString += "1";
			} else {
				retString += "0";
			}
		}
		return retString;
	}

	private String getResourceConfigString() {
		String retString = "";
		for (int i = 0; i < resourceConfig.length; i++) {
			if (resourceConfig[i].equals(" ")) {
				retString += "-";
			} else {
				retString += resourceConfig[i];
			}
		}
		return retString;

	}

	public int getFirmID() {
		return firmID;
	}

	public double getFitness() {
		return Simulation.landscape.getFitness(resourceConfig);
	}

	public double getFitness(Landscape l) {
		return l.getFitness(resourceConfig);
	}

	public void setRank(int aRank) {
		rank = aRank;
	}

	public int getRank() {
		return rank;
	}

	public int compareTo(Firm compareFirm) {
		double compareFitness = ((Firm)compareFirm).getFitness();
		double thisFitness = this.getFitness();
		if(thisFitness < compareFitness) {
			return 1;
		} else if(compareFitness < thisFitness) {
			return -1;
		} else {
			return 0;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
		if (!(o instanceof Firm)) {
			return false;
		}

		if(((Firm) o).getFirmID() == this.firmID) return true;
		else return false;
	}

	private void syncResources() {
		for (int i = 0; i < resourceConfig.length; i++) {
			if (resourceConfig[i].equals(" ")) {
				resources[i] = false;
			} else {
				resources[i] = true;
			}
		}
	}

	public String toString() {
		String retString = "firmType (" + firmType + ")\t";
		retString += "firmID (" + firmID + ")\t";
		retString += "initResources (" + initResources + ")\t";
		retString += "innovation (" + innovation + ")\t";
		retString += "resourcesIncrement (" + resourcesIncrement + ")\t";
		retString += "searchScope (" + searchScope + ")\t";
		retString += "searchThreshold (" + searchThreshold + ")\t";
		retString += "resourceDecision (" + resourceDecision + ")\t";
		retString += "resourceThreshold (" + resourceThreshold + ")\t" + getResourceConfigString();
		return retString;
	}

	public String toStringWithFitness(Landscape l) {
		//System.out.println(getResourceConfigString());
		String retString = firmID + "\t" + getResourceConfigString() + "\t" + l.getFitness(resourceConfig);
		return retString;
	}

	public String toStringFull(Landscape l) {
		//System.out.println(getResourceConfigString());
		String retString = firmID + "\t" + rank + "\t" + printResConfig(resourceConfig) + "\t" + l.getFitness(resourceConfig);
		return retString;
	}
}
