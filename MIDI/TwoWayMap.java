package MIDI;
import java.util.ArrayList;
import java.util.HashMap;


public class TwoWayMap {
	private HashMap<String, Integer> hash;
	private ArrayList<String> array;
	
	public TwoWayMap(String[] values){
		hash = new HashMap<String, Integer>();
		array = new ArrayList<String>();
		
		for(int arrayC = 0; arrayC < values.length; arrayC ++){
			hash.put(values[arrayC], arrayC);
			array.add(values[arrayC]);
		}
	}
	
	public int getIndex(String arg0){
		return hash.get(arg0);
	}
	
	public String getString(int arg0){
		return array.get(arg0);
	}
}
