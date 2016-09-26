package org.cilogon.d2.admin;

import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import edu.uiuc.ncsa.security.util.cli.ArgumentNotFoundException;
import edu.uiuc.ncsa.security.util.cli.CommonCommands;
import edu.uiuc.ncsa.security.util.cli.InputLine;
import org.cilogon.d2.storage.User;
import org.cilogon.d2.storage.UserStore;
import org.cilogon.d2.util.Incrementable;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/30/13 at  4:05 PM
 */
public class CounterCommands extends CommonCommands {

    public UserStore getUserStore() {
        return userStore;
    }

    UserStore userStore;

    public Incrementable getIncrementable() {
        return incrementable;
    }

    Incrementable incrementable;

    public CounterCommands(MyLoggingFacade logger, String indent, Incrementable incrementable, UserStore userStore) {
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
        sayi("\nnextValue\n");

    }

    public void nextvalue(InputLine inputLine) {
        if (showHelp(inputLine)) {
            showNextValueHelp();
            return;
        }

        sayi("next value is " + getIncrementable().nextValue());
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

    public void reset(InputLine inputLine) {
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
            sayi2("Enter the new value for the system counter [" + currentValue + "]:");
            String proposedValue = readline();
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
        sayi2("Are you sure you want to change the counter to start at \"" + newValue + "\"? [y|n]:");
        if (isOk(readline())) {
            try {
                getIncrementable().destroy();
                if(!getIncrementable().createNew(newValue)){
                    sayi("There was a problem, but no exception was thrown.");

                }
            } catch (Throwable throwable) {
                sayi("There was a problem. Message reads " + throwable.getMessage());
                sayi2("Show stacktrace? [y|n]:");
                if (isOk(readline())) {
                    throwable.printStackTrace();
                }
                say2("aborting...");
                return;
            }

        }
        sayi2("Would you like to test it? [y|n]:");
        if (isOk(readline())) {
            sayi("Testing consists of displaying the next value and creating (but not saving) a user record successfully.");
            try {
                long value2 = getIncrementable().nextValue();
                if (value2 != newValue) {
                    // checks that internally it took.
                    throw new IllegalStateException("Error: the expected next value was " + newValue + " and we got " + value2);
                }
                sayi2("The next value returned by the counter is " + value2 + ". Does this look right? [y|n]:");
                if (!isOk(readline())) {
                    say2("ok. You should go check this by some other means. There was an issue which was not detectable");
                    return;
                }
                sayi2("Now we will create a new user. Proceed? [y|n]:");
                if (isOk(readline())) {
                    User user = getUserStore().create(true);
                    if (getUserStore().containsKey(user.getIdentifier())) {
                        throw new IllegalStateException("Error: The user with identifier \"" + user.getIdentifierString() + "\" already exists. This should not happen.");
                    }
                    sayi("A user was created with the following identifier: " + user.getIdentifierString());
                    sayi("You should inspect this and be sure that the expected value is correct.");
                }
            } catch (Throwable throwable) {
                sayi("There was a problem. Message reads " + throwable.getMessage());
                sayi2("Show stacktrace? [y|n]:");
                if (isOk(readline())) {
                    throwable.printStackTrace();
                }
                sayi("aborting...");
                return;

            }
        }
        sayi("done!");
    }
}
