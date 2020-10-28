package ca.inuktitutcomputing.morph;

import ca.nrc.testing.AssertString;
import ca.nrc.testing.Asserter;

import static ca.inuktitutcomputing.morph.Decomposition.MorphFormat;

public class AssertDecomposition extends Asserter {

    public AssertDecomposition(Object _gotObject, String mess) {
        super(_gotObject, mess);
    }

    public AssertDecomposition assertFormattedDecompStrEquals(
        String expFormatted, String origDecompStr, MorphFormat format)
        throws Exception {

        String gotFormatedDecomp =
            Decomposition.formatDecompStr(origDecompStr, format);
        AssertString.assertStringEquals(
    baseMessage+"\nWrong formatted decomp for input '"+origDecompStr+"' with format="+format,
            expFormatted, gotFormatedDecomp

        );

        return this;
    }
}
