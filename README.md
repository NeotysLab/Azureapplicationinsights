<p align="center"><img src="/screenshots/ai_logo.png" width="40%" alt="Azure Insights Logo" /></p>

# Azure Application Insights	Integration for NeoLoad

## Overview

These Advanced Actions allows you to integrate with [Azure ](https://dev.applicationinsights.io) in order to correlate data from one tool to another.

This bundle provides inbound and an outbound integration:
  
* **DynatraceMonitoring**   
    * **Azure Application Insights &rarr; NeoLoad**: Retrieves infrastructure and service metrics from Azure Applicaiton Insights and inserts them in NeoLoad External Data so that
      you can correlate NeoLoad and Azure Application Insights metrics within NeoLoad.

     
| Property | Value |
| -----| -------------- |
| Maturity | Stable |
| Author   | Neotys Partner Team |
| License  | [BSD Simplified](https://www.neotys.com/documents/legal/bsd-neotys.txt) |
| NeoLoad  | 6.+ (Enterprise or Professional Edition w/ Integration & Advanced Usage and NeoLoad Web option required)|
| Bundled in NeoLoad | No
| Download Binaries | <ul><li>[latest release](https://github.com/Neotys-Labs/Dynatrace/releases/latest) is only compatible with NeoLoad from version 6.7</li><li> Use this [release](https://github.com/Neotys-Labs/Dynatrace/releases/tag/Neotys-Labs%2FDynatrace.git-2.0.10) for previous NeoLoad versions</li></ul>|

## Installation

1. Download the [latest release](https://github.com/Neotys-Labs/Dynatrace/releases/latest) for NeoLoad from version 6.7 or this [release](https://github.com/Neotys-Labs/Dynatrace/releases/tag/Neotys-Labs%2FDynatrace.git-2.0.10) for previous NeoLoad versions.
1. Read the NeoLoad documentation to see [How to install a custom Advanced Action](https://www.neotys.com/documents/doc/neoload/latest/en/html/#25928.htm).

<p align="center"><img src="/screenshots/dynatrace_advanced_action.png" alt="New Relic Advanced Action" /></p>

## NeoLoad Set-up

Once installed, how to use in a given NeoLoad project:

1. Create a “Azure Insights” User Path.
1. Insert "AzureMonitoring" in the ‘Actions’ block.
   <p align="center"><img src="/screenshots/dynatrace_user_path.png" alt="Dynatrace User Path" /></p>
1. Select the **Actions** container and set a pacing duration of 30 seconds.
   <p align="center"><img src="/screenshots/actions_container_pacing.png" alt="Action's Pacing" /></p>
1. Select the **Actions** container and set the "Reset user session and emulate new browser between each iteration" runtime parameters to "No".
   <p align="center"><img src="/screenshots/actions_container_reset_iteration_no.png" alt="Action's Runtime parameters" /></p>
1. Create a "PopulationAzure" Population that contains 100% of "AzureMonitoring" User Path.
   <p align="center"><img src="/screenshots/dynatrace_population.png" alt="Dynatrace Population" /></p>
1. In the **Runtime** section, select your scenario, select the "PopulationAzure" population and define a constant load of 1 user for the full duration of the load test.
   <p align="center"><img src="/screenshots/dynatrace_load_variation_policy.png" alt="Load Variation Policy" /></p>
1. Do not use multiple load generators. Good practice should be to keep only the local one.
1. Verify to have a license with "Integration & Advanced Usage".
   <p align="center"><img src="/screenshots/license_integration_and_advanced_usage.png" alt="License with Integration & Advanced Usage" /></p>

## Azure Insights Set-up

You need the Application ID and an API Key to access Application Insights through the API. To get these two keys:
1. In Azure portal, open the Application Insights resource for your application and open Settings, API Access.
1. The Application ID is a unique, unchangeable identifier for this application.
1. Create a new API key, checking the "Read telemetry" box.
1. Copy the key before closing the Create API key blade and save it somewhere secure. (If you lose the key, you'll need to create another.)
<p align="center"><img src="screenshots/create-read-api-key.png" alt="Azure Insights settings" /></p>

## Parameters for Dynatrace Monitoring

Tip: Get NeoLoad API information in NeoLoad preferences: Project Preferences / REST API.

| Name             | Description |
| -----            | ----- |
| ApplicationId      | Identifier of your Azure Application Insights ID |
| AzureApiKey  | API key of your Azure application Insights account |
| dataExchangeApiUrl (Optional)  | Where the DataExchange server is located. Optional, by default it is: http://${NL-ControllerIp}:7400/DataExchange/v1/Service.svc/ |
| dataExchangeApiKey (Optional)  | API key of the DataExchange API |
| proxyName (Optional) |  The name of the NeoLoad proxy to access to Dynatrace |


## Check User Path

This bundle does not work with the Check User Path mode.
A Bad context error should be raised.

## Status Codes

* Azure monitoring
    * NL-AZURE MONITORING_ACTION-01: Could not parse arguments
    * NL-AZURE MONITORING_ACTION-02: Technical Error
    * NL-AZURE MONITORING_ACTION-03: Bad context
