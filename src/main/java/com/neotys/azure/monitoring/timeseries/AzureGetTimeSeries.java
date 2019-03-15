package com.neotys.azure.monitoring.timeseries;

import com.google.common.base.Optional;
import com.neotys.azure.common.AzureContext;
import com.neotys.azure.common.AzureUtils;
import com.neotys.azure.common.HTTPGenerator;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Proxy;
import com.neotys.rest.dataexchange.client.DataExchangeAPIClient;
import com.neotys.rest.dataexchange.model.EntryBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.neotys.azure.common.HTTPGenerator.HTTP_GET_METHOD;

public class AzureGetTimeSeries {
    private static final String DYNATRACE_API_PROCESS_GROUP = "entity/infrastructure/process-groups";
    private static final String DYNATRACE_HOSTS = "entity/infrastructure/hosts";
    private static final String DYNATRACE_TIMESERIES = "timeseries";

    private static final String COUNT = "COUNT";
    private static final String NONE = null;

    private static final String ENTITY_ID = "entityId";
    private static final String DISPLAY_NAME = "displayName";
    private static final String AZURE = "AZURE";
    private static final String PROCESS = "Process";
    private AzureContext azureContext;

    private List<String> metricnamelist;
    private final Optional<String> proxyName;
    private final DataExchangeAPIClient dataExchangeApiClient;
    private final String azureApiKey;
    private final String applicationID;
    private final Optional<String> azureHostName;
    private HTTPGenerator httpGenerator;
    private Map<String, String> header = null;
    private boolean isRunning = true;
    private final Context context;
    private long startTS;
    private boolean traceMode;
    private List<AzureMetric> azureMetricList;
    public AzureGetTimeSeries(final Context context,
                              final String aureApiKey,
                              final String applicationId,
                              final DataExchangeAPIClient dataExchangeAPIClient,
                              final Optional<String> proxyName,
                              final Optional<String> azureHostNameManagedHostname,
                              final boolean traceMode) throws Exception {
        this.context = context;
	    this.azureApiKey = aureApiKey;
	    this.applicationID = applicationId;
	    this.dataExchangeApiClient = dataExchangeAPIClient;
	    this.proxyName = proxyName;
	    this.azureHostName = azureHostNameManagedHostname;
	    this.traceMode = traceMode;


	    this.isRunning = true;
	    this.header = new HashMap<>();
	    List<String> metricnamelist=new ArrayList<>();
	    azureContext =new AzureContext(azureApiKey, azureHostName, applicationId);
        this.metricnamelist= AzureUtils.getApplicationMetadata(context, azureContext, proxyName, traceMode);
        this.azureMetricList = new ArrayList<>();

    }

    private void getMetricData()
    {

        metricnamelist.stream().forEach(
                metric-> {
                    try {
                        azureMetricList.addAll(AzureUtils.getMetricData(metric, context, azureContext, proxyName, traceMode));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }
    public void processAzureData() throws Exception {
        if (isRunning) {
            List<com.neotys.rest.dataexchange.model.Entry> metricEntries = processtMetricData();



            if(!metricEntries.isEmpty()){
                //Send merged Entries
                dataExchangeApiClient.addEntries(metricEntries);
            }
        }
    }


    private List<com.neotys.rest.dataexchange.model.Entry> processtMetricData() throws Exception {
        List<com.neotys.rest.dataexchange.model.Entry> entries = new ArrayList<>();
            if (isRunning) {
                getMetricData();
                entries.addAll(toEntries(azureMetricList));
                }


        return entries;
    }

    private List<com.neotys.rest.dataexchange.model.Entry> toEntries(final List<com.neotys.azure.monitoring.timeseries.AzureMetric> dynatraceMetrics) {
        return dynatraceMetrics.stream()
                .map(this::toEntry)
                .collect(Collectors.toList());
    }

    private com.neotys.rest.dataexchange.model.Entry toEntry(final AzureMetric azureMetric) {
        List<String> path = new ArrayList<>();
        path.add(AZURE);
        path.add(azureMetric.getMetricName());


        return new EntryBuilder(path, azureMetric.getTime())
                .unit(null)
                .value(azureMetric.getValue())
                .build();
    }

    private Optional<Proxy> getProxy(final Optional<String> proxyName, final String url) throws MalformedURLException {
        if (proxyName.isPresent()) {
            return Optional.fromNullable(context.getProxyByName(proxyName.get(), new URL(url)));
        }
        return Optional.absent();
    }



    private void sendTokenIngetParam(final Map<String, String> param) {
        param.put("Api-Token", azureApiKey);
    }

    public void setTestToStop() {
        isRunning = false;
    }

}


