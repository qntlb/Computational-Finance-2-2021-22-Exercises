package com.andreamazzon.handout9.draw;

import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeMap;

public class Testing {
	public static void main(String[] args) {
		HashMap<String, Double> map = new HashMap<String, Double>();
		ValueComparator bvc = new ValueComparator(map);
		TreeMap<String, Double> sorted_map = new TreeMap<String, Double>(bvc);

		map.put("A", 44.5);
		map.put("B", 99.4);
		map.put("C", 67.4);
		map.put("D", 67.3);

		System.out.println("unsorted map: " + map);
		sorted_map.putAll(map);
		System.out.println("results: " + sorted_map);
	}
}

class ValueComparator implements Comparator<String> {
	HashMap<String, Double> base;

	public ValueComparator(HashMap<String, Double> base) {
		this.base = base;
	}

	// Note: this comparator imposes orderings that are inconsistent with
	// equals.
	@Override
	public int compare(String a, String b) {
		if (base.get(a) >= base.get(b)) {
			return -1;
		} else {
			return 1;
		} // returning 0 would merge keys
	}
}
