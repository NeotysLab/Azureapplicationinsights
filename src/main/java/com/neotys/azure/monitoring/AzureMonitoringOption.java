package com.neotys.azure.monitoring;

import com.neotys.action.argument.ArgumentValidator;
import com.neotys.action.argument.Option;
import com.neotys.extensions.action.ActionParameter;

import static com.neotys.action.argument.DefaultArgumentValidator.ALWAYS_VALID;
import static com.neotys.action.argument.DefaultArgumentValidator.NON_EMPTY;
import static com.neotys.action.argument.Option.AppearsByDefault.False;
import static com.neotys.action.argument.Option.AppearsByDefault.True;
import static com.neotys.action.argument.Option.OptionalRequired.Optional;
import static com.neotys.action.argument.Option.OptionalRequired.Required;
import static com.neotys.extensions.action.ActionParameter.Type.TEXT;

/**
 *
 */
enum AzureMonitoringOption implements Option {
    ApplicationId("applicationId", Required, True, TEXT,
            "Application ID",
            "Application ID Monitored by Azure INsights",
            NON_EMPTY),

    AzureApiKey("AzureApiKey", Required, True, TEXT,
            "Azure API key.",
            "Azure API key",
            NON_EMPTY),


    AzureManagedHostname("AzureManagedHostname", Optional, False, TEXT,
            "Azure Insights custom Hostname",
            "Azure Insights custom Hostname",
            ALWAYS_VALID),
    NeoLoadDataExchangeApiUrl("dataExchangeApiUrl", Optional, False, TEXT,
            "",
            "Where the DataExchange server is located. Optional, by default it is: http://${NL-ControllerIp}:7400/DataExchange/v1/Service.svc/",
            NON_EMPTY),

    NeoLoadDataExchangeApiKey("dataExchangeApiKey", Optional, False, TEXT,
            "",
            "Identification key specified in NeoLoad.",
            ALWAYS_VALID),

    NeoLoadProxy("proxyName", Optional, False, TEXT,
            "",
            "The NeoLoad proxy name to access Dynatrace.",
            ALWAYS_VALID),

    TraceMode("traceMode", Optional, False, TEXT,
            "",
            "",
            ALWAYS_VALID);


    private final String name;
    private final Option.OptionalRequired optionalRequired;
    private final Option.AppearsByDefault appearsByDefault;
    private final ActionParameter.Type type;
    private final String defaultValue;
    private final String description;
    private final ArgumentValidator argumentValidator;

    AzureMonitoringOption(final String name, final Option.OptionalRequired optionalRequired,
                          final Option.AppearsByDefault appearsByDefault,
                          final ActionParameter.Type type, final String defaultValue, final String description,
                          final ArgumentValidator argumentValidator) {
        this.name = name;
        this.optionalRequired = optionalRequired;
        this.appearsByDefault = appearsByDefault;
        this.type = type;
        this.defaultValue = defaultValue;
        this.description = description;
        this.argumentValidator = argumentValidator;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Option.OptionalRequired getOptionalRequired() {
        return optionalRequired;
    }

    @Override
    public Option.AppearsByDefault getAppearsByDefault() {
        return appearsByDefault;
    }

    @Override
    public ActionParameter.Type getType() {
        return type;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public ArgumentValidator getArgumentValidator() {
        return argumentValidator;
    }
}
