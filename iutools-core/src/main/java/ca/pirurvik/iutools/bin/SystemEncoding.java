package ca.pirurvik.iutools.bin;

public class SystemEncoding {
	

	public static void main(String args[]) {
		System.out.println("System encoding: "+System.getProperty("file.encoding"));
		
		if (args.length>0) {
			String word = args[0];
			for (int i=0; i<word.length(); i++) {
				char c = word.charAt(i);
				int code = word.codePointAt(i);
				System.out.println("c["+i+"] = "+c+" --- "+code);
			}
		}
	}

}