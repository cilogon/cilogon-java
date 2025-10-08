package org.cilogon.oauth2.admin;

import edu.uiuc.ncsa.security.core.util.AbstractEnvironment;
import edu.uiuc.ncsa.security.core.util.ConfigurationLoader;
import edu.uiuc.ncsa.security.core.util.StringUtils;
import edu.uiuc.ncsa.security.util.cli.CLIDriver;
import edu.uiuc.ncsa.security.util.cli.CommonCommands2;
import edu.uiuc.ncsa.security.util.cli.InputLine;
import edu.uiuc.ncsa.security.util.configuration.XMLConfigUtil;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.cilogon.oauth2.servlet.loader.CILOA2ConfigurationLoader;
import org.cilogon.oauth2.servlet.loader.CILogonOA2ServiceEnvironment;
import org.cilogon.oauth2.servlet.util.CILogonConfiguration;
import org.oa4mp.server.admin.oauth2.base.CopyCommands;
import org.oa4mp.server.admin.oauth2.tools.OA2Commands;
import org.oa4mp.server.loader.oauth2.OA2SE;

import static org.cilogon.oauth2.admin.Banners.*;
import static org.cilogon.oauth2.admin.CommandConstants.*;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/25/15 at  2:53 PM
 */
public class CILogonOA2Commands extends OA2Commands {
    public CILogonOA2Commands(CLIDriver driver) {
        super(driver);

    }

    @Override
    public String getPrompt() {
        return "cilogon>";
    }

    @Override
    public ConfigurationLoader<? extends AbstractEnvironment> getLoader() {
        if (loader == null) {
            ConfigurationNode node =
                    XMLConfigUtil.findConfiguration(getConfigFile(), getConfigName(), getComponentName());
            loader = new CILOA2ConfigurationLoader<OA2SE>(node, getDriver().getLogger());
        }
        return loader;

    }


