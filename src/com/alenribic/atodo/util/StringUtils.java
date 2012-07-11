package com.alenribic.atodo.util;

public final class StringUtils {
	
	private StringUtils() {}
	
	// @TODO(#alen): very bad implementation that needs rewriting 
	public static String join(String[] items, String delim) {
		boolean first = true;
		String result = "";
		for (int i = 0; i < items.length; i++) {
			if (first) {
				result += items[i];
				first = false;
			} else result += delim + items[i]; 
		}
		return result;
	}
}
