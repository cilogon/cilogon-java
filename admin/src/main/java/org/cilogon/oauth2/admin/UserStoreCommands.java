package org.cilogon.oauth2.admin;

import edu.uiuc.ncsa.security.core.Identifiable;
import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.Store;
import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import edu.uiuc.ncsa.security.util.cli.BasicSorter;
import edu.uiuc.ncsa.security.util.cli.InputLine;
import org.cilogon.oauth2.servlet.loader.CILogonOA2ServiceEnvironment;
import org.cilogon.oauth2.servlet.storage.archiveUser.ArchivedUser;
import org.cilogon.oauth2.servlet.storage.archiveUser.ArchivedUserStore;
import org.cilogon.oauth2.servlet.storage.transaction.CILOA2ServiceTransaction;
import org.cilogon.oauth2.servlet.storage.transaction.CILOA2TransactionKeys;
import org.cilogon.oauth2.servlet.storage.user.*;
import org.cilogon.oauth2.servlet.util.DNUtil;
import org.oa4mp.delegation.server.ServiceTransaction;
import org.oa4mp.server.admin.myproxy.oauth2.base.OA4MPStoreCommands;

import java.io.IOException;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 5/23/13 at  9:47 AM
 */
public class UserStoreCommands extends OA4MPStoreCommands {


    public UserStoreCommands(MyLoggingFacade logger, String defaultIndent, Store userStore, ArchivedUserStore archivedUserStore) throws Throwable {
        super(logger, defaultIndent, userStore);
        this.archivedUserStore = archivedUserStore;
        setSortable(new BasicSorter());
    }

    ArchivedUserStore archivedUserStore;

