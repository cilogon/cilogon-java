package org.cilogon.d2.util;

import edu.uiuc.ncsa.security.core.exceptions.GeneralException;
import edu.uiuc.ncsa.security.core.util.DebugUtil;
import org.cilogon.d2.storage.User;

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
        return computeFNAL;
    }

    public static void setComputeFNAL(boolean computeFNAL) {
        DNUtil.computeFNAL = computeFNAL;
    }

    protected static boolean computeFNAL = false;


    protected static int getCase(User user) {
        if (user.hasOpenID() || user.hasOpenIDConnect())
            return OPENID_CASE;

        // Fix for CIL-172. Supports multiple LIGO IDPs.
        if (user.getIdP() != null && user.getIdP().matches(LIGO_IDP)) return LIGO_CASE;
        if (user.getEmail().toLowerCase().endsWith("fnal.gov")) return FNL_CASE;
        return DEFAULT_CASE;
    }

    public static String getDN(User user, CILServiceTransactionInterface transaction) {
        switch (getCase(user)) {
            case LIGO_CASE:
                return getLIGODN(user);
            case FNL_CASE:
                if (isComputeFNAL()) {
                    return getFNLDN(user);
                }
                // If not enabled, FNAL fall through to default case;
            case OPENID_CASE:
            case DEFAULT_CASE:
            default:
                return getDefaultDN(user, transaction);
        }
    }

    protected static String getLIGODN(User user) {
        String baseString = "/DC=org/DC=cilogon/C=US/O=LIGO/CN=%s %s %s email=%s";
        return String.format(baseString,
                user.getFirstName(),
                user.getLastName(),
                user.getRemoteUser().getName(),
                user.getEmail());
    }

    protected static String getDefaultDN(User user, CILServiceTransactionInterface transaction) {
        String c_us = "/C=US"; // country string
        boolean useCUS = true;
        if (transaction != null) {
            DebugUtil.dbg(DNUtil.class,"getDefaultDN:LOA=" + transaction.getLoa());
            DebugUtil.dbg(DNUtil.class,"getDefaultDN:user is Use US in DN?=" + user.isUseUSinDN());
            // Command line utilities will send along a null transaction since there is
            // no pending request. Best we can do is assume they are in the US for now.
            useCUS = user.isUseUSinDN();
        }
        DebugUtil.dbg(DNUtil.class, "getDefaultDN:Final useCUS = " + useCUS);
        String baseString = "/DC=org/DC=cilogon" + (useCUS ? "/C=US" : "") + "/O=%s/CN=%s %s %s email=%s";
        return String.format(baseString,
                user.getIDPName(),
                user.getFirstName(),
                user.getLastName(),
                user.getSerialString(),
                user.getEmail());
    }

    protected static final int FNL_CASE = 12;


    protected static String getFNLDN(User user) {
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
        String id = "UID:" + eppn.substring(0, eppn.indexOf("@"));
        String rc = null;
        if (cns[0].equals("People")) {
            String baseString = "/DC=org/DC=cilogon/C=US/O=Fermi National Accelerator Laboratory/OU=People/CN=%s %s/CN=%s email=%s";
            rc = String.format(baseString,
                    user.getFirstName(),
                    user.getLastName(),
                    id,
                    user.getEmail());
            DebugUtil.dbg(DNUtil.class, "OA2DNUtil.getFNLDN: people case=" + rc);
            return rc;
        }

        if (cns.length == 3 && cns[0].equals("Robots")) {
            String baseString = "/DC=org/DC=cilogon/C=US/O=Fermi National Accelerator Laboratory/OU=Robots/CN=%s/CN=%s/CN=%s %s/CN=%s email=%s";
            rc = String.format(baseString,
                    cns[1],
                    cns[2],
                    user.getFirstName(),
                    user.getLastName(),
                    id,
                    user.getEmail());
            DebugUtil.dbg(DNUtil.class, "OA2DNUtil.getFNLDN: robot case=" + rc);
            return rc;

        }
        throw new GeneralException("Could not determine type of organizational unit from " + user.getOrganizationalUnit());
    }
}
