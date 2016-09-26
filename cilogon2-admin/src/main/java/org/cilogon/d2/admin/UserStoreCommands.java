package org.cilogon.d2.admin;

import edu.uiuc.ncsa.security.core.Identifiable;
import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.Store;
import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import edu.uiuc.ncsa.security.util.cli.BasicSorter;
import edu.uiuc.ncsa.security.util.cli.InputLine;
import edu.uiuc.ncsa.security.util.cli.StoreCommands;
import org.cilogon.d2.storage.*;
import org.cilogon.d2.util.ArchivedUserStore;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 5/23/13 at  9:47 AM
 */
public class UserStoreCommands extends StoreCommands {

    @Override
    public void extraUpdates(Identifiable identifiable) {
    }

    public UserStoreCommands(MyLoggingFacade logger, String defaultIndent, Store userStore, ArchivedUserStore archivedUserStore) {
        super(logger, defaultIndent, userStore);
        this.archivedUserStore = archivedUserStore;
        setSortable(new BasicSorter());
    }

    ArchivedUserStore archivedUserStore;

    public UserStoreCommands(MyLoggingFacade logger, UserStore userStore) {
        super(logger, userStore);
        setSortable(new BasicSorter());
    }

    protected UserStore getUserStore() {
        return (UserStore) getStore();
    }

    public ArchivedUserStore getArchivedUserStore() {
        return archivedUserStore;
    }

    public void setArchivedUserStore(ArchivedUserStore archivedUserStore) {
        this.archivedUserStore = archivedUserStore;
    }

    @Override
    protected String format(Identifiable identifiable) {
        User user = (User) identifiable;
        String lastName = isEmpty(user.getLastName()) ? "-" : user.getLastName();
        String firstName = isEmpty(user.getFirstName()) ? "-" : user.getFirstName();
        return lastName + ", " + firstName + ", id=" + user.getIdentifierString();
    }


    @Override
    public String getName() {
        return "  user";
    }

    protected void showArchiveHelp() {
        say("Archive a given user.");
        say("Syntax:\n");
        say("archive [index|uid]\n");
        say("where index is the one given by the list (ls) command or you may supply the user's identifier, escaped with a /");
        say("Archiving a user has the following effects:\n");
        say("* it will create a new archived user entry in that store");
        say("* this entry will have all the current user information in it.");
        say("* the new archived user's id will be displayed.");
        say("You should only archive a user if the user's information has changed. Good practice is to archive the user if there are");
        say("any changes to the information rather than just editing the user's record. Archive first, then edit.");
        say("Examples\n");
        say("archive 4");
        say("This creates an archive entry for the user with index 4 in the list command\n");
        say("archive /http://cilogon.org/serverX/users/43ab44e8df7345cba");
        say("This archives the user with the given unique identifier.");
    }

    public void archive(InputLine inputLine) {
        if (showHelp(inputLine)) {
            showArchiveHelp();
            return;
        }
        User user = (User) findItem(inputLine);
        Identifier newID = getArchivedUserStore().archiveUser(user.getIdentifier());
        sayi("New archived user id=" + newID);
    }

