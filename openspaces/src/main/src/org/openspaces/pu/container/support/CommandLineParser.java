package org.openspaces.pu.container.support;

import java.util.List;
import java.util.ArrayList;

/**
 * @author kimchy
 */
public abstract class CommandLineParser {

    public static class Parameter {

        private String name;

        private String[] arguments;

        public Parameter(String name, String[] arguments) {
            this.name = name;
            this.arguments = arguments;
        }

        public String getName() {
            return name;
        }

        public String[] getArguments() {
            return arguments;
        }
    }

    public static Parameter[] parse(String[] args) throws IllegalArgumentException {
        if (args.length == 0) {
            return new Parameter[0];
        }
        if (args.length == 1) {
            throw new IllegalArgumentException("Command line structure is incorrect, only one parameter");
        }
        List params = new ArrayList();
        int index = 0;
        while (index < args.length) {
            if (!args[index].startsWith("-")) {
                throw new IllegalArgumentException("Command line argument [" + args[index] + "] is supposed to start with -");
            }
            if ((index + 1) == args.length) {
                throw new IllegalArgumentException("Command line argument [" + args[index] + "] has no argument");
            }
            String name = args[index].substring(1, args[index].length());
            index += 1;
            List arguments = new ArrayList();
            for (; index < args.length; index++) {
                if (args[index].startsWith("-")) {
                    break;
                }
                arguments.add(args[index]);
            }
            Parameter parameter = new Parameter(name, (String[]) arguments.toArray(new String[arguments.size()]));
            params.add(parameter);
        }
        return (Parameter[]) params.toArray(new Parameter[params.size()]);
    }
}
