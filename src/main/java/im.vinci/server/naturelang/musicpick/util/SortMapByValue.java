package im.vinci.server.naturelang.musicpick.util;

import java.util.*;
import java.util.Map.Entry;

public class SortMapByValue {

	
	public static List<Entry<String,Double>> sort(Map<String, Double> map){
		List<Entry<String,Double>> list = new ArrayList<Entry<String,Double>>(map.entrySet());

		Collections.sort(list,new Comparator<Entry<String,Double>>() {
			//升序排序
			public int compare(Entry<String, Double> o1,
					Entry<String, Double> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}

		});

		return list;
	}
}
