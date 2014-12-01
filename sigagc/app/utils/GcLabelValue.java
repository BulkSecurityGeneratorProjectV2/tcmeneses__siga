package utils;

import java.text.Normalizer;

public class GcLabelValue {
	private String label;
	private String value;
	private String text;

	public GcLabelValue(String label, String value) {
		super();
		this.label = label;
		this.value = value;
		this.text = removeAcento(label).toLowerCase();
	}

	public String getLabel() {
		return label;
	}

	public String getValue() {
		return value;
	}


	public String getItemLabel() {
		String[] a = label.split(": ");
		if (a.length == 2)
			return a[1];
		return label;
	}

	public boolean fts(String[] palavras) {
		for (String palavra : palavras) {
			if (!text.contains(palavra))
				return false;
		}
		return true;
	}
	
	public static String removeAcento(String acentuado) {
		if (acentuado == null)
			return null;
		return Normalizer.normalize(acentuado, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
//		String semacentos = new String(acentuado.replaceAll("[����]", "A").replaceAll("[���]", "E")
//				.replaceAll("[���]", "I").replaceAll("[����]", "O")
//				.replaceAll("[����]", "U").replaceAll("[�]", "C")
//				.replaceAll("[����]", "a").replaceAll("[���]", "e")
//				.replaceAll("[���]", "i").replaceAll("[����]", "o")
//				.replaceAll("[����]", "u").replaceAll("[�]", "c"));
//		return semacentos;
	}
}
