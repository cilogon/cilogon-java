package org.cilogon.oauth2.admin;

import edu.uiuc.ncsa.security.core.util.AbstractEnvironment;
import edu.uiuc.ncsa.security.core.util.ConfigurationLoader;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import edu.uiuc.ncsa.security.util.cli.CLIDriver;
import edu.uiuc.ncsa.security.util.cli.CommonCommands;
import edu.uiuc.ncsa.security.util.cli.InputLine;
import edu.uiuc.ncsa.security.util.configuration.XMLConfigUtil;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.lang.StringUtils;
import org.cilogon.oauth2.servlet.loader.CILOA2ConfigurationLoader;
import org.cilogon.oauth2.servlet.loader.CILogonOA2ServiceEnvironment;
import org.cilogon.oauth2.servlet.util.CILogonConfiguration;
import org.oa4mp.server.admin.myproxy.oauth2.base.CopyCommands;
import org.oa4mp.server.admin.myproxy.oauth2.tools.OA2Commands;
import org.oa4mp.server.loader.oauth2.OA2SE;

import static org.cilogon.oauth2.admin.Banners.*;
import static org.cilogon.oauth2.admin.CommandConstants.*;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/25/15 at  2:53 PM
 */
public class CILogonOA2Commands extends OA2Commands {
    public CILogonOA2Commands(MyLoggingFacade logger) {
        super(logger);

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
            loader = new CILOA2ConfigurationLoader<OA2SE>(node, getMyLogger());
        }
        return loader;

    }


    public static void main(String[] args) {
        try {

            CILogonOA2Commands ciLogonCommands = new CILogonOA2Commands(null);
            ciLogonCommands.startup(args);
            if (ciLogonCommands.executeComponent()) {
                return;
            }
            CLIDriver cli = new CLIDriver(ciLogonCommands);
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
            userStoreCommands = new UserStoreCommands(getMyLogger(), indent, getCILogonSE().getUserStore(), getCILogonSE().getArchivedUserStore());
        }
        return userStoreCommands;
    }

    IDPCommands idpCommands = null;

    protected IDPCommands getIdpCommands(String indent) throws Throwable {
        if (idpCommands == null) {
            idpCommands = new IDPCommands(getMyLogger(), indent, getCILogonSE().getIDPStore());
        }
        return idpCommands;
    }

    CounterCommands counterCommands = null;

    protected CounterCommands getCounterCommands(String indent) throws Throwable {
        if (counterCommands == null) {
            counterCommands = new CounterCommands(getMyLogger(), indent, getCILogonSE().getIncrementable(), getCILogonSE().getUserStore());
        }
        return counterCommands;
    }

    ArchivedUserStoreCommands archivedUserStoreCommands = null;

    protected ArchivedUserStoreCommands getArchivedUserStoreCommands(String indent) throws Throwable {
        if (archivedUserStoreCommands == null) {
            archivedUserStoreCommands = new ArchivedUserStoreCommands(getMyLogger(), indent, getCILogonSE().getArchivedUserStore(), getCILogonSE().getUserStore());
        }
        return archivedUserStoreCommands;
    }

    TwoFactorCommands twoFactorCommands = null;

    protected TwoFactorCommands getTwoFactorCommands(String indent) throws Throwable {
        if (twoFactorCommands == null) {
            twoFactorCommands = new TwoFactorCommands(getMyLogger(), indent, getCILogonSE().getTwoFactorStore());
        }
        return twoFactorCommands;

    }


    @Override
    protected void runComponent(String componentName) throws Throwable {
        String indent = "  ";
        CommonCommands commands = null;
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
                commands = new CopyCommands(getMyLogger(), new CILogonOA2CopyTool(), new CILogonOA2CopyToolVerifier(), getConfigFile());
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

        String indent = "  ";
        if (inputLine.hasArg(USERS)) {
            UserStoreCommands usc = new UserStoreCommands(getMyLogger(), indent, getCILogonSE().getUserStore(), getCILogonSE().getArchivedUserStore());
            CLIDriver cli = new CLIDriver(usc);
            cli.start();
            return true;
        }

        if (inputLine.hasArg(IDPS)) {
            IDPCommands usc = new IDPCommands(getMyLogger(), indent, getCILogonSE().getIDPStore());
            CLIDriver cli = new CLIDriver(usc);
            cli.start();
            return true;
        }

        if (inputLine.hasArg(COUNTER)) {
            CounterCommands cc = new CounterCommands(getMyLogger(), indent, getCILogonSE().getIncrementable(), getCILogonSE().getUserStore());
            CLIDriver cli = new CLIDriver(cc);
            cli.start();
            return true;
        }

        if (inputLine.hasArg(ARCHIVED_USER)) {
            ArchivedUserStoreCommands usc = new ArchivedUserStoreCommands(getMyLogger(), indent, getCILogonSE().getArchivedUserStore(), getCILogonSE().getUserStore());
            CLIDriver cli = new CLIDriver(usc);
            cli.start();
            return true;
        }
        if (inputLine.hasArg(TWO_FACTOR)) {
            TwoFactorCommands tfc = new TwoFactorCommands(getMyLogger(), indent, getCILogonSE().getTwoFactorStore());
            CLIDriver cli = new CLIDriver(tfc);
            cli.start();
            return true;
        }
        if (inputLine.hasArg(COPY)) {
            // need a different copy tool than the standard, so load that here.
            CopyCommands cc = new CopyCommands(getMyLogger(), new CILogonOA2CopyTool(), new CILogonOA2CopyToolVerifier(), getConfigFile());
            CLIDriver cli = new CLIDriver(cc);
            cli.start();
            return true;
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
            String stars = StringUtils.rightPad("", width + 1, "*");
            say(stars);
            say(padLineWithBlanks("* CILogon CLI (Command Line Interpreter)", width) + "*");
            say(padLineWithBlanks("* Version " + CILogonConfiguration.VERSION_NUMBER, width) + "*");
            say(padLineWithBlanks("* By Jeff Gaynor  NCSA", width) + "*");
            say(padLineWithBlanks("* type 'help' for a list of commands", width) + "*");
            say(padLineWithBlanks("*      'exit' or 'quit' to end this session.", width) + "*");
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
