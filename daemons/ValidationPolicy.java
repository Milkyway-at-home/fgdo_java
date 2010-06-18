package fgdo_java.daemons;

import fgdo_java.database.Result;
import fgdo_java.database.Workunit;

import java.util.LinkedList;

public interface ValidationPolicy {

	boolean checkPair(Result canonicalResult, Result unvalidatedResult);

	boolean checkSet(Workunit workunit, LinkedList<Result> unvalidatedResults);

}
