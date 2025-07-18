package org.cilogon.oauth2.admin;

import edu.uiuc.ncsa.security.core.Identifiable;
import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.Store;
import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import edu.uiuc.ncsa.security.util.cli.CLIDriver;
import edu.uiuc.ncsa.security.util.cli.InputLine;
import org.cilogon.oauth2.servlet.storage.archiveUser.ArchivedUser;
import org.cilogon.oauth2.servlet.storage.archiveUser.ArchivedUserStore;
import org.cilogon.oauth2.servlet.storage.user.User;
import org.cilogon.oauth2.servlet.storage.user.UserNotFoundException;
import org.cilogon.oauth2.servlet.storage.user.UserStore;
import org.oa4mp.server.admin.oauth2.base.OA4MPStoreCommands;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 5/23/13 at  11:45 AM
 */
public class ArchivedUserStoreCommands extends OA4MPStoreCommands {

    public static final String USER_FLAG = "u";


    public ArchivedUserStoreCommands(CLIDriver driver, String defaultIndent, ArchivedUserStore archivedUserStore, UserStore userStore) throws Throwable{
        super(driver, defaultIndent, archivedUserStore);
        this.userStore = userStore;
    }

    public UserStore getUserStore() {
        return userStore;
    }

    public void setUserStore(UserStore userStore) {
        this.userStore = userStore;
    }

    UserStore userStore;

    public ArchivedUserStoreCommands(CLIDriver driver, Store store) throws Throwable {
        super(driver, store);
    }

    @Override
    protected String format(Identifiable identifiable) {
        ArchivedUser archivedUser = (ArchivedUser) identifiable;
        User user = null;
        try {
            user = archivedUser.getUser();
        } catch (UserNotFoundException unfx) {
            // this is benign and can occur if the user is removed frrom the system.
        }
        String lastName = isEmpty(user.getLastName()) ? "-" : user.getLastName();
        String firstName = isEmpty(user.getFirstName()) ? "-" : user.getFirstName();
        String out = lastName + ", " + firstName;
        out = out + ", " + (user == null ? "(no active user)" : " user id=" + user.getIdentifier()) + ", archived " + archivedUser.getArchivedDate();
        return out;
    }

    @Override
    public String getName() {
        return "  archived users";
    }

    @Override
    public boolean update(Identifiable identifiable) {
        info("Attempt to update archived user with id=" + identifiable.getIdentifierString() + " was refused since updates are not currently supported.");
        sayi("Sorry, but you cannot update archived users. This operation is currently unsupported");
        return false;
    }

    protected User findUser(InputLine inputLine) throws Throwable {
        Identifiable x = super.findSingleton(inputLine);
        if (x == null) {
            // then this was not found in the default store and was not recognized as
            // having an integer argument. Try to get it from the user store
            String arg = inputLine.getLastArg().substring(1);
            Identifier id = BasicIdentifier.newID(arg);
            return getUserStore().get(id);
        }
        // the supplied item is an archived user.
        return ((ArchivedUser) x).getUser();
    }

    protected void listByUser() {
        Set<Identifier> aUserIds = getArchivedUserStore().keySet();
        HashSet<Identifier> uniqueUserIds = new HashSet<Identifier>();
        for (Identifier id : aUserIds) {
            uniqueUserIds.add(getArchivedUserStore().get(id).getUser().getIdentifier());
        }
        int counter = 0;
        int badCount = 0;
        // so we now have a set of unique user ids that have been archived.
        for (Identifier userid : uniqueUserIds) {
            try {
                User user = getUserStore().get(userid);
                printUsers(counter++, user);
            } catch (UserNotFoundException unfx) {
                badCount++;
            }
        }
        sayi("Found " + counter + " users.");
        if (0 < badCount) {
            say("There were " + badCount + " archived users with no associated user");
        }
    }

    protected void printUsers(int counter, User user) {
        String lastName = isEmpty(user.getLastName()) ? "-" : user.getLastName();
        String firstName = isEmpty(user.getFirstName()) ? "-" : user.getFirstName();

        List<ArchivedUser> aUsers = getArchivedUserStore().getAllByUserId(user.getIdentifier());
        String x = "";
        if (0 <= counter) {
            x = counter + ". ";
        }
        say(x + lastName + ", " + firstName + ", (" + aUsers.size() + " entries), id=" + user.getIdentifier());
        for (ArchivedUser a : aUsers) {
            sayi(INDENT + "archive id=" + a.getIdentifier() + ", archived on " + a.getArchivedDate());
        }
    }

    @Override
    public void ls(InputLine inputLine) throws Throwable {
        if (showHelp(inputLine) || 3 < inputLine.size()) {
            showLSHelp();
            return;
        }
        if (inputLine.hasArg(USER_FLAG)) {
            // no id. This means show everyone.
            if (inputLine.getLastArg().equals(USER_FLAG)) {
                listByUser();
                return;
            }else {
                // then they want to the specific users for this archive entry
                if (inputLine.getArg(1).contains(USER_FLAG)) {
                    User u = findUser(inputLine);
                    if (u == null) {
                        sayi("user not found for the given identifier.");
                        return;
                    }
                    printUsers(-1, u);
                    return;
                }
            }
        }
        super.ls(inputLine);
    }

