package ca.pirurvik.iutools.utilbin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class compareHansards {

	public static void main(String[] args) throws IOException {
		Set<String> roots2002 = new HashSet<String>();
		Set<String> roots2018 = new HashSet<String>();
		String h2002 = "/Users/benoitfarley/Inuktitut/Pirurvik/notes et résultats/verbroots-hansard-1999-2002.csv";
		String h2018 = "/Users/benoitfarley/Inuktitut/Pirurvik/notes et résultats/verbroots-hansard-1999-2018.csv";
		File f2002 = new File(h2002);
		File f2018 = new File(h2018);
		String line;
		BufferedReader br2002 = new BufferedReader(new FileReader(f2002));
		while ( (line=br2002.readLine())!=null ) {
			String lineParts[] = line.split(",");
			String rootId = lineParts[0];
			roots2002.add(rootId);
		}
		br2002.close();
		BufferedReader br2018 = new BufferedReader(new FileReader(f2018));
		while ( (line=br2018.readLine())!=null ) {
			String lineParts[] = line.split(",");
			String rootId = lineParts[0];
			roots2018.add(rootId);
		}
		br2018.close();
		
		List<String> roots2002NotIn2018 = new ArrayList<String>();
		Iterator<String> it2002 = roots2002.iterator();
		while (it2002.hasNext()) {
			String root2002 = it2002.next();
			if ( !roots2018.contains(root2002) )
				roots2002NotIn2018.add(root2002);
		}
		Collections.sort(roots2002NotIn2018);
		
		List<String> roots2018NotIn2002 = new ArrayList<String>();
		Iterator<String> it2018 = roots2018.iterator();
		while (it2018.hasNext()) {
			String root2018 = it2018.next();
			if ( !roots2002.contains(root2018) )
				roots2018NotIn2002.add(root2018);
		}
		Collections.sort(roots2018NotIn2002);
		
		System.out.println("Roots of Hansard 2002 not in Hansard 2018: ("+roots2002NotIn2018.size()+")");
		for (int i=0; i<roots2002NotIn2018.size(); i++)
			System.out.println(roots2002NotIn2018.get(i));

		System.out.println("\nRoots of Hansard 2018 not in Hansard 2002: ("+roots2018NotIn2002.size()+")");
		for (int i=0; i<roots2018NotIn2002.size(); i++)
			System.out.println(roots2018NotIn2002.get(i));

	}

}
