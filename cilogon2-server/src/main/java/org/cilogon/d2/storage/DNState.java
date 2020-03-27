package org.cilogon.d2.storage;

import edu.uiuc.ncsa.security.core.util.BitSetUtil;
import net.sf.json.JSONArray;

import java.util.BitSet;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/20/20 at  11:54 AM
 */
public class DNState {

    BitSet bitSet;

    public static int DN_STATE_LENGTH = 5;
    public static int DN_STATE_NULL = 0;
    public int DN_STATE_SET = 1;
    public int DN_STATE_CHANGED = 2;
    // If any of these indices ever change, you will break this! If you need to add new state to tract, just
    // add to the bit vector.
    public static int DN_STATE_EMAIL_INDEX = 0;
    public static int DN_STATE_IDP_NAME = 1;
    public static int DN_STATE_DISPLAY_NAME_INDEX = 2;
    public static int DN_STATE_FIRST_NAME_INDEX = 3;
    public static int DN_STATE_LAST_NAME_INDEX = 4;
    JSONArray array;
    /**
     * If the email bit only is set
     */
    int email = 1;
    /**
     * If the IDP Display name bit only is set
     */
    int idpName = 2;
    /**
     * If the user display name only is set
     */
    int dName = 4;
    /**
     * If the first and last name only are set
     */
    int flName = 24;
    /**
     * Valid DNState to compute FN if email, idpName and user display name are set.
     */
    public int valid_dName = email + idpName + dName;
    /**
     * Valid DNState to compute FN if email, idpName first name and last name are set.
     */

    public int valid_flName = email + idpName + flName;
    public int valid_All = email + idpName + flName + dName;

    public DNState() {
        bitSet = new BitSet();
    }

    public DNState(int value) {
        setStateValue(value);
    }


    public boolean allZero() {
        return bitSet.cardinality() == 0;
    }

    public boolean isUpdated() {
        return updated;
    }

    boolean updated = false;

    /**
     * This determines if the updated state and this state require a new serial string be issued.
     * This DN State is what is stored for the user. The argument is created from checking the updates to the user.
     *
     * @param updatedState
     * @return
     */
    public boolean keepSerialString(DNState updatedState) {
        BitSet thisBS = bitSet;
        BitSet updatedBS = updatedState.bitSet;
        // case 1: The user can already make a  serial string
        if (canGetDN()) {
            // So we can already compute the DN. Check if the updates are to any required fields.
            updatedBS.and(thisBS); // have to compute it and stash it in the argument so we DON'T change the stored state
            return !BitSetUtil.orCompress(updatedBS); // or == true applied here means something changed.
        }
        // case 2: Together, the new and old allow the user to finally get a DN
        DNState scratch = new DNState(getStateValue());
        BitSet scratchBS = scratch.bitSet;
        scratchBS.and(updatedBS);
        // be sure that we give preference to the first last name in case we have all names
        //
        if (scratch.canGetDN()) {
            this.bitSet = scratchBS; // this effectively means we are going to set this in the store
            updated = true;
            // OK so the FIRST time they can get a serial string does not require changing it. It just means return what is there.
            return true;
        } else {
            if (scratchBS.get(DN_STATE_FIRST_NAME_INDEX) && scratchBS.get(DN_STATE_LAST_NAME_INDEX)) {
                scratch.setIt(DN_STATE_DISPLAY_NAME_INDEX, false);
                if (scratch.canGetDN()) { // so all the names were sent. We will store this and use first and last
                    this.bitSet = scratchBS;
                    updated = true;
                    return true;
                }
            }

            // have to check that too much info was not sent.
            if (scratchBS.get(DN_STATE_DISPLAY_NAME_INDEX)) {
                // so user display name was sent along with exactly one of these. Get rid of both flags so only display name is used.
                scratch.setIt(DN_STATE_FIRST_NAME_INDEX, false);
                scratch.setIt(DN_STATE_LAST_NAME_INDEX, false);
                if (scratch.canGetDN()) {
                    this.bitSet = scratchBS;
                    updated = true;
                    return true;

                }
            }
        }
        return true; // fall through case. Change nothing!

    }

    public int getStateValue() {
        if(bitSet.isEmpty()) return 0;
        // vector of [M,I, D, F,L]
        return BitSetUtil.toInt(bitSet);
    }

    /**
     * Set the value of this to the give integer
     *
     * @param v
     */
    public void setStateValue(int v) {
        bitSet = BitSet.valueOf(new long[]{v});
    }

    protected void setIt(int index, boolean ok) {
        bitSet.set(index, ok);
    }

    public void setIDPName(boolean ok) {
        setIt(DN_STATE_IDP_NAME, ok);
    }
    public boolean hasIDPName(){return bitSet.get(DN_STATE_IDP_NAME);}

    public void setEmail(boolean ok) {
        setIt(DN_STATE_EMAIL_INDEX, ok);
    }
    public boolean hasEmail(){return bitSet.get(DN_STATE_EMAIL_INDEX);}

    public void setDisplayName(boolean ok) {
        setIt(DN_STATE_DISPLAY_NAME_INDEX, ok);
    }
    public boolean hasDisplayName(){return bitSet.get(DN_STATE_DISPLAY_NAME_INDEX);}

    public void setFirstName(boolean ok) {
        setIt(DN_STATE_FIRST_NAME_INDEX, ok);
    }
    public boolean hasFirstName(){return bitSet.get(DN_STATE_FIRST_NAME_INDEX);}

    public void setLastName(boolean ok) {
        setIt(DN_STATE_LAST_NAME_INDEX, ok);
    }
    public boolean hasLastName(){return bitSet.get(DN_STATE_LAST_NAME_INDEX);}

    public String[] getDNNames(User user) {
        if (getStateValue() == valid_dName) {
            return new String[]{user.getDisplayName()};
        }
        if (getStateValue() == valid_flName) {
            return new String[]{user.getFirstName(), user.getLastName()};
        }
        throw new IllegalStateException("Error: Could not determine which user names to return");
    }

    public boolean canGetDN() {
        // correspond to [1,0,1,1] and [1,1,0,0] resp.
        if(getStateValue() == valid_All){
            throw new IllegalStateException("Error: Cannot have all values set at once.");
        }
        return getStateValue() == valid_flName || getStateValue() == valid_dName ;
    }

}
