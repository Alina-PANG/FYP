package fyp.app.objects;

import fyp.app.util.Globals;

/*
 * A Product (e.g., java.app) is a bundle of resources
 * represented as indices of resources (within N) 
 */

public class Product {
	private boolean[] resources; 
	private int[] configuration; 
	
	// generate random product
	public Product() {
		resources = new boolean[Globals.getN()];
		int resourcesSet = 0;
		while (resourcesSet < Globals.getInitResourcesForType(0)) {
			int r = Globals.rand.nextInt(Globals.getN());
			if (!resources[r]) {
				resourcesSet++;
				resources[r] = true;
			}
		}
	}

	// generate random product of size numResources
	public Product(int numResources) { 
		resources = new boolean[Globals.getN()];
		
		int resourcesSet = 0;
		while (resourcesSet < numResources) {
			int r = Globals.rand.nextInt(Globals.getN());
			if (!resources[r]) {
				resourcesSet++;
				resources[r] = true;
			}
		}
		
	}

	public Product(int[] indices) {
		resources = new boolean[Globals.getN()];
		for (int i = 0; i < indices.length; i++) {
			resources[indices[i]] = true;
		}
	}
	
	public boolean isValidResources(int idx) {
		return resources[idx];
	}
	
	public String toString() {
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
	
	public static void main(String[] args) {
//		int[] x = {1, 3, 5, 7}; //00010000
		Product p = new Product();
		System.out.println(p.toString());
	}
}
