package fyp.app.util;

import java.io.*;
import java.util.ArrayList;

public class InfluenceMatrix {
	String matrixFile;
	ArrayList<Interdependence> interdependencies = new ArrayList<Interdependence>();
	int cases;
	int[] positionIndex;
	
	public InfluenceMatrix(String filename) {
		matrixFile = filename;
		loadMatrixFile();
		setCasesPositions();
	}
	
	private void loadMatrixFile() {
		try{
			FileInputStream fstream = new FileInputStream(matrixFile);
		    DataInputStream in = new DataInputStream(fstream);
	        BufferedReader br = new BufferedReader(new InputStreamReader(in));
		    String strLine;
		    
		    int line = 0;
		    while ((strLine = br.readLine()) != null)   {
		    	if (!strLine.equals("")) { // in case there is an extra empty line at the end
			        String[] result = strLine.split(",");
			        // check policy choice size (cols)
			        if (result.length != Globals.getN()) {
			        	System.err.println("incorrect number of policy choices (too many columns " + Globals.getN() + "\t" + result.length + ")");
			        	System.exit(0);
			        } 
			        
			        // check policy choice size (rows)
			        if (line >= Globals.getN()) {
			        	System.err.println("incorrect number of policy choices (too many rows)");
			        	System.exit(0);
			        }
			        
			        //System.out.println("N: " + result.length);
			        boolean[] temp = new boolean[result.length];
			        for (int i = 0; i < result.length; i++) {
			        	if (result[i].equals("x")) {
			        		temp[i] = true;
			        	} else {
			        		temp[i] = false;
			        	}
			        	//System.out.println(result[x]);
			        }
			        //interdependencies.add(new Interdependence(line, temp));
			        interdependencies.add(new Interdependence(line, Globals.getN(), temp));
		    	}
		    	line++;
		    }
		    //Close the input stream
		    in.close();
		} catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		
	}
	
	private void setCasesPositions() {
		int cumCases = 0;
		positionIndex = new int[interdependencies.size()];
		
		for (int i = 0; i < interdependencies.size(); i++) {
			positionIndex[i] = cumCases; // 0 to start with and then add
//			System.out.println("setting positionIndex at " + i + " to " + cumCases);
			cumCases += Math.pow(2, ((Interdependence)interdependencies.get(i)).getNumDependencies() + 1);
		}
		cases = cumCases;
	}
	/**
	 * returns number of possible cases for the landscape given the influence matrix
	 */
	
	public int numCases() {
		return cases;
	}

	public int getStartPosition(int idx) {
		return positionIndex[idx];
	}
	
	public void print() {
		System.out.println("size: " + interdependencies.size());
		for (Interdependence ind : interdependencies) {
			System.out.println(ind.toString());
		}
	}

	public Interdependence getDependenceAt(int idx) {
		Interdependence intdep = (Interdependence)interdependencies.get(idx);
		// just checking -- may remove if not needed
		if (intdep.getPolicyPosition() != idx) { System.err.println("error: policy position and index do not match"); System.exit(0); }
		return intdep;	
	}
	
	// For testing purposes only
	public static void main(String args[]) {
		InfluenceMatrix im = new InfluenceMatrix("matrix.txt");
		im.print();
		System.out.println(im.numCases());
		
		String[] loc = {"1", "1", "0", "1", "0", "0", "0", "1", "0", "1", "1", "0", "0", "0", "0", "1", "1", "1", "0", "1"}; //11010000
		Location l = new Location(loc); 
		
		System.out.println(l.getLocationAt(0, im));
	}
}
