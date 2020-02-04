package org.cilogon.d2.util;

import edu.uiuc.ncsa.security.core.exceptions.GeneralException;
import edu.uiuc.ncsa.security.core.exceptions.NFWException;
import edu.uiuc.ncsa.security.core.util.DebugUtil;
import edu.uiuc.ncsa.security.servlet.ServletDebugUtil;
import net.freeutils.charset.UTF7Charset;
import org.cilogon.d2.storage.User;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetEncoder;
import java.util.StringTokenizer;

/**
 * A utility for computing distinguished names. Since we are accumulating a slew of these,
 * this organizes them and their logic.
 * <p>Created by Jeff Gaynor<br>
 * on 5/14/12 at  8:44 AM
 */
public class DNUtil {
    public final static String LIGO_IDP = "https://login\\d?.ligo.org/idp/shibboleth";
    protected static final int DEFAULT_CASE = 0;
    protected static final int LIGO_CASE = 10;
    protected static final int OPENID_CASE = 11;

    public static boolean isComputeFNAL() {
        ServletDebugUtil.dbg(DNUtil.class, "computeFNAL=" + computeFNAL);
        return computeFNAL;
    }

    public static void setComputeFNAL(boolean computeFNAL) {
        DNUtil.computeFNAL = computeFNAL;
    }

    protected static boolean computeFNAL = false;


    protected static int getCase(User user) {
        ServletDebugUtil.dbg(DNUtil.class, "in getCase");
        if (user.hasOpenID() || user.hasOpenIDConnect())
            return OPENID_CASE;

        // Fix for CIL-172. Supports multiple LIGO IDPs.
        if (user.getIdP() != null && user.getIdP().matches(LIGO_IDP)) return LIGO_CASE;
        // Fix for CIL-234
        if (user.getePPN() != null && user.getePPN().getName().toLowerCase().endsWith("fnal.gov")) return FNL_CASE;
        ServletDebugUtil.dbg(DNUtil.class, "returning default case");

        return DEFAULT_CASE;
    }

    public static String getDN(User user, AbstractCILServiceTransaction transaction, boolean returnEmail) {
        ServletDebugUtil.trace(DNUtil.class, "in getDN.");
        switch (getCase(user)) {
            case LIGO_CASE:
                return getLIGODN(user, returnEmail);
            case FNL_CASE:
                if (isComputeFNAL()) {
                    return getFNLDN(user, returnEmail);
                }
                // If not enabled, FNAL fall through to default case;
            case OPENID_CASE:
            case DEFAULT_CASE:
            default:
                return getDefaultDN(user, returnEmail);
        }
    }

