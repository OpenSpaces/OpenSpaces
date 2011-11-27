package org.openspaces.pu.container.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jini.rio.core.RequiredDependencies;
import org.jini.rio.core.RequiredDependency;

import com.gigaspaces.internal.utils.StringUtils;
/**
 * Converts {@link RequiredDependencies} to command-line parameters (GSM Operational String)
 * Currently supported are only dependencies that are required before a ProcessingUnit instance is deployed on a GSC. 
 * @author itaif
 */
public class RequiredDependenciesCommandLineParser {

    public static final String INSTANCE_DEPLOYMENT_REQUIRED_DEPENDENCIES_PARAMETER_NAME = "deployment-dependencies";
    public static final String INSTANCE_START_REQUIRED_DEPENDENCIES_PARAMETER_NAME = "start-dependencies";
    
    // Separator between key.value pairs (name=service1)
    private static final String DEPENDENCY_KEYVALUE_SEPERATOR = "=";

    // Separator between properties pairs (name=service1,waitForNumberOfInstances=1)
    private static final String DEPENDENCY_PROPERTIES_SEPERATOR = ",";

    public static CommandLineParser.Parameter convertRequiredDependenciesToCommandLineParameter(String commandLineParameter, RequiredDependencies requiredDependencies) {
        
        List<String> opStringArguments = new ArrayList<String>();
        for (String requiredDependencyName : requiredDependencies.getRequiredDependenciesNames()) {
            Map<String,String> properties = requiredDependencies.getRequiredDependencyByName(requiredDependencyName).getProperties();
            String[] keyValuePairs = StringUtils.convertKeyValuePairsToArray(properties, DEPENDENCY_KEYVALUE_SEPERATOR);
            String opStringArgument = StringUtils.arrayToDelimitedString(keyValuePairs, DEPENDENCY_PROPERTIES_SEPERATOR);
            opStringArguments.add(opStringArgument);
        }
        return new CommandLineParser.Parameter(commandLineParameter, opStringArguments.toArray(new String[opStringArguments.size()]));
    }

    
    public static boolean isInstanceDeploymentDependencies(CommandLineParser.Parameter parameter) {
        return parameter.getName().equalsIgnoreCase(INSTANCE_DEPLOYMENT_REQUIRED_DEPENDENCIES_PARAMETER_NAME);
    }
    
    public static boolean isInstanceStartDependencies(CommandLineParser.Parameter parameter) {
        return parameter.getName().equalsIgnoreCase(INSTANCE_START_REQUIRED_DEPENDENCIES_PARAMETER_NAME);
    }
    
    public static RequiredDependencies convertCommandlineParameterToInstanceDeploymentDependencies(CommandLineParser.Parameter parameter) {
            if (!isInstanceDeploymentDependencies(parameter)) {
                throw new IllegalArgumentException("Parameter is " + parameter.getName() + " instead of " + INSTANCE_DEPLOYMENT_REQUIRED_DEPENDENCIES_PARAMETER_NAME);
            }
            
            return convertCommandlineParameterToRequiredDependencies(parameter);
    }

    public static RequiredDependencies convertCommandlineParameterToInstanceStartDependencies(CommandLineParser.Parameter parameter) {
        if (!isInstanceStartDependencies(parameter)) {
            throw new IllegalArgumentException("Parameter is " + parameter.getName() + " instead of " + INSTANCE_START_REQUIRED_DEPENDENCIES_PARAMETER_NAME);
        }
        
        return convertCommandlineParameterToRequiredDependencies(parameter);
    }
    
    private static RequiredDependencies convertCommandlineParameterToRequiredDependencies(
            CommandLineParser.Parameter parameter) {
        final RequiredDependencies requiredDependencies = new RequiredDependencies();
        for (final String opStringArgument : parameter.getArguments()) {
            final String[] keyValueParis = StringUtils.delimitedListToStringArray(opStringArgument, DEPENDENCY_PROPERTIES_SEPERATOR);
            final Map<String, String> properties = StringUtils.convertArrayToKeyValuePairs(keyValueParis, DEPENDENCY_KEYVALUE_SEPERATOR);
            requiredDependencies.addRequiredDependency(new RequiredDependency(properties));
        }
        return requiredDependencies;
    }
}
