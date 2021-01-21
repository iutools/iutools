package org.iutools.linguisticdata;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.Ignore;
import org.junit.Test;

import ca.nrc.json.PrettyPrinter;

public class AffixTest {

    @Test
    public void test__makeContextualBehavioursForConsonantalContext_withCommonBehaviours() throws Exception {
        Affix suffix = new Suffix();
        suffix.makeFormsAndActions("t", "aluk",
                "aluk aluk aaluk",
                "s i(i) s",
                "i(ra) - i(ra)" );
        suffix.makeFormsAndActions("k", "aluk",
                "aluk aaluk",
                "s s",
                "i(ra) i(ra)" );
        suffix.makeFormsAndActions("q", "aluk",
                "aluk aaluk",
                "s s",
                "i(ra) i(ra)" );
        suffix._makeContextualBehavioursForConsonantalContext();
        Map<Character,List<ContextualBehaviour>> contextualBehaviours = suffix.contextualBehaviours;
        List<String> expectedBehaviours = new ArrayList<String>();
        expectedBehaviours.add("C,aluk,s,i(ra)");
        expectedBehaviours.add("C,aaluk,s,i(ra)");
        expectedBehaviours.add("t,aluk,i(i),s");
        Set<Character> keys = contextualBehaviours.keySet();
        int nbGotBehaviours = 0;
        Iterator<Character> ikeys = keys.iterator();
        while (ikeys.hasNext()) {
            List<ContextualBehaviour> listOfBehaviours = contextualBehaviours.get(ikeys.next());
            nbGotBehaviours += listOfBehaviours.size();
        }
        assertEquals("The number of behaviours returned is incorrect.",expectedBehaviours.size(),nbGotBehaviours);
        while (ikeys.hasNext()) {
            List<ContextualBehaviour> listOfBehaviours = contextualBehaviours.get(ikeys.next());
            for (int i=0; i<listOfBehaviours.size(); i++) {
                ContextualBehaviour behaviour = listOfBehaviours.get(i);
                String representation = behaviour.context.toString()+","+behaviour.basicForm+","+behaviour.action1.strng+","+behaviour.action2.strng;
                assertTrue(representation+": not in expectations",expectedBehaviours.contains(representation));
            }
        }
    }

    @Test
    public void test__makeContextualBehavioursForConsonantalContext_withNoCommonBehaviours() throws Exception {
        Affix suffix = new Suffix();
        suffix.makeFormsAndActions("t", "guq",
                "guq",
                "s",
                "" );
        suffix.makeFormsAndActions("k", "guq",
                "guq",
                "s",
                "" );
        suffix.makeFormsAndActions("q", "guq",
                "ruq",
                "s",
                "" );
        suffix._makeContextualBehavioursForConsonantalContext();
        Map<Character,List<ContextualBehaviour>> contextualBehaviours = suffix.contextualBehaviours;
        List<String> expectedBehaviours = new ArrayList<String>();
        expectedBehaviours.add("t,guq,s,");
        expectedBehaviours.add("k,guq,s,");
        expectedBehaviours.add("q,ruq,s,");
        Set<Character> keys = contextualBehaviours.keySet();
        int nbGotBehaviours = 0;
        Iterator<Character> ikeys = keys.iterator();
        while (ikeys.hasNext()) {
            List<ContextualBehaviour> listOfBehaviours = contextualBehaviours.get(ikeys.next());
            nbGotBehaviours += listOfBehaviours.size();
        }
        assertEquals("The number of behaviours returned is incorrect.",expectedBehaviours.size(),nbGotBehaviours);
        while (ikeys.hasNext()) {
            List<ContextualBehaviour> listOfBehaviours = contextualBehaviours.get(ikeys.next());
            for (int i=0; i<listOfBehaviours.size(); i++) {
                ContextualBehaviour behaviour = listOfBehaviours.get(i);
                String representation = behaviour.context.toString()+","+behaviour.basicForm+","+behaviour.action1.strng+","+behaviour.action2.strng;
                assertTrue(representation+": not in expectations",expectedBehaviours.contains(representation));
            }
        }
    }