    public static void main(String[] args) {

            try {
                InputLine inputLine = new InputLine(OA2Commands.class.getSimpleName(), args);
                CLIDriver cli = new CLIDriver(); // actually run the driver that parses commands and passes them along
                CILogonOA2Commands ciLogonCommands = new CILogonOA2Commands(cli);

                inputLine = cli.bootstrap(inputLine);
                cli.addCommands(ciLogonCommands);

                ciLogonCommands.bootstrap(inputLine); // read the command line options and such to set the state
                cli.start();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

    protected CILogonOA2ServiceEnvironment getCILogonSE() throws Exception {
        return (CILogonOA2ServiceEnvironment) getEnvironment();
    }

    @Override
    protected boolean hasComponent(String componentName) {
        return super.hasComponent(componentName) ||
                componentName.equals(USERS) || componentName.equals(IDPS) ||
                componentName.equals(COUNTER) || componentName.equals(ARCHIVED_USER) ||
                componentName.equals(TWO_FACTOR);
    }

    UserStoreCommands userStoreCommands = null;

    protected UserStoreCommands getUserStoreCommands(String indent) throws Throwable {
        if (userStoreCommands == null) {
            userStoreCommands = new UserStoreCommands(new CLIDriver(), indent, getCILogonSE().getUserStore(), getCILogonSE().getArchivedUserStore());
            configureCommands(getDriver(), userStoreCommands);
            userStoreCommands.initHelp();

        }
        return userStoreCommands;
    }

    IDPCommands idpCommands = null;

    protected IDPCommands getIdpCommands(String indent) throws Throwable {
        if (idpCommands == null) {
            idpCommands = new IDPCommands(new CLIDriver(), indent, getCILogonSE().getIDPStore());
            configureCommands(getDriver(), idpCommands);
            idpCommands.initHelp();

        }
        return idpCommands;
    }

    CounterCommands counterCommands = null;

    protected CounterCommands getCounterCommands(String indent) throws Throwable {
        if (counterCommands == null) {
            counterCommands = new CounterCommands(new CLIDriver(), indent, getCILogonSE().getIncrementable(), getCILogonSE().getUserStore());
            configureCommands(getDriver(), counterCommands);
            counterCommands.initHelp();
        }
        return counterCommands;
    }

    ArchivedUserStoreCommands archivedUserStoreCommands = null;

    protected ArchivedUserStoreCommands getArchivedUserStoreCommands(String indent) throws Throwable {
        if (archivedUserStoreCommands == null) {
            archivedUserStoreCommands = new ArchivedUserStoreCommands(new CLIDriver(), indent, getCILogonSE().getArchivedUserStore(), getCILogonSE().getUserStore());
            configureCommands(getDriver(), archivedUserStoreCommands);
            archivedUserStoreCommands.initHelp();
        }
        return archivedUserStoreCommands;
    }

    TwoFactorCommands twoFactorCommands = null;

    protected TwoFactorCommands getTwoFactorCommands(String indent) throws Throwable {
        if (twoFactorCommands == null) {
            twoFactorCommands = new TwoFactorCommands(new CLIDriver(), indent, getCILogonSE().getTwoFactorStore());
            configureCommands(getDriver(), twoFactorCommands);
            twoFactorCommands.initHelp();
        }
        return twoFactorCommands;

    }


    @Override
    protected void runComponent(String componentName) throws Throwable {
        String indent = "  ";
        CommonCommands2 commands = null;
        switch (componentName) {
            case USERS:
                commands = getUserStoreCommands(indent);
                break;
            case IDPS:
                commands = getIdpCommands(indent);
                break;
            case COUNTER:
                commands = getCounterCommands(indent);
                break;
            case ARCHIVED_USER:
                commands = getArchivedUserStoreCommands(indent);
                break;
            case TWO_FACTOR:
                commands = getTwoFactorCommands(indent);
                break;
            case COPY:
                // older command component. Just make a new one every time.
                commands = new CopyCommands(getDriver(), new CILogonOA2CopyTool(), new CILogonOA2CopyToolVerifier(), getConfigFile());
                break;
        }


        if (commands != null) {
            CLIDriver cli = new CLIDriver(commands);
            cli.start();
            return;
        }

        super.runComponent(componentName);
    }

    @Override
    public boolean use(InputLine inputLine) throws Throwable {
        CommonCommands2 commands = null;

        String indent = "  ";
        if (inputLine.hasArg(USERS)) {
            commands = getUserStoreCommands(indent);
        }
        if (inputLine.hasArg(IDPS)) {
            commands = getIdpCommands(indent);
        }

        if (inputLine.hasArg(COUNTER)) {
            commands = getCounterCommands(indent);
        }
        if (inputLine.hasArg(ARCHIVED_USER)) {
            commands = getArchivedUserStoreCommands(indent);
        }
        if (inputLine.hasArg(TWO_FACTOR)) {
            commands = getTwoFactorCommands(indent);
        }

        if (commands != null) {
            return switchOrRun(inputLine, commands);
        }
        if (super.use(inputLine)) {
            return true;
        }

        say("could not find the component named \"" + inputLine.getArg(1) + "\". Type 'use --help' for help");
        return false;
    }

    @Override
    public void about() {
        about(true, true);
    }

    public void about(boolean showBanner, boolean showHeader) {
        int width = 60;
        String banner = TIMES; // default
        if (logoName.equals("roman")) banner = ROMAN;
        if (logoName.equals("os2")) banner = OS2;
        if (logoName.equals("times")) banner = TIMES;
        if (logoName.equals("fraktur")) banner = FRAKTUR;
        if (logoName.equals("plain")) banner = PLAIN;
        if (logoName.equals("none")) {
            showBanner = false;
        }
        if (showBanner) {
            say(TIMES);
        }
        if (showHeader) {
            String stars = StringUtils.repeatString("*", width + 1);
            say(stars);
            say(StringUtils.pad2("* CILogon CLI (Command Line Interpreter)", width) + "*");
            say(StringUtils.pad2("* Version " + CILogonConfiguration.CILOGON_VERSION_NUMBER, width) + "*");
            say(StringUtils.pad2("* By Jeff Gaynor  NCSA", width) + "*");
            say(StringUtils.pad2("* type 'help' for a list of commands", width) + "*");
            say(StringUtils.pad2("*      'exit' or 'quit' to end this session.", width) + "*");
            say(stars);
        }
    }

    @Override
    public void useHelp() {
        super.useHelp();
        say("CILogon specific components:");
        say("* " + USERS + " - user records");
        say("* " + ARCHIVED_USER + " - archived user records");
        say("* " + COUNTER + " - the current counter (allows to reset it to a new value).");
        say("* " + TWO_FACTOR + " - two factor information");
        say("* " + IDPS + " - identity provider records.\n");
    }

    @Override
    protected ConfigurationLoader<? extends AbstractEnvironment> figureOutLoader(String fileName, String configName) throws Throwable {
        ConfigurationNode node = XMLConfigUtil.findConfiguration(fileName, configName, getComponentName());
        CILOA2ConfigurationLoader serverLoader = new CILOA2ConfigurationLoader<>(node);
        return serverLoader;
    }
}
