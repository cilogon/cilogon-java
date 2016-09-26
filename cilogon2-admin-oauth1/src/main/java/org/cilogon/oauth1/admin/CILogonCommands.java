package org.cilogon.oauth1.admin;

import edu.uiuc.ncsa.myproxy.oa4mp.server.CopyCommands;
import edu.uiuc.ncsa.myproxy.oa4mp.server.OA4MPCommands;
import edu.uiuc.ncsa.security.core.util.AbstractEnvironment;
import edu.uiuc.ncsa.security.core.util.ConfigurationLoader;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import edu.uiuc.ncsa.security.util.cli.CLIDriver;
import edu.uiuc.ncsa.security.util.cli.InputLine;
import org.apache.commons.lang.StringUtils;
import org.cilogon.d2.admin.*;
import org.cilogon.d2.util.CILogonSE;
import org.cilogon.oauth1.loader.CILogonConfigurationLoader;

import static org.cilogon.d2.admin.CommandConstants.*;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 5/23/13 at  9:57 AM
 */
public class CILogonCommands extends OA4MPCommands {
    public CILogonCommands(MyLoggingFacade logger) {
        super(logger);
    }

    @Override
    public String getPrompt() {
        return "cilogon2>";
    }

    @Override
    public ConfigurationLoader<? extends AbstractEnvironment> getLoader() {
        return new CILogonConfigurationLoader(getConfigurationNode(), getMyLogger());
    }

    public static void main(String[] args) {
        try {
            CILogonCommands ciLogonCommands = new CILogonCommands(null);
            ciLogonCommands.start(args);
            if (ciLogonCommands.executeComponent()) {
                return;
            }
            CLIDriver cli = new CLIDriver(ciLogonCommands);
            cli.start();
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }

    protected CILogonSE getCILogonSE() throws Exception {
        return (CILogonSE) getEnvironment();
    }

    @Override
    protected boolean hasComponent(String componentName) {
        return super.hasComponent(componentName) ||
                componentName.equals(USERS) || componentName.equals(IDPS) ||
                componentName.equals(COUNTER) || componentName.equals(ARCHIVED_USER) ||
                componentName.equals(TWO_FACTOR);
    }

    @Override
    protected void runComponent(String componentName) throws Exception {
        String indent = "  ";
        if (componentName.equals(USERS)) {
            UserStoreCommands usc = new UserStoreCommands(getMyLogger(), indent, getCILogonSE().getUserStore(), getCILogonSE().getArchivedUserStore());
            CLIDriver cli = new CLIDriver(usc);
            cli.start();
            return;
        }

        if (componentName.equals(IDPS)) {
            IDPCommands usc = new IDPCommands(getMyLogger(), indent, getCILogonSE().getIDPStore());
            CLIDriver cli = new CLIDriver(usc);
            cli.start();
            return;
        }

        if (componentName.equals(COUNTER)) {
            CounterCommands cc = new CounterCommands(getMyLogger(), indent, getCILogonSE().getIncrementable(), getCILogonSE().getUserStore());
            CLIDriver cli = new CLIDriver(cc);
            cli.start();
            return;
        }

        if (componentName.equals(ARCHIVED_USER)) {
            ArchivedUserStoreCommands usc = new ArchivedUserStoreCommands(getMyLogger(), indent, getCILogonSE().getArchivedUserStore(), getCILogonSE().getUserStore());
            CLIDriver cli = new CLIDriver(usc);
            cli.start();
            return;
        }
        if (componentName.equals(TWO_FACTOR)) {
            TwoFactorCommands tfc = new TwoFactorCommands(getMyLogger(), indent, getCILogonSE().getTwoFactorStore());
            CLIDriver cli = new CLIDriver(tfc);
            cli.start();
            return;
        }
        if (componentName.equals(COPY)) {
            CopyCommands cc = new CopyCommands(getMyLogger(), new CILogonCopyTool(), new CILogonCopyToolVerifier(), getConfigFile());
            CLIDriver cli = new CLIDriver(cc);
            cli.start();
            return;
        }
        super.runComponent(componentName);
    }

    @Override
    public boolean use(InputLine inputLine) throws Exception {

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
            CopyCommands cc = new CopyCommands(getMyLogger(), new CILogonCopyTool(), new CILogonCopyToolVerifier(), getConfigFile());
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
        int width = 60;
        String stars = StringUtils.rightPad("", width + 1, "*");
        say(stars);
        say(padLineWithBlanks("* CILogon 2 CLI (Command Line Interpreter)", width) + "*");
        say(padLineWithBlanks("* Version " + CILogonConfigurationLoader.VERSION_NUMBER, width) + "*");
        say(padLineWithBlanks("* By Jeff Gaynor  NCSA", width) + "*");
        say(padLineWithBlanks("*  (National Center for Supercomputing Applications)", width) + "*");
        say(padLineWithBlanks("*", width) + "*");
        say(padLineWithBlanks("* type 'help' for a list of commands", width) + "*");
        say(padLineWithBlanks("*      'exit' or 'quit' to end this session.", width) + "*");
        say(stars);
    }

    @Override
    public void useHelp() {
        say("Choose the component you wish to use.");
        say("you specify the component as use + name. Supported components to manage are:\n");
        say("* " + CLIENTS + " - client records");
        say("* " + CLIENT_APPROVALS + " - client approval records");
        say("* " + USERS + " - user records");
        say("* " + ARCHIVED_USER + " - archived user records");
        say("* " + COUNTER + " - the current counter (allows to reset it to a new value).");
        say("* " + TWO_FACTOR + " - two factor information");
        say("* " + IDPS + " - identity provider records.\n");
        say("e.g.\n\nuse " + CLIENTS + "\n\nwill call up the client management component.");
        say("Type 'exit' when you wish to exit the component and return to the main menu");
    }
}
