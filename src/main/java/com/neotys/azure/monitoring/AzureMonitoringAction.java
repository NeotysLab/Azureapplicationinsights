package com.neotys.azure.monitoring;

import com.google.common.base.Optional;
import com.neotys.action.argument.Arguments;
import com.neotys.action.argument.Option;
import com.neotys.extensions.action.Action;
import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.ActionEngine;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public final class AzureMonitoringAction implements Action {
	private static final String BUNDLE_NAME = "com.neotys.dynatrace.monitoring.bundle";
	private static final String DISPLAY_NAME = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault()).getString("displayName");
	private static final String DISPLAY_PATH = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault()).getString("displayPath");

	@Override
	public String getType() {
		return "AzureMonitoringAction";
	}

	@Override
	public List<ActionParameter> getDefaultActionParameters() {
		final List<ActionParameter> parameters = new ArrayList<>();

		for (final com.neotys.azure.monitoring.AzureMonitoringOption option : com.neotys.azure.monitoring.AzureMonitoringOption.values()) {
			if (Option.AppearsByDefault.True.equals(option.getAppearsByDefault())) {
				parameters.add(new ActionParameter(option.getName(), option.getDefaultValue(),
						option.getType()));
			}
		}

		return parameters;
	}

	@Override
	public Class<? extends ActionEngine> getEngineClass() {
		return com.neotys.azure.monitoring.AzureMonitoringActionEngine.class;
	}

	@Override
	public Icon getIcon() {
		return com.neotys.azure.common.AzureUtils.getAzureIcon();
	}

	@Override
	public String getDescription() {
		final StringBuilder description = new StringBuilder();
		description.append("The AzureMonitoring Action will get metrics from Azure Insights and send NeoLoad computed ones.\n")
				.append("The list is available at: https://github.com/Neotys-Labs/Azure\n\n")
				.append(Arguments.getArgumentDescriptions(AzureMonitoringOption.values()));

		return description.toString();
	}

	@Override
	public String getDisplayName() {
		return DISPLAY_NAME;
	}

	@Override
	public String getDisplayPath() {
		return DISPLAY_PATH;
	}

	@Override
	public Optional<String> getMinimumNeoLoadVersion() {
		return Optional.of("6.7");
	}

	@Override
	public Optional<String> getMaximumNeoLoadVersion() {
		return Optional.absent();
	}
}
