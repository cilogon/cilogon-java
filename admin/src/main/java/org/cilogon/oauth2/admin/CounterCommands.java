package org.cilogon.oauth2.admin;

import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import edu.uiuc.ncsa.security.util.cli.ArgumentNotFoundException;
import edu.uiuc.ncsa.security.util.cli.CommonCommands;
import edu.uiuc.ncsa.security.util.cli.InputLine;
import org.cilogon.oauth2.servlet.storage.user.User;
import org.cilogon.oauth2.servlet.storage.user.UserStore;
import org.cilogon.oauth2.servlet.util.Incrementable;

import java.io.IOException;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/30/13 at  4:05 PM
 */
public class CounterCommands extends CommonCommands {
    @Override
    public void bootstrap() throws Throwable {
        getHelpUtil().load("/help/counter_commands_help.xml");
    }

    public UserStore getUserStore() {
        return userStore;
    }

    UserStore userStore;

    public Incrementable getIncrementable() {
        return incrementable;
    }

    Incrementable incrementable;

    public CounterCommands(MyLoggingFacade logger, String indent, Incrementable incrementable, UserStore userStore) throws Throwable {
        super(logger);
        this.defaultIndent = indent;
        this.incrementable = incrementable;
        this.userStore = userStore;
    }

    @Override
    public String getPrompt() {
        return "  counter>";
    }

    protected void showNextValueHelp() {
        sayi("This will increment the counter and show the result. Syntax:");
        sayi("\nnext_value\n");

    }

    public void next_value(InputLine inputLine) {
        if (showHelp(inputLine)) {
            showNextValueHelp();
            return;
        }

        sayi("next value is " + getIncrementable().nextValue());
    }

    @Override
    public void print_help() throws Exception {
        super.print_help();
        say("--Counter specific commands");
        sayi("next_value = show the next value. Note this does increment it in the store.");
        sayi("reset = reset the counter to a new value. WARNING since ids are made using this, do not use it lightly!");;
    }

    protected void showResetHelp() {
        say2("This will reset the counter for the entire system to a value you specify.");
        say2("Since this changes all user ids for users (and possible other identifiers) use this with extreme caution.");
        say2("Syntax is \n\nreset\n");
        say2("You will then be prompted for the value and given a change to back out of it. Entering an ");
        say2("also end this call. Afterwards, you will be asked if you want to test this. The test consists");
        say2("of getting the next value and seeing that it is the expected one, then creating a new user");
        say2("(but not saving it!) and checking that the user id is not currently in use. This should pass.");
        say2("The checks are not airtight but should reasonably intercept most issues.");
    }

    public void reset(InputLine inputLine) throws IOException {
        if (showHelp(inputLine)) {
            showResetHelp();
            return;
        }
        long newValue = 0L;

        boolean promptForValue = true;
        if (2 == inputLine.size()) {
            try {
                newValue = inputLine.getIntArg(1);
                promptForValue = false;
            } catch (ArgumentNotFoundException ax) {
                // do nothing, just prompt for it
                sayi("Could not parse argument \"" + inputLine.getArg(1) + "\" as a number.");
            }
        }
        long currentValue = getIncrementable().nextValue();
        sayi("This will reset the counter for the system. Be sure you really want to do this!");
        if (promptForValue) {
            String proposedValue = readline("Enter the new value for the system counter [" + currentValue + "]:");
            if (isEmpty(proposedValue)) {
                sayi("You hit return, so no changes will be done. Exiting...");
                return;
            }
            try {
                newValue = Long.parseLong(proposedValue);
            } catch (Throwable t) {
                sayi("Sorry but \"" + proposedValue + "\" is not a number. Operation aborted");
                return;
            }
        }
        if (isOk(readline("Are you sure you want to change the counter to start at \"" + newValue + "\"? [y|n]:"))) {
            try {
                getIncrementable().destroy();
                if(!getIncrementable().createNew(newValue)){
                    sayi("There was a problem, but no exception was thrown.");

                }
            } catch (Throwable throwable) {
                sayi("There was a problem. Message reads " + throwable.getMessage());
                if (isOk(readline("Show stacktrace? [y|n]:"))) {
                    throwable.printStackTrace();
                }
                say2("aborting...");
                return;
            }

        }
        if (isOk(readline("Would you like to test it? [y|n]:"))) {
            sayi("Testing consists of displaying the next value and creating (but not saving) a user record successfully.");
            try {
                long value2 = getIncrementable().nextValue();
                if (value2 != newValue) {
                    // checks that internally it took.
                    throw new IllegalStateException("Error: the expected next value was " + newValue + " and we got " + value2);
                }
                if (!isOk(readline("The next value returned by the counter is " + value2 + ". Does this look right? [y|n]:"))) {
                    say2("ok. You should go check this by some other means. There was an issue which was not detectable");
                    return;
                }
                if (isOk(readline("Now we will create a new user. Proceed? [y|n]:"))) {
                    User user = getUserStore().create(true);
                    if (getUserStore().containsKey(user.getIdentifier())) {
                        throw new IllegalStateException("Error: The user with identifier \"" + user.getIdentifierString() + "\" already exists. This should not happen.");
                    }
                    sayi("A user was created with the following identifier: " + user.getIdentifierString());
                    sayi("You should inspect this and be sure that the expected value is correct.");
                }
            } catch (Throwable throwable) {
                sayi("There was a problem. Message reads " + throwable.getMessage());
                if (isOk(readline("Show stacktrace? [y|n]:"))) {
                    throwable.printStackTrace();
                }
                sayi("aborting...");
                return;

            }
        }
        sayi("done!");
    }

}
