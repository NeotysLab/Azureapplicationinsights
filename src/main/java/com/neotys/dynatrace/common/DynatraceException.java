package com.neotys.dynatrace.common;

/**
 * Created by anouvel on 14/12/2017.
 */
public class DynatraceException extends Exception {
	//Parameterless Constructor
	public DynatraceException() {
	}

	//Constructor that accepts a message
	public DynatraceException(final String message) {
		super(message);
	}
}
