package uk.gov.dwp.uc.dip;


import org.apache.commons.cli.CommandLine;

final public class Configuration {
    public static CommandLine cmd = null;

    public static CommandLine getConfiguration(){
        return cmd;
    }
}
