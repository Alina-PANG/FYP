package fyp.app.util;

public class Location {
	String[] location; // N bit location with either ' ', '0' or '1'; ' ' (empty space) means that this resource is not a part of the firm or consumer
	
	public Location(String[] loc) {
		location = new String[Globals.getN()];
		System.arraycopy(loc, 0, location, 0, loc.length);
	}

	public Location(char[] loc) {
		location = new String[Globals.getN()];
		for (int i = 0; i < location.length; i++) {
			location[i] = Character.toString(loc[i]);
		}
	}

	public String[] getLocation() {
		return location;
	}
	
	public String getLocationAt(int index) {
		return location[index]; 
	}

	public String getLocationAt(int index, InfluenceMatrix im) {
		String retString = "";
		Interdependence intdep = im.getDependenceAt(index);
		
		for (int i = index; i < index + Globals.getN(); i++){
			if (intdep.isDependent(i % Globals.getN())) {
				retString += location[i % Globals.getN()];
			}
		}
		// System.out.println(index + "\t" + retString);
		return retString;
	}

	public static Location getLocationFromInt(int num) {
		String loc = Integer.toBinaryString(num);
		int zeros = Globals.getN() - loc.length();
		for (int j = 0; j < zeros; j++) {
			loc = "0" + loc;
		}
		char[] locArray = loc.toCharArray();
		return new Location(locArray);
	}

	public static String getLocationStringFromInt(int num) {
		String loc = Integer.toBinaryString(num);
		int zeros = Globals.getN() - loc.length();
		for (int j = 0; j < zeros; j++) {
			loc = "0" + loc;
		}
		return loc;
	}

	public boolean isValidResource(int index) {
		if (location[index].equals(" ")) { 
			return false;
		} else {
			return true;
		}
	}

	public String toString() {
		String retString = "";
		for (int i = 0; i < location.length; i++) {
			if (location[i].equals(" ")) {
				retString += " ";
			} else {
				retString += location[i];
			}
		}
		return retString;
	}

}