    protected static String getLIGODN(User user, boolean returnEmail) {
        String baseString = "/DC=org/DC=cilogon/C=US/O=LIGO/CN=%s %s %s";
        String name = null;
        if (user.getRemoteUser() != null && !user.getRemoteUser().isEmpty()) {
            name = user.getRemoteUser().getName();
        }
        if (name == null && !user.getePPN().isEmpty()) {
            name = user.getePPN().getName();
        }
        if (name == null) {
            throw new NFWException("Error: LIGO user \"" + user.getIdentifierString() + "\" has neither the remote user nor EPPN set. Cannot create a DN");
        }
        if (user.getOrganizationalUnit() != null && !user.getOrganizationalUnit().isEmpty()) {
            // ONLY in this case is it possible that we have a robot DN
            // CIL-512 compute DN for robots too.
            StringTokenizer st = new StringTokenizer(user.getOrganizationalUnit(), ":");
            String[] cns = new String[st.countTokens()];
            int i = 0;
            while (st.hasMoreTokens()) {
                cns[i++] = st.nextToken();
                DebugUtil.dbg(DNUtil.class, "OA2DNUtil.getLIGODN: cns[i]==" + cns[i - 1]); // cause we incremented
            }

            if (cns.length == 3 && cns[0].equals("Robots")) {
                String eppn = user.getePPN().getName();
                String id = "UID:" + eppn.substring(0, eppn.indexOf("@"));

                  /*
                   /DC=org/DC=cilogon/C=US/O=LIGO/OU=Robots/CN=o3.ncsa.illinois.edu/CN=cron/CN=Jim Basney/CN=UID:jim.basney
                   */
                baseString = "/DC=org/DC=cilogon/C=US/O=LIGO/OU=Robots/CN=%s/CN=%s/CN=%s %s/CN=%s";
                if (returnEmail) {
                    baseString = baseString + " email=%s";
                    return String.format(baseString,
                            cns[1],
                            cns[2],
                            toUTF7(user.getFirstName()),
                            toUTF7(user.getLastName()),
                            id,
                            user.getEmail());
                }
                return String.format(baseString,
                        cns[1],
                        cns[2],
                        toUTF7(user.getFirstName()),
                                toUTF7(user.getLastName()),
                        id);
            }
        }

        // Note that unlike the FNAL case, there is no "people" field in the OU for LIGO generally. It is implicit.
        if (returnEmail) {
            baseString = baseString + " email=%s";
            return String.format(baseString,
                    toUTF7(user.getFirstName()),
                    toUTF7(user.getLastName()),
                    name,
                    user.getEmail());
        }
        return String.format(baseString,
                toUTF7(user.getFirstName()),
                toUTF7(user.getLastName()),
                name);
/*
        throw new GeneralException("Error: computing LIGO DN. Could not determine proper " +
                "format from organizational unit=\"" + user.getOrganizationalUnit() + "\"");
*/

    }

    // Fix for CIL-320: getting DN should not depend on transaction state.
    protected static String getDefaultDN(User user, boolean returnEmail) {
        String baseString = "/DC=org/DC=cilogon" + (user.isUseUSinDN() ? "/C=US" : "") + "/O=%s/CN=%s %s %s";

        if (returnEmail) {
            baseString = baseString + " email=%s";
            return String.format(baseString,
                    toUTF7(user.getIDPName()),
                    toUTF7(user.getFirstName()),
                    toUTF7(user.getLastName()),
                    user.getSerialString(),
                    user.getEmail());

        }
        return String.format(baseString,
                toUTF7(user.getIDPName()),
                toUTF7(user.getFirstName()),
                toUTF7(user.getLastName()),
                user.getSerialString());
    }

    protected static final int FNL_CASE = 12;

