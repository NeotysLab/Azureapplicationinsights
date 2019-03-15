package com.neotys.azure.common;

public final class Constants {

	/*** Dynatrace ***/
	public static final String AZURE_INSIGHTS = "AZURE_INSIGHTS";

	/*** NeoLoad context (Data Exchange API) ***/
	public static final String NEOLOAD_CONTEXT_HARDWARE = AZURE_INSIGHTS;
	public static final String NEOLOAD_CONTEXT_LOCATION = AZURE_INSIGHTS;
	public static final String NEOLOAD_CONTEXT_SOFTWARE = AZURE_INSIGHTS;
	
	/*** NeoLoad Current Virtual user context (Keep object in cache cross iterations) ***/
	public static final String NL_DATA_EXCHANGE_API_CLIENT = "NLDataExchangeAPIClient";
	public static final String AZURE_LAST_EXECUTION_TIME = "AzureLastExecutionTime";

    public static final String TRACE_MODE = "traceMode";
}
