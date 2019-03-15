package com.neotys.azure.common;

/**
 * Created by anouvel on 14/12/2017.
 */
public class AzureException extends Exception {
	//Constructor that accepts a message
	public AzureException(final String message) {
		super(message);
	}
}
