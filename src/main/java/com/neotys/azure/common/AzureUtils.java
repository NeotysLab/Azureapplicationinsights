package com.neotys.azure.common;

import com.google.common.base.Optional;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Proxy;
import com.neotys.extensions.action.engine.ProxyType;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

import static com.neotys.azure.common.HTTPGenerator.HTTP_GET_METHOD;
import static com.neotys.azure.common.HttpResponseUtils.getJsonResponse;

/**
 * Created by Hrexed on 02/03/2019.
 */
public class AzureUtils {
    private static final String AZURE_URL = "/v1/apps/";
    private static final String AZURE_METRICS = "/metrics/";
    private static final String AZURE_METRIC_METADATA="/metrics/metadata";
    private static final String AZURE_PROTOCOL = "https://";
    private static final ImageIcon AZURE_ICON;
    static {
        final URL iconURL = com.neotys.azure.common.AzureUtils.class.getResource("ai.png");
        if (iconURL != null) {
            AZURE_ICON = new ImageIcon(iconURL);
        } else {
            AZURE_ICON = null;
        }
    }

    private AzureUtils() {
    }

    public static ImageIcon getAzureIcon() {
        return AZURE_ICON;
    }





    public static List<String> getApplicationMetadata(final Context context, final AzureContext azureContext, final Optional<String> proxyName, final boolean traceMode)
            throws Exception {
        final String azureUrl = getDynatraceApiUrl(azureContext.getAzureHostname(), azureContext.getaZureApplicationID()) + AZURE_METRIC_METADATA;
        final Map<String, String> parameters = new HashMap<>();


        final Optional<Proxy> proxy = getProxy(context, proxyName, azureUrl);
        final HTTPGenerator http = new HTTPGenerator(HTTP_GET_METHOD, azureUrl, azureContext.getHeaders(), parameters, proxy);
        final List<String> applicationMetricsMetedataList = new ArrayList<>();
        try {
            if(traceMode){
                context.getLogger().info("Azure , get application metrics medata:\n" + http.getRequest());
            }
            final HttpResponse httpResponse = http.execute();

            if (HttpResponseUtils.isSuccessHttpCode(httpResponse.getStatusLine().getStatusCode())) {
                final JSONObject jsonObject = getJsonResponse(httpResponse);
                if (jsonObject != null) {
                    extractMetricsNameFromResponse(applicationMetricsMetedataList, jsonObject);
                }
            } else {
                final String stringResponse = HttpResponseUtils.getStringResponse(httpResponse);
                throw new AzureException(httpResponse.getStatusLine().getReasonPhrase() + " - "+ azureUrl + " - " + stringResponse);
            }
        } finally {
            http.closeHttpClient();
        }

        if (traceMode) {
            context.getLogger().info("Found metrics: " + applicationMetricsMetedataList);
        }

        return applicationMetricsMetedataList;
    }

    private static long getUTCDate(String dateString)
    {
        long date=Date.parse(dateString);
        return date;
    }



    public static List<com.neotys.azure.monitoring.timeseries.AzureMetric> getMetricData(final String metricId, final Context context, final AzureContext azureContextContext, final Optional<String> proxyName, final boolean traceMode)
            throws Exception {
        JSONObject jsonMetricSegment;

        final String url = com.neotys.azure.common.AzureUtils.getDynatraceApiUrl(azureContextContext.getAzureHostname(), azureContextContext.getaZureApplicationID()) + AZURE_METRICS + "/" + metricId;
        final Map<String, String> parameters = new HashMap<>();

        parameters.put("timespan","PT1M");
        parameters.put("interval","PT1M");

        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);


        final Optional<Proxy> proxy = getProxy(context, proxyName, url);
        final HTTPGenerator  http = new HTTPGenerator(HTTP_GET_METHOD,  url,  azureContextContext.getHeaders(), parameters, proxy);