    @Override
    public boolean update(Identifiable identifiable) {
        User user = (User) identifiable;
        String input = null;
        say("Update the values. A return accepts the existing or default value in []'s");
        input = getInput("enter the identifier", user.getIdentifierString());

        user.setFirstName(getInput("first name", user.getFirstName()));
        user.setLastName(getInput("last name", user.getLastName()));
        String temp = null;
        getRemoteUser(user);
        getEPPN(user);
        getEPTID(user);
        getOpenID(user);
        getOpenIDConnect(user);

        user.setIdP(getInput("idp", user.getIdP()));
        user.setEmail(getInput("email", user.getEmail()));
        user.setIDPName(getInput("idp name", user.getIDPName()));
        user.setAffiliation(getInput("affiliation", user.getAffiliation()));
        user.setOrganizationalUnit(getInput("organizational unit", user.getOrganizationalUnit()));
        user.setDisplayName(getInput("user's display name", user.getDisplayName()));
        sayi("Current serial identifier is \"" + user.getSerialIdentifier() + "\"");
        sayi("Autogenerate a new serial identifier [y/n]?");
        boolean noNewSID = !isOk(readline()); // since the question is positive but the argument is neg., this is right.
        if(noNewSID) {
            String sid = getInput("serial string", user.getSerialIdentifier().toString());
            if (sid != null) {
                user.setSerialIdentifier(BasicIdentifier.newID(sid));
            }
        }
        sayi("save changes [y/n]?");
        boolean saveIt = isOk(readline());
        if (saveIt) {
            if (!input.equals(user.getIdentifierString())) {
                sayi2("remove user with id=\"" + user.getIdentifier() + "\" [y/n]? ");
                if (isOk(readline())) {
                    getStore().remove(user.getIdentifier());
                    sayi(" user removed. Be sure to save any changes.");
                }
                user.setIdentifier(BasicIdentifier.newID(input));
            }
            getUserStore().update(user,noNewSID);
            clearEntries();
        }

        // do the saving here since we have to make a choice about serial identifiers.
        // returning anything other than false will cause a new serial id to be created every time.
        return false;

    }

    protected String getPersonPrompt(String prompt, PersonName person) {
        String temp;
        if (person == null) {
            temp = getInput(prompt, null);
        } else {
            temp = getInput(prompt, person.getName());
        }
        return temp;
    }

    protected void getOpenID(User user) {
        String temp = getPersonPrompt("open id", user.getOpenID());
        if (!isEmpty(temp)) {
            user.setOpenID(new OpenID(temp));
        } else {
            user.setOpenID(null);
        }
    }

    protected void getOpenIDConnect(User user) {
        String temp = getPersonPrompt("open id connect", user.getOpenIDConnect());
        if (!isEmpty(temp)) {
            user.setOpenIDConnect(new OpenIDConnect(temp));
        } else {
            user.setOpenIDConnect(null);
        }
    }

    protected void getEPTID(User user) {
        String temp = getPersonPrompt("eptid", user.getePTID());
        if (!isEmpty(temp)) {
            user.setePTID(new EduPersonTargetedID(temp));
        } else {
            user.setePTID(null);
        }
    }

    protected void getEPPN(User user) {
        String temp = getPersonPrompt("eppn", user.getePPN());
        if (!isEmpty(temp)) {
            user.setePPN(new EduPersonPrincipleName(temp));
        } else {
            user.setePPN(null);
        }
    }

    protected void getRemoteUser(User user) {
        String temp = getPersonPrompt("remote user", user.getRemoteUser());
        if (!isEmpty(temp)) {
            user.setRemoteUser(new RemoteUserName(temp));
        } else {
            user.setRemoteUser(null);
        }
    }

    @Override
    protected void longFormat(Identifiable identifiable) {
        User user = (User) identifiable;
        sayi(getValue(user.getLastName()) + ", " + getValue(user.getFirstName()));
        sayi("user id=" + user.getIdentifier());
        sayi("DN=" + user.getDN(null));
        sayi("remote user=" + user.getRemoteUser() + ", email=" + user.getEmail());
        sayi("idp=" + user.getIdP() + ", idp name=" + user.getIDPName());
        sayi("serial string =" + user.getSerialString());
    }

    @Override
    public void rm(InputLine inputLine) {
        if (showHelp(inputLine)) {
            showRMHelp();
            return;
        }
        Identifiable x = findItem(inputLine);

        sayi2("Archive user record before removing it? [y/n]:");
        if (isOk(readline())) {
            Identifier auId = getArchivedUserStore().archiveUser(x.getIdentifier());
            sayi("User archive record create with id =\"" + auId + "\".");
        }
        getStore().remove(x.getIdentifier());
        say("Done. object with id = " + x.getIdentifierString() + " has been removed from the store");
        clearEntries();
    }
}
