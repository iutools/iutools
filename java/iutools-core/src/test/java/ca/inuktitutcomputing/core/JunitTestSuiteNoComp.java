package ca.inuktitutcomputing.core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)

@Suite.SuiteClasses(
{
   CompiledCorpus_IUMorphemeTest.class,
   CompiledCorpusTest.class,
   CorpusReader_DirectoryTest.class,
   QueryExpanderEvaluatorTest.class,
   QueryExpanderTest.class,
   SpellCheckerTest.class
}
)


public class JunitTestSuiteNoComp {   

} 