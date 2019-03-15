package com.neotys.azure.monitoring.timeseries;

public class AzureMetric {

	private double value;
	private long time;
	private String metricName;
	private String aggregationType;
	

	public double getValue() {
		return value;
	}

	public long getTime() {
		return time;
	}

	public String getMetricName() {
		return metricName;
	}



	public AzureMetric(double value, long time, String metricName, String entity) {
		super();
		this.value = value;
		this.time = time;
		this.metricName = metricName;

	}
}
