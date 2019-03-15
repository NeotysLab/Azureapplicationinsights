package com.neotys.azure.common;

import com.google.common.base.Optional;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by anouvel on 20/12/2017.
 */
public class AzureContext {
	private final String apiKey;
	private final Optional<String> azureHostname;
	private final String aZureApplicationID;
	private  Map<String, String> headers;
	private static final String AZURE_HOST="api.applicationinsights.io";

	public AzureContext(final String apiKey,
						final Optional<String> azureHostname,
						final String aZureApplicationID
						) {
		this.apiKey = apiKey;
		this.aZureApplicationID = aZureApplicationID;

		headers=new HashMap<>();
		if(azureHostname.isPresent()) {
			this.azureHostname = azureHostname;
			headers.put("Host", azureHostname.get());

		}else {
			headers.put("Host", AZURE_HOST);
			this.azureHostname = Optional.of(AZURE_HOST);

		}

		headers.put("Connection<","keep-alive");
		headers.put("x-api-key",apiKey);

	}

	public String getApiKey() {
		return apiKey;
	}

	public Optional<String> getAzureHostname() {
		return azureHostname;
	}

	public String getaZureApplicationID() {
		return aZureApplicationID;
	}


	public Map<String, String> getHeaders() {
		return headers;
	}
}
