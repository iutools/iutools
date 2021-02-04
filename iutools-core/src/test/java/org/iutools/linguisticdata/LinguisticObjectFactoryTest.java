package org.iutools.linguisticdata;

import ca.nrc.json.PrettyPrinter;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class LinguisticObjectFactoryTest {

	@Test
	public void test__makeBaseCompositionRoot() throws LinguisticDataException {
		// arviat,,1,n,place,p,arviaq,communauté de Eskimo Point,settlement of Eskimo Point,,arviq,.
		HashMap<String,String> linguisticDataMap = new HashMap<String,String>();
		linguisticDataMap.put("morpheme","arviat");
		linguisticDataMap.put("variant","");
		linguisticDataMap.put("nb","1");
		linguisticDataMap.put("type","n");
		linguisticDataMap.put("nature","place");
		linguisticDataMap.put("number","p");
		linguisticDataMap.put("compositionRoot","arviaq");
		linguisticDataMap.put("freMean","communauté de Eskimo Point");
		linguisticDataMap.put("engMean","settlement of Eskimo Point");
		linguisticDataMap.put("combination","");
		linguisticDataMap.put("root","arviq");
		Base base = new Base(linguisticDataMap);
		Base baseComp = LinguisticObjectFactory._makeBaseCompositionRoot(base, linguisticDataMap, "arviaq");
		System.out.println("ARVIAT/1N:\n"+PrettyPrinter.print(base));
		System.out.println("-------------\nARVIAQ/1N:\n"+PrettyPrinter.print(baseComp));
		//System.out.print(PrettyPrinter.print(baseComp));
		Assert.assertEquals("'morpheme' of original base is incorrect.","arviat",base.morpheme);
		Assert.assertEquals("'morpheme' of composition root is incorrect.","arviaq",baseComp.morpheme);
		Assert.assertEquals("'variant' is incorrect.",null,baseComp.variant);
		Assert.assertEquals("'compositionRoot' is incorrect.",null,baseComp.compositionRoot);
		Assert.assertEquals("'originalMorpheme' is incorrect.","arviat/1n",baseComp.originalMorpheme);
		Assert.assertEquals("'subtype' is incorrect.","nc",baseComp.subtype);
		Assert.assertEquals("'id' is incorrect.","arviat/1n",baseComp.id);
	}

}