        final List<com.neotys.azure.monitoring.timeseries.AzureMetric> metrics = new ArrayList<>();
        try {
            if(traceMode){
                context.getLogger().info("Azure metrics, get Metrics:\n" + http.getRequest() );
            }
            final HttpResponse httpResponse = http.execute();

            final int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (HttpResponseUtils.isSuccessHttpCode(statusCode)) {
                jsonMetricSegment = HttpResponseUtils.getJsonResponse(httpResponse);
                if (jsonMetricSegment == null || !jsonMetricSegment.has("value")) {
                    context.getLogger().debug("No value found.");
                    return Collections.emptyList();
                }
                jsonMetricSegment = jsonMetricSegment.getJSONObject("value");
                if (jsonMetricSegment.has("segments")) {
                    final JSONArray jsonSegments = jsonMetricSegment.getJSONArray("segments");
                    for(int i=0;i<jsonSegments.length();i++)
                    {
                        JSONObject metricdata=jsonSegments.getJSONObject(i);
                        String date=metricdata.getString("start");
                        long startTS=getUTCDate(date);
                        Iterator iterator=metricdata.keys();
                        iterator.next();
                        String metricName= (String) iterator.next();
                        JSONObject metricvalue=metricdata.getJSONObject(metricName);
                        for (Iterator it = metricvalue.keys(); it.hasNext(); ) {
                            Iterator iterate = (Iterator) it.next();
                            String aggregationtype= (String) iterate.next();
                            Double value=metricvalue.getDouble(aggregationtype);
                            final com.neotys.azure.monitoring.timeseries.AzureMetric metric = new com.neotys.azure.monitoring.timeseries.AzureMetric( value, startTS, metricName, aggregationtype);
                            metrics.add(metric);

                        }

                    }
                }
                if(metrics.isEmpty() && traceMode){
                    context.getLogger().info("No timeseries found.");
                }
            }
            else if(statusCode != HttpStatus.SC_BAD_REQUEST && statusCode != HttpStatus.SC_NOT_FOUND){
                final String stringResponse = HttpResponseUtils.getStringResponse(httpResponse);
                throw new AzureException(httpResponse.getStatusLine().getReasonPhrase() + " - "+ url  + " - " + stringResponse);
            }
        } finally {
            http.closeHttpClient();
        }
        return metrics;
    }




    private static void extractMetricsNameFromResponse(final List<String> metricId, final JSONObject jsonObject) {


        final JSONObject jsonmetricsObject = jsonObject.getJSONObject("metrics");
        for (Iterator it = jsonmetricsObject.keys(); it.hasNext(); )
        {
            String key = (String) it.next();
                metricId.add(key);

        }
    }

    private static void extractProcessGroupIdsFromResponse(final List<String> processgroupInstanceIds, final JSONObject jsonObject) {

        JSONObject fromRelationships=jsonObject.getJSONObject("fromRelationships");
        try {
            JSONArray processgroupinstances = fromRelationships.getJSONArray("runsOn");
            for (int j = 0; j < processgroupinstances.length(); j++) {
                processgroupInstanceIds.add(processgroupinstances.getString(j));
            }
        }
        catch (JSONException e)
        {
            //----print the exeption
        }


    }


    private static String extractProcessGroupInstanceIdsFromResponse(final List<String> processgroupInstanceIds, final JSONObject jsonObject) {

        JSONObject fromRelationships=jsonObject.getJSONObject("fromRelationships");
        JSONArray  processgroupinstances=fromRelationships.getJSONArray("runsOnProcessGroupInstance");
        String applicationName=jsonObject.getString("displayName");
        for(int j=0;j<processgroupinstances.length();j++)
        {
            processgroupInstanceIds.add(processgroupinstances.getString(j));
        }

        return applicationName;

    }



    private static String extractProcessGroupFromResponse(final List<String> processgroupIds, final JSONObject jsonObject) {

        JSONObject fromRelationships=jsonObject.getJSONObject("fromRelationships");
        JSONArray  processgroupinstances=fromRelationships.getJSONArray("runsOn");
        String applicationName=jsonObject.getString("displayName");
        for(int j=0;j<processgroupinstances.length();j++)
        {
            processgroupIds.add(processgroupinstances.getString(j));
        }

        return applicationName;

    }

    private static void extractSerivcesIdsFromResponse(final List<String> serviceId, final JSONArray jsonArrayResponse,String applicationName) {
        applicationName=applicationName.trim();
        for (int i = 0; i < jsonArrayResponse.length(); i++) {
            final JSONObject jsonApplication = jsonArrayResponse.getJSONObject(i);
            if (jsonApplication.has("entityId") && jsonApplication.has("displayName")) {
                if(jsonApplication.getString("displayName").equalsIgnoreCase(applicationName)) {
                    JSONObject fromRelationships=jsonApplication.getJSONObject("fromRelationships");
                    JSONArray  servicesList=fromRelationships.getJSONArray("calls");
                    for(int j=0;j<servicesList.length();j++)
                    {
                        serviceId.add(servicesList.getString(j));
                    }
                }
            }
        }
    }
    private static void extractHostIdsFromProcesGroupResponse(final List<String> hostId, final JSONObject jsonObjectResponse) {
        JSONObject fromRelationships=jsonObjectResponse.getJSONObject("fromRelationships");
        JSONArray  processOF=fromRelationships.getJSONArray("runsOn");

        for(int j=0;j<processOF.length();j++)
        {
            hostId.add(processOF.getString(j));
        }

    }
    private static void extractHostIdsFromResponse(final List<String> hostId, final JSONObject jsonObjectResponse) {
        JSONObject fromRelationships=jsonObjectResponse.getJSONObject("fromRelationships");
        JSONArray  processOF=fromRelationships.getJSONArray("isProcessOf");

        for(int j=0;j<processOF.length();j++)
        {
            hostId.add(processOF.getString(j));
        }

    }
    public static Optional<Proxy> getProxy(final Context context, final Optional<String> proxyName, final String url) throws MalformedURLException {
        if (proxyName.isPresent()) {
            return Optional.fromNullable(context.getProxyByName(proxyName.get(), new URL(url)));
        }
        return Optional.absent();
    }

    public static Optional<Proxy> getNeoLoadWebProxy(final Context context, final String url) throws MalformedURLException {
       return Optional.fromNullable(context.getProxyByType(ProxyType.NEOLOAD_WEB, new URL(url)));
    }

    public static String getDynatraceApiUrl(final Optional<String> azureHostname, final String AzureApplicationID) {
        if (azureHostname.isPresent()) {
            return AZURE_PROTOCOL + azureHostname.get() + AZURE_URL + AzureApplicationID ;
        }
        return null;
    }



}
