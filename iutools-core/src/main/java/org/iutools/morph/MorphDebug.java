package org.iutools.morph;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.morph.r2l.DecompositionState;

public class MorphDebug {
	
	public static void traceDecomps(Logger tLogger,
											  Collection<DecompositionState> decomps, String mess) {
		traceDecomps(tLogger, decomps, mess, null);
	}

		public static void traceDecomps(Logger tLogger,
												  Collection<DecompositionState> decomps, String mess,
												  Boolean onlyPrintSize) {
		
			if (onlyPrintSize == null) {
				onlyPrintSize = false;
			}
		
		if (tLogger.isTraceEnabled()) {
			if (onlyPrintSize) {
				mess += "\nsize is "+decomps.size();
			} else {
				mess += "\nDecompositions are:\n"+ DecompositionState.toString(decomps);
			}
			tLogger.trace(mess);
		}
	}

	public static void traceDecomps(Logger tLogger,
											  DecompositionState[] decomps, String mess) {
		if (tLogger.isTraceEnabled()) {
			mess += DecompositionState.toString(decomps);
			tLogger.trace(mess);
		}
	}

}