    public UserStoreCommands(MyLoggingFacade logger, UserStore userStore) throws Throwable {
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
    protected User createEntry(int magicNumber) {
        return getUserStore().create(true); // create a user with a new identifier.
    }

    @Override
    protected String format(Identifiable identifiable) {
        User user = (User) identifiable;
        String lastName = isEmpty(user.getLastName()) ? "-" : user.getLastName();
        String firstName = isEmpty(user.getFirstName()) ? "-" : user.getFirstName();
        return lastName + ", " + firstName + ", id=" + user.getIdentifierString();
    }

    @Override
    public void print_help() throws Exception {
        super.print_help();
        say("--Archive specific:");
        sayi("archive = archive a given user.");
    }

    @Override
    public String getName() {
        return "user";
    }

    protected void showArchiveUserHelp() {
        say("(deprecated) Archive a given user, using the archive store.");
        say("Syntax:\n");
        say("archive index\n");
        say("where index is the one given by the list (ls) command or you may supply the user's identifier, escaped with a /");
        printIndexHelp(true);
        say("Archiving a user has the following effects:\n");
        say("* it will create a new archived user entry in that store");
        say("* this entry will have all the current user information in it.");
        say("* the new archived user's id will be displayed.");
        say("You should only archive a user if the user's information has changed. Good practice is to archive the user if there are");
        say("any changes to the information rather than just editing the user's record. Archive first, then edit.");
        say("Examples\n");
        say("archive 4");
        say("This creates an archive entry for the user with index 4 in the list command\n");
        say("archive http://cilogon.org/serverX/users/43ab44e8df7345cba");
        say("This archives the user with the given unique identifier.");
    }

    public void archiveUser(InputLine inputLine) throws Throwable {
        if (showHelp(inputLine)) {
            showArchiveUserHelp();
            return;
        }
        User user = (User) findSingleton(inputLine, "user not found");
        Identifier newID = getArchivedUserStore().archiveUser(user.getIdentifier());
        sayi("New archived user id=" + newID);
    }

    @Override
    public void extraUpdates(Identifiable identifiable, int magicNumber) throws IOException {
        super.extraUpdates(identifiable, magicNumber);
        User user = (User) identifiable;
        UserKeys keys = (UserKeys) getSerializationKeys();
        user.setFirstName(getPropertyHelp(keys.firstName(), "first name", user.getFirstName()));
        user.setLastName(getPropertyHelp(keys.lastName(), "last name", user.getLastName()));
        user.setUseUSinDN(getPropertyHelp(keys.useUSinDN(), "is IDP in US (y/n)?", "y").equalsIgnoreCase("y"));

        // have to get the next items so that the values are created if possible
        getRemoteUser(user);
        getEPPN(user);
        getEPTID(user);
        getOpenID(user);
        getOpenIDConnect(user);

        user.setIdP(getPropertyHelp(keys.idp(), "idp", user.getIdP()));
        user.setEmail(getPropertyHelp(keys.email(), "email", user.getEmail()));
        user.setIDPName(getPropertyHelp(keys.idpDisplayName(), "idp name", user.getIDPName()));
        user.setAffiliation(getPropertyHelp(keys.affiliation(), "affiliation", user.getAffiliation()));
        user.setOrganizationalUnit(getPropertyHelp(keys.organizationalUnit(), "organizational unit", user.getOrganizationalUnit()));
        user.setDisplayName(getPropertyHelp(keys.displayName(), "user's display name", user.getDisplayName()));
        sayi("Current serial identifier is \"" + user.getSerialIdentifier() + "\"");
        if (getPropertyHelp(keys.serialString(), "Manually set new serial identifier [y/n]?", "n").equalsIgnoreCase("y")) {
            String sid = getPropertyHelp(keys.serialString(), "  enter new serial string", user.getSerialIdentifier().toString());
            if (sid != null) {
                user.setSerialIdentifier(BasicIdentifier.newID(sid));
            }
        }
    }


    protected String getPersonPrompt(String prompt, PersonName person) throws IOException {
        String temp;
        if (person == null) {
            temp = getInput(prompt, null);
        } else {
            temp = getInput(prompt, person.getName());
        }
        return temp;
    }

    protected void getOpenID(User user) throws IOException {
        String temp = getPersonPrompt("open id", user.getOpenID());
        if (!isEmpty(temp)) {
            user.setOpenID(new OpenID(temp));
        } else {
            user.setOpenID(null);
        }
    }

    protected void getOpenIDConnect(User user) throws IOException {
        String temp = getPersonPrompt("open id connect", user.getOpenIDConnect());
        if (!isEmpty(temp)) {
            user.setOpenIDConnect(new OpenIDConnect(temp));
        } else {
            user.setOpenIDConnect(null);
        }
    }

    protected void getEPTID(User user) throws IOException {
        String temp = getPersonPrompt("eptid", user.getePTID());
        if (!isEmpty(temp)) {
            user.setePTID(new EduPersonTargetedID(temp));
        } else {
            user.setePTID(null);
        }
    }

    protected void getEPPN(User user) throws IOException {
        String temp = getPersonPrompt("eppn", user.getePPN());
        if (!isEmpty(temp)) {
            user.setePPN(new EduPersonPrincipleName(temp));
        } else {
            user.setePPN(null);
        }
    }

    protected void getRemoteUser(User user) throws IOException {
        String temp = getPersonPrompt("remote user", user.getRemoteUser());
        if (!isEmpty(temp)) {
            user.setRemoteUser(new RemoteUserName(temp));
        } else {
            user.setRemoteUser(null);
        }
    }

    @Override
    protected int longFormat(Identifiable identifiable, boolean isVerbose) {
        int width = super.longFormat(identifiable, isVerbose);
        int realWidth = width - indentWidth();
        User user = (User) identifiable;
        try {
            if (user.canGetCert()) {
                say(formatLongLine("DN", user.getDN(null, false), realWidth, isVerbose));
            }
        } catch (Throwable t) {
            say(formatLongLine("DN", "(could not compute DN)", realWidth, isVerbose));
        }
        return width;
    }

    @Override
    public void rm(InputLine inputLine) throws Throwable {
        if (showHelp(inputLine)) {
            showRMHelp();
            return;
        }
        List<Identifiable> x = findItem(inputLine);
        if (x == null || x.isEmpty()) {
            say("No users found");
            return;
        }
        boolean archiveUsers = isOk(readline("Archive user record before removing it? [y/n]:"));
        for (Identifiable id : x) {
            if (archiveUsers) {
                Identifier auId = getArchivedUserStore().archiveUser(id.getIdentifier());
            }
            getStore().remove(id.getIdentifier());
        }
        say("Done. " + x.size() + " users have been removed from the store");
        clearEntries();
    }

    // CIL-1310
    public void dn(InputLine inputLine) throws Throwable {
        if (showHelp(inputLine)) {
            say("dn [-email] index");
            say("Compute the DN for the current user");
            say(" -email - return the email for the user");
            return;
        }
        List<Identifiable> x = findItem(inputLine);
        if (x == null) {
            say("no such user");
            return;
        }

        for (Identifiable id : x) {
            say(DNUtil.getDN((User) id, null, inputLine.hasArg("-email")));
        }
    }


    @Override
    protected void initHelp() throws Throwable {
        super.initHelp();
        getHelpUtil().load("/help/user_help.xml");
    }

    @Override
    public void change_id(InputLine inputLine) throws Throwable {
        // Fix for https://github.com/ncsa/oa4mp/issues/243 make sure they understand the full ramifications!
        if(!"y".equals(getInput("This will also update the user uid for all pending transactions. Proceed(y/n)?", "n"))){
            return;
        }
        super.change_id(inputLine);
    }

    /**
     * This will update any current transactions to the new user id.
     * @param updatedUser
     * @param newID
     * @param updatePermissions
     * @return
     */
    // Fallout from https://github.com/ncsa/oa4mp/issues/243
    @Override
    public ChangeIDRecord doChangeID(Identifiable updatedUser, Identifier newID, boolean updatePermissions) {
        ChangeIDRecord changeIDRecord = super.doChangeID(updatedUser, newID, updatePermissions);
        CILOA2TransactionKeys keys = new CILOA2TransactionKeys();
        CILogonOA2ServiceEnvironment cilSE = (CILogonOA2ServiceEnvironment) getEnvironment();
        List<ServiceTransaction> transactions = cilSE.getTransactionStore().search(keys.userUID(), changeIDRecord.oldID.toString(), false );
        for(ServiceTransaction transaction : transactions) {
            if(transaction instanceof CILOA2ServiceTransaction){
                CILOA2ServiceTransaction stx = (CILOA2ServiceTransaction) transaction;
                stx.setUserUID(newID);
                getEnvironment().getTransactionStore().save(stx);
            }
        }
        changeIDRecord.updateCount = changeIDRecord.updateCount + transactions.size();
        // Now for archived users.
        UserKeys userKeys = new UserKeys();
        List<ArchivedUser> aUsers = cilSE.getArchivedUserStore().search(userKeys.userID(), changeIDRecord.oldID.toString(), false );
        for(ArchivedUser archivedUser : aUsers) {
            archivedUser.setUser((User) updatedUser);
            cilSE.getArchivedUserStore().save(archivedUser);
        }
        changeIDRecord.updateCount = changeIDRecord.updateCount + aUsers.size();
        return changeIDRecord;
    }

    @Override
    protected int updateStorePermissions(Identifier newID, Identifier oldID, boolean copy) {
        return 0;
    }
}