package com.neotys.azure.monitoring;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.neotys.action.result.ResultFactory;
import com.neotys.azure.common.Constants;
import com.neotys.azure.monitoring.timeseries.AzureGetTimeSeries;

import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.ActionEngine;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Logger;
import com.neotys.extensions.action.engine.SampleResult;
import com.neotys.rest.dataexchange.client.DataExchangeAPIClient;
import com.neotys.rest.dataexchange.client.DataExchangeAPIClientFactory;
import com.neotys.rest.dataexchange.model.ContextBuilder;
import com.neotys.rest.error.NeotysAPIException;
import org.apache.olingo.odata2.api.exception.ODataException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.emptyToNull;
import static com.neotys.action.argument.Arguments.getArgumentLogString;
import static com.neotys.action.argument.Arguments.parseArguments;

public final class AzureMonitoringActionEngine implements ActionEngine {

    private static final String STATUS_CODE_INVALID_PARAMETER = "NL-AZURE MONITORING_ACTION-01";
    private static final String STATUS_CODE_TECHNICAL_ERROR = "NL-AZURE_MONITORING_ACTION-02";
    private static final String STATUS_CODE_BAD_CONTEXT = "NL-AZURE_MONITORING_ACTION-03";

    private AzureGetTimeSeries azureIntegration;

    @Override
    public SampleResult execute(final Context context, final List<ActionParameter> parameters) {
        final SampleResult sampleResult = new SampleResult();
        final StringBuilder requestBuilder = new StringBuilder();
        final StringBuilder responseBuilder = new StringBuilder();


        final Map<String, Optional<String>> parsedArgs;
        try {
            parsedArgs = parseArguments(parameters, AzureMonitoringOption.values());
        } catch (final IllegalArgumentException iae) {
            return ResultFactory.newErrorResult(context, STATUS_CODE_INVALID_PARAMETER, "Could not parse arguments: ", iae);
        }

        if (context.getWebPlatformRunningTestUrl() == null) {
            return ResultFactory.newErrorResult(context, STATUS_CODE_BAD_CONTEXT, "Bad context: ", new com.neotys.azure.common.AzureException("No NeoLoad Web test is running"));
        }

        final Logger logger = context.getLogger();
        if (logger.isDebugEnabled()) {
            logger.debug("Executing " + this.getClass().getName() + " with parameters: "
                    + getArgumentLogString(parsedArgs, AzureMonitoringOption.values()));
        }

        final String applicationID = parsedArgs.get(AzureMonitoringOption.ApplicationId.getName()).get();
        final String azureApiKey = parsedArgs.get(AzureMonitoringOption.AzureApiKey.getName()).get();
        final Optional<String> AzureHostname = parsedArgs.get(AzureMonitoringOption.AzureManagedHostname.getName());
        final Optional<String> dataExchangeApiKey = parsedArgs.get(AzureMonitoringOption.NeoLoadDataExchangeApiKey.getName());
        final Optional<String> proxyName = parsedArgs.get(AzureMonitoringOption.NeoLoadProxy.getName());
        final Optional<String> optionalTraceMode = parsedArgs.get(AzureMonitoringOption.TraceMode.getName());
        final String dataExchangeApiUrl = Optional.fromNullable(emptyToNull(parsedArgs.get(AzureMonitoringOption.NeoLoadDataExchangeApiUrl.getName()).orNull()))
                .or(() -> getDefaultDataExchangeApiUrl(context));

        if (context.getLogger().isDebugEnabled()) {
            context.getLogger().debug("Data Exchange API URL used: " + dataExchangeApiUrl);
        }

        try {

            boolean traceMode = optionalTraceMode.isPresent() && Boolean.valueOf(optionalTraceMode.get());

	       DataExchangeAPIClient dataExchangeAPIClient = getDataExchangeAPIClient(context, requestBuilder, dataExchangeApiUrl, dataExchangeApiKey);

            azureIntegration = new AzureGetTimeSeries(context, azureApiKey, applicationID,  dataExchangeAPIClient, proxyName, AzureHostname,  traceMode);
            azureIntegration.processAzureData();

            //first call send event to dynatrace
            sampleResult.sampleEnd();
        } catch (Exception e) {
            return ResultFactory.newErrorResult(context, STATUS_CODE_TECHNICAL_ERROR, "Error encountered :", e);
        }

        sampleResult.setRequestContent(requestBuilder.toString());
        sampleResult.setResponseContent(responseBuilder.toString());
        return sampleResult;
    }

    private String getDefaultDataExchangeApiUrl(final Context context) {
        return "http://" + context.getControllerIp() + ":7400/DataExchange/v1/Service.svc/";
    }

    private boolean validateAgggregationType(Optional<String> aggregatetype)
    {
        ImmutableList<String> dynatraceAggregagtionTypes=ImmutableList.of("MIN","MAX","SUM","AVG","MEDIAN","COUNT","PERCENTILE");
        if(aggregatetype.isPresent())
        {
            if(dynatraceAggregagtionTypes.contains(aggregatetype.get()))
                return true;
        }
        return false;
    }

    private DataExchangeAPIClient getDataExchangeAPIClient(final Context context, final StringBuilder requestBuilder, final String dataExchangeApiUrl, final Optional<String> dataExchangeApiKey) throws GeneralSecurityException, IOException, ODataException, URISyntaxException, NeotysAPIException {
        DataExchangeAPIClient dataExchangeAPIClient = (DataExchangeAPIClient) context.getCurrentVirtualUser().get(Constants.NL_DATA_EXCHANGE_API_CLIENT);
        if (dataExchangeAPIClient == null) {
                final ContextBuilder contextBuilder = new ContextBuilder();
                contextBuilder.hardware(Constants.NEOLOAD_CONTEXT_HARDWARE).location(Constants.NEOLOAD_CONTEXT_LOCATION).software(
                        Constants.NEOLOAD_CONTEXT_SOFTWARE).script("DynatraceMonitoring" + System.currentTimeMillis());
                dataExchangeAPIClient = DataExchangeAPIClientFactory.newClient(dataExchangeApiUrl,
                        contextBuilder.build(),
                        dataExchangeApiKey.orNull());
                context.getCurrentVirtualUser().put(Constants.NL_DATA_EXCHANGE_API_CLIENT, dataExchangeAPIClient);
                requestBuilder.append("DataExchangeAPIClient created.\n");
        } else {
            requestBuilder.append("DataExchangeAPIClient retrieved from User Path Context.\n");
        }
        return dataExchangeAPIClient;
    }

    @Override
    public void stopExecute() {
        if (azureIntegration != null)
            azureIntegration.setTestToStop();
    }

}