    protected void extractUser(InputLine inputLine) throws Throwable {
        Identifiable x = findSingleton(inputLine, "user not found");
        if (x != null) {
            longFormat(x);
            return;
        }
        // here's the problem -- we want to show either the archived users for a given
        // user id OR the specific information for an archived user.
        try {
            User user = findUser(inputLine);
            List<ArchivedUser> aUsers = getArchivedUserStore().getAllByUserId(user.getIdentifier());
            sayi(user.getLastName() + ", " + user.getFirstName() + ", (" + aUsers.size() + " entries), id = " + user.getIdentifier());
            for (ArchivedUser a : aUsers) {
                sayi("archive id=" + a.getIdentifier() + ", archived on " + a.getArchivedDate());
            }
        } catch (Throwable throwable) {
            sayi("Sorry, user not found. Type \"ls --help\" for help.");
        }
    }

    protected ArchivedUserStore getArchivedUserStore() {
        return (ArchivedUserStore) getStore();
    }

    @Override
    protected void showLSHelp() {
        super.showLSHelp();
        say("Listing users and archived users.");
        say("--------------------------------.");
        say("Syntax:\n");
        say("ls [-u [user]] index\n");
        say("List the entries of a user.");
        say("If the id refers to an archived entry, that will be shown.");
        say("Entering \"ls -u\" will list every archived user, but organized by user id.");
        say("Warning: That means teh entire store is show, which may be simply enormous. ");
        printIndexHelp(true);
    }


    protected void showRestoreHelp() {
        sayi("This restores an archived user's record. Syntax:\n");
        sayi("restore index|archived id\n");
        sayi("Note that the id is the archived id, not the user id. This will give you the");
        sayi("option of archiving the currently active user record first before replacing it");
        sayi("with the stated archived user's information.");
    }

    public void restore(InputLine inputLine) {
        if (showHelp(inputLine)) {
            showRestoreHelp();
            return;
        }
        info("Starting restore of archived user");
        ArchivedUser archivedUser = null;
        String NOT_FOUND_MSG = "Sorry, but I cannot seem to find the archived user with that index or id. Try again.";
        try {
            archivedUser = (ArchivedUser) findSingleton(inputLine);
        } catch (Throwable t) {
            info("Attempt to restore archived user failed since no user was found for the given id");
            sayi(NOT_FOUND_MSG);
            return;
        }
        if (archivedUser == null) {
            info("Attempt to restore archived user failed since no user was found for the given id");
            sayi(NOT_FOUND_MSG);
            return;
        }
        sayi("This is the archived user record you specified:");
        info("restoring archived user with id=" + archivedUser.getIdentifierString());
        longFormat(archivedUser);
        User oldUser = archivedUser.getUser();
        sayi("Are you sure you want to restore this user (id=\"" + oldUser.getIdentifierString() + "\"), overwriting any current user information?[y|n]:");
        if (!isOk(readline())) {
            sayi("Restore aborted. Returning.");
            info("Restoring archived user with id=" + archivedUser.getIdentifierString() + " aborted.");
            return;
        }
        boolean userActive = getUserStore().containsKey(oldUser.getIdentifier());
        User currentUser = null;
        if (userActive) {
            currentUser = getUserStore().get(oldUser.getIdentifier());
            if (oldUser.equals(currentUser)) {
                sayi("The currently archived user is identical to the active user. Did you still want to restore? [y|n]:");
                if (!isOk(readline())) {
                    info("Restoring archived user with id=" + archivedUser.getIdentifierString() + " aborted.");
                    sayi("Restore aborted. Returning.");
                    return;
                }
            }
            sayi("Did you want to archived the currently active user with id \"" + oldUser.getIdentifierString() + "\"? [y|n]:");
            if (isOk(readline())) {
                info("Archiving currently active user with id=" + oldUser.getIdentifierString());
                getArchivedUserStore().archiveUser(oldUser.getIdentifier());
                sayi("User archived.");
            }

        } else {
            currentUser = getUserStore().create();
            currentUser.setIdentifier(oldUser.getIdentifier());
        }
        // so now we copy everything back to the current user record and save it.
        oldUser.copyTo(currentUser, false); // copy, don't change the id of the current user (since that was set earlier)
        getUserStore().save(currentUser);
        info("User with id=" + currentUser.getIdentifierString() + " restored from archived user with id=" + archivedUser.getIdentifierString());
        sayi("User restored from archive. Done!");

    }

    @Override
    public void change_id(InputLine inputLine) throws Throwable {
        say("Altering the ID for an archived user is not supported");
    }

    @Override
    protected int updateStorePermissions(Identifier newID, Identifier oldID, boolean copy) {
        return 0;
    }
}
