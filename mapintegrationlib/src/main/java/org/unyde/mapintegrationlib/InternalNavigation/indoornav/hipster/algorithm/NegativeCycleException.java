package org.unyde.mapintegrationlib.InternalNavigation.indoornav.hipster.algorithm;


public class NegativeCycleException extends RuntimeException {
    private static final String message = "Existence of a negative cycle detected";

    public NegativeCycleException() {
        super(message);
    }
}
