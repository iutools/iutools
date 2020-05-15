package ca.inuktitutcomputing.morph;

import java.util.Collection;

import org.apache.log4j.Logger;

public class MorphDebug {
	
	public static void traceDecomps(Logger tLogger, 
			Collection<Decomposition> decomps, String mess) {
		traceDecomps(tLogger, decomps, mess, null);
	}

		public static void traceDecomps(Logger tLogger, 
			Collection<Decomposition> decomps, String mess,
			Boolean onlyPrintSize) {
		
			if (onlyPrintSize == null) {
				onlyPrintSize = false;
			}
		
		if (tLogger.isTraceEnabled()) {
			if (onlyPrintSize) {
				mess += "\nsize is "+decomps.size();
			} else {
				mess += "\nDecompositions are:\n"+Decomposition.toString(decomps);
			}
			tLogger.trace(mess);
		}
	}

	public static void traceDecomps(Logger tLogger, 
			Decomposition[] decomps, String mess) {
		if (tLogger.isTraceEnabled()) {
			mess += Decomposition.toString(decomps);
			tLogger.trace(mess);
		}
	}

}