    /**
     * Converts a String to its UTF7 equivalent and returns that. E.g. A string like <br/><br/>
     * Шоста@和楽器.com
     * <br/><br/>
     * would be returned as
     * <br/><br/>
     * +BCgEPgRBBEIEMABAVIxpfVZo.com
     * @param inString
     * @return
     */
    protected static String toUTF7(String inString) {
        return oldUTF7(inString);
    }
    protected static String newUTF7(String inString) {
        try {
            UTF7Charset utf7 = new UTF7Charset();

            CharsetEncoder encoder = utf7.newEncoder();
            ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(inString));
            //byte[] rawBytes = inString.getBytes(utf7);
            byte[] converted = new byte[bbuf.limit()];
            System.arraycopy(bbuf.array(),0,converted,0,converted.length);
            System.out.println(bbuf.limit());
            String output =  new String(converted);
            // jcharset does not add a final "-" (which can be optional in certain cases according to RFC 2515) but we *always* expect one in DNs, so we add it in.
            if(!output.endsWith("-")){
                output = output + "-";
            }
            return output;
        }catch(CharacterCodingException cx){
            throw new GeneralException("Bad character encoding for UTF 7", cx);
        }
    }

    /**
     * Convert a string to UTF 7 if it has not been already converted. 
     * @param inString
     * @return
     */
    protected static String oldUTF7(String inString) {
        // UTF 7 string start with + and end with -.
        if(inString == null || inString.isEmpty()){
            throw new IllegalArgumentException("Error: no DN to convert to UTF-7");
        }
        inString = inString.trim();
        if(inString.endsWith("-") && inString.startsWith("+")){
            return inString;
        }
        UTF7Charset utf7 = new UTF7Charset();
        byte[] rawBytes = inString.getBytes(utf7);
        return new String(rawBytes);
    }

    public static void main(String[] args){
        //String input = "/DC=org/DC=cilogon/C=US/O=Google/CN=+MNUw6yAVMOowxjDqIBU- +MNUw6yAVMOo- A299626 email=boomerangfish@gmail.com";
        String input = "フル―リテリ―";
        String testString = "+MNUw6yAVMOowxjDqIBU-";
        System.out.println("original = \"" + input + "\"");
        String encodedToUTF7String =  toUTF7(input);
        System.out.println("to UTF 7 = \"" + encodedToUTF7String + "\"");
        System.out.println("is expected conversion correct? " + testString.equals(encodedToUTF7String));

    }
    protected static String getFNLDN(User user, boolean returnEmail) {
        DebugUtil.dbg(DNUtil.class, "OA2DNUtil.getFNLDN: user=" + user);
        if (user.getOrganizationalUnit() == null) {
            throw new GeneralException("Error: No organizational unit has been specified for this user. DN cannot be generated.");
        }

        StringTokenizer st = new StringTokenizer(user.getOrganizationalUnit(), ":");
        String[] cns = new String[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens()) {
            cns[i++] = st.nextToken();
            DebugUtil.dbg(DNUtil.class, "OA2DNUtil.getFNLDN: cns[i]==" + cns[i - 1]); // cause we incremented
        }
        String eppn = user.getePPN().getName();
        if(eppn == null || eppn.isEmpty()){
            DebugUtil.dbg(DNUtil.class, "OA2DNUtil.getFNLDN: Missing EPPN, cannot create the DN."); // cause we incremented
            throw new GeneralException("Error: Missing EPPN, cannot create the correct DN");
        }
        String id = "UID:" + eppn.substring(0, eppn.indexOf("@"));
        String rc = null;
        if (cns[0].equals("People")) {

            String baseString = "/DC=org/DC=cilogon/C=US/O=Fermi National Accelerator Laboratory/OU=People/CN=%s %s/CN=%s";
            if (returnEmail) {
                baseString = baseString + " email=%s";

                rc = String.format(baseString,
                        toUTF7(user.getFirstName()),
                        toUTF7(user.getLastName()),
                        id,
                        user.getEmail());
                DebugUtil.dbg(DNUtil.class, "OA2DNUtil.getFNLDN: people case=" + rc);
                return rc;
            }
            rc = String.format(baseString,
                    toUTF7(user.getFirstName()),
                    toUTF7(user.getLastName()),
                    id);
            DebugUtil.dbg(DNUtil.class, "OA2DNUtil.getFNLDN: people case=" + rc);
            return rc;


        }

        if (cns.length == 3 && cns[0].equals("Robots")) {
            String baseString = "/DC=org/DC=cilogon/C=US/O=Fermi National Accelerator Laboratory/OU=Robots/CN=%s/CN=%s/CN=%s %s/CN=%s";
            if (returnEmail) {

                // Fix for "robot bug" baseString was not concatenated, it was replaced.
                baseString = baseString + " email=%s";
                rc = String.format(baseString,
                        cns[1],
                        cns[2],
                        toUTF7(user.getFirstName()),
                        toUTF7(user.getLastName()),
                        id,
                        user.getEmail());
                DebugUtil.dbg(DNUtil.class, "OA2DNUtil.getFNLDN: robot case=" + rc);
                return rc;

            }
            rc = String.format(baseString,
                    cns[1],
                    cns[2],
                    toUTF7(user.getFirstName()),
                    toUTF7(user.getLastName()),
                    id);
            DebugUtil.dbg(DNUtil.class, "OA2DNUtil.getFNLDN: robot case=" + rc);
            return rc;

        }
        throw new GeneralException("Could not determine type of organizational unit from " + user.getOrganizationalUnit());
    }
}