    @Test
    public void test__makeContextualBehavioursForConsonantalContext_withAllCommonBehaviours() throws Exception {
        Affix suffix = new Suffix();
        suffix.makeFormsAndActions("t", "&&aq",
                "&&aq",
                "s",
                "" );
        suffix.makeFormsAndActions("k", "&&aq",
                "&&aq",
                "s",
                "" );
        suffix.makeFormsAndActions("q", "&&aq",
                "&&aq",
                "s",
                "" );
        suffix._makeContextualBehavioursForConsonantalContext();
        Map<Character,List<ContextualBehaviour>> contextualBehaviours = suffix.contextualBehaviours;
        List<String> expectedBehaviours = new ArrayList<String>();
        expectedBehaviours.add("C,&&aq,s,");
        Set<Character> keys = contextualBehaviours.keySet();
        int nbGotBehaviours = 0;
        Iterator<Character> ikeys = keys.iterator();
        while (ikeys.hasNext()) {
            List<ContextualBehaviour> listOfBehaviours = contextualBehaviours.get(ikeys.next());
            nbGotBehaviours += listOfBehaviours.size();
        }
        assertEquals("The number of behaviours returned is incorrect.",expectedBehaviours.size(),nbGotBehaviours);
        while (ikeys.hasNext()) {
            List<ContextualBehaviour> listOfBehaviours = contextualBehaviours.get(ikeys.next());
            for (int i=0; i<listOfBehaviours.size(); i++) {
                ContextualBehaviour behaviour = listOfBehaviours.get(i);
                String representation = behaviour.context.toString()+","+behaviour.basicForm+","+behaviour.action1.strng+","+behaviour.action2.strng;
                assertTrue(representation+": not in expectations",expectedBehaviours.contains(representation));
            }
        }
    }

//	@Test
//	public void test_getFormsInContext__Case_ijaq() throws Exception {
//		String affixId = "ijaq/1nv";
//		Affix affix = LinguisticData.getInstance().getAffixWithId(affixId);
//		
//		char context = 'V';
//		String form = affix.vform[0];
//		Action action1 = affix.vaction1[0];
//		Action action2 = affix.vaction2[0];
//		HashSet<SurfaceFormInContext> surfaceFormsInContext = affix.getFormsInContext(context, form, action1, action2, affixId);
//		assertEquals("",2,surfaceFormsInContext.size());
//		assertTrue("",surfaceFormsInContext.contains(new SurfaceFormInContext("ijaq","V","V",affixId)));
//		assertTrue("",surfaceFormsInContext.contains(new SurfaceFormInContext("ngijaq","VV","V",affixId)));
//		
//		context = 't';
//		form = affix.tform[0];
//		action1 = affix.taction1[0];
//		action2 = affix.taction2[0];
//		surfaceFormsInContext = affix.getFormsInContext(context, form, action1, action2, affixId);
//		assertEquals("",1,surfaceFormsInContext.size());
//		assertTrue("",surfaceFormsInContext.contains(new SurfaceFormInContext("aijaq","C","t",affixId)));
//		
//		context = 'k';
//		form = affix.kform[0];
//		action1 = affix.kaction1[0];
//		action2 = affix.kaction2[0];
//		surfaceFormsInContext = affix.getFormsInContext(context, form, action1, action2, affixId);
//		assertEquals("",2,surfaceFormsInContext.size());
//		assertTrue("",surfaceFormsInContext.contains(new SurfaceFormInContext("ijaq","V","k",affixId)));
//		assertTrue("",surfaceFormsInContext.contains(new SurfaceFormInContext("ngijaq","VV","k",affixId)));
//		
//		context = 'q';
//		form = affix.qform[0];
//		action1 = affix.qaction1[0];
//		action2 = affix.qaction2[0];
//		surfaceFormsInContext = affix.getFormsInContext(context, form, action1, action2, affixId);
//		assertEquals("",2,surfaceFormsInContext.size());
//		assertTrue("",surfaceFormsInContext.contains(new SurfaceFormInContext("ijaq","V","q",affixId)));
//		assertTrue("",surfaceFormsInContext.contains(new SurfaceFormInContext("ngijaq","VV","q",affixId)));
//	}
//	
//	@Test
//	public void test_getSurfaceFormsInContext__Case_ijaq() throws Exception {
//		String affixId = "ijaq/1nv";
//		Affix affix = LinguisticData.getInstance().getAffixWithId(affixId);
//		
//		char context = 'V';
//		HashSet<SurfaceFormInContext> surfaceFormsInContext = affix.getSurfaceFormsInContext(context, affixId);
//		assertEquals("",2,surfaceFormsInContext.size());
//		assertTrue("",surfaceFormsInContext.contains(new SurfaceFormInContext("ijaq","V","V",affixId)));
//		assertTrue("",surfaceFormsInContext.contains(new SurfaceFormInContext("ngijaq","VV","V",affixId)));
//		
//		context = 't';
//		surfaceFormsInContext = affix.getSurfaceFormsInContext(context, affixId);
//		assertEquals("",1,surfaceFormsInContext.size());
//		assertTrue("",surfaceFormsInContext.contains(new SurfaceFormInContext("aijaq","C","t",affixId)));
//		
//		context = 'k';
//		surfaceFormsInContext = affix.getSurfaceFormsInContext(context, affixId);
//		assertEquals("",2,surfaceFormsInContext.size());
//		assertTrue("",surfaceFormsInContext.contains(new SurfaceFormInContext("ijaq","V","k",affixId)));
//		assertTrue("",surfaceFormsInContext.contains(new SurfaceFormInContext("ngijaq","VV","k",affixId)));
//		
//		context = 'q';
//		surfaceFormsInContext = affix.getSurfaceFormsInContext(context, affixId);
//		assertEquals("",2,surfaceFormsInContext.size());
//		assertTrue("",surfaceFormsInContext.contains(new SurfaceFormInContext("ijaq","V","q",affixId)));
//		assertTrue("",surfaceFormsInContext.contains(new SurfaceFormInContext("ngijaq","VV","q",affixId)));
//	}

}
