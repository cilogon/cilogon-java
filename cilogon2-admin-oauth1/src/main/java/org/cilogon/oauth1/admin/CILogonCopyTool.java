package org.cilogon.oauth1.admin;

import edu.uiuc.ncsa.myproxy.oa4mp.server.CopyTool;
import edu.uiuc.ncsa.security.core.util.AbstractEnvironment;
import edu.uiuc.ncsa.security.core.util.ConfigurationLoader;
import org.cilogon.d2.admin.CILogonCopyExtension;
import org.cilogon.oauth1.loader.CILogonConfigurationLoader;

/**
 * A configuration driven copy tool. Specify a configurations and copy whole stores between them.
 * <p/>
 * <p>Created by Jeff Gaynor<br>
 * on 4/22/12 at  1:22 PM
 */
public class CILogonCopyTool extends CopyTool {

    public CILogonCopyTool() {
        super();
        // The extension copies the other stores (users, archived users, idps, etc.)
        setCopyExtension(new CILogonCopyExtension(this));
    }

    @Override
    public ConfigurationLoader<? extends AbstractEnvironment> getLoader() throws Exception {
        return new CILogonConfigurationLoader(getConfigurationNode(), getMyLogger());
    }



    public static void staticHelp() {
        say("A command line tool to either backup a CILogon server or copy one to another");
        say("usage: CopyCommands options");
        defaultHelp(true);
        say("Where the options are given as -x (fnord) = short option, (long option), and [] = optional. Other options:");
        say("  -" + SOURCE_CONFIG_NAME_OPTION + " (-" + SOURCE_CONFIG_NAME_LONG_OPTION + ") -- set the name of the source server. The source is never modified.");
        say("  -" + CONFIG_FILE_OPTION + " (-" + CONFIG_FILE_LONG_OPTION + ") -- the configuration file for the source server");
        say("  -" + TARGET_CONFIG_NAME_OPTION + " (-" + TARGET_CONFIG_NAME_LONG_OPTION + ") -- the name of the target server");
        say("  -" + TARGET_CONFIG_FILE_OPTION + " (-" + TARGET_CONFIG_FILE_LONG_OPTION + ") --  the name of the target server config file.");
        say("Note 1: The copy is always destructive to the target server. At the end, the target will be replaced by");
        say("        the contents of the source.");
        say("Note 2: Omitting the name of the source implicitly assumes that it is localhost.");
        say("Note 3: If only the source configuration is given, it is assumed that this is the same as the target.");
        say("\nExamples. Copying one server to another:");
        say("  java -jar adminTool.jar -" + TARGET_CONFIG_NAME_OPTION + " polo2");
        say("       -> (Most likely use) Copy the local machine to polo2.");
        say("\nA simple backup example:");
        say("  java -jar adminTool.jar -" + TARGET_CONFIG_NAME_OPTION + " backup");
        say("      -> make a backup of the current server using the standard configuration file and the configuration named 'backup'");
        say("        At the end of this, localhost and its backup are identical.");
        say("\nRestoring a server:");
        say("  java -jar adminTool.jar -" + SOURCE_CONFIG_NAME_OPTION + " backup -" + TARGET_CONFIG_NAME_OPTION + " localhost");
        say("     -> restore the previous backup from localhost.");
        say("\nFull example.");
        say("  java -jar adminTool.jar -"
                + SOURCE_CONFIG_NAME_OPTION + " polo2 -"
                + CONFIG_FILE_OPTION + " /home/jgaynor/cfg.xml -"
                + TARGET_CONFIG_NAME_OPTION + " polo-staging -"
                + TARGET_CONFIG_FILE_OPTION + " /home/jgaynor/cfg.xml");
        say("       -> copy polo2 to polo-staging. Use the specified file for configurations. ");

    }

    @Override
    public void help() {
        staticHelp();
    }

    public static void main(String[] args) {
        CopyTool adminTool = new CILogonCopyTool();
        try {
            adminTool.run(args);
        } catch (Throwable e) {
            // Since this will probably be called only by a bash script, catch all errors and exceptions
            // then return a non-zero exit code
            e.printStackTrace();
            System.exit(1);
        }
    }


}
