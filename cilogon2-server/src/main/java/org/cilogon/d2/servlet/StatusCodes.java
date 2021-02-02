package org.cilogon.d2.servlet;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/28/21 at  6:56 AM
 */
public class StatusCodes {
    /*
    Success codes
     */
    public static final int STATUS_OK = 0x0; // 0
    public static final int STATUS_ACTION_NOT_FOUND = 0x1; //1
    public static final int STATUS_NEW_USER = 0x2; //2
    public static final int STATUS_USER_SERIAL_STRING_UPDATED = 0x4; //4
    public static final int STATUS_USER_NOT_FOUND = 0x6;  //6
    public static final int STATUS_USER_EXISTS = 0x8; //8
    /*
    error codes
     */
    public static final int STATUS_USER_EXISTS_ERROR = 0xFFFA1; //1048481
    public static final int STATUS_USER_NOT_FOUND_ERROR = 0xFFFA3; // 1048483
    public static final int STATUS_TRANSACTION_NOT_FOUND = 0xFFFA5; //1048485
    public static final int STATUS_IDP_SAVE_FAILED = 0xFFFA7; // 1048487
    public static final int STATUS_DUPLICATE_ARGUMENT = 0xFFFF1; // 1048561
    public static final int STATUS_INTERNAL_ERROR = 0xFFFF3; // 1048563 was "database failure"
    public static final int STATUS_SAVE_IDP_FAILED = 0xFFFF5; // 1048565
    public static final int STATUS_MALFORMED_INPUT = 0xFFFF7; // 1048567
    public static final int STATUS_MISSING_ARGUMENT = 0xFFFF9; // 1048569
    public static final int STATUS_NO_REMOTE_USER = 0xFFFFB; // 1048571
    public static final int STATUS_NO_IDENTITY_PROVIDER = 0xFFFFD; // 1048573
    public static final int STATUS_CLIENT_NOT_FOUND = 0xFFFFF; // 1048575
    public static final int STATUS_EPTID_MISMATCH = 0x100001; // 1048577
    public static final int STATUS_PAIRWISE_ID_MISMATCH = 0x100003; // 1048579
    public static final int STATUS_SUBJECT_ID_MISMATCH = 0x100005; // 1048581

    public static String getMessage(int status){
        switch(status){
            case STATUS_OK:
                return "ok";
            case STATUS_ACTION_NOT_FOUND: //1
            return  "action not found";
            case STATUS_NEW_USER: //2
                return "new user ok";
            case STATUS_USER_SERIAL_STRING_UPDATED: //4
                return  "serial string update ok";
            case STATUS_USER_NOT_FOUND:  //6
                return  "user not found";
            case STATUS_USER_EXISTS: //8
                return  "user already exists"; // informational message
            case STATUS_USER_EXISTS_ERROR: //1048481
                return "user already exists"; // actual error that the the user should not exist.
            case STATUS_USER_NOT_FOUND_ERROR: // 1048483
                return "user not found";
            case STATUS_TRANSACTION_NOT_FOUND: //1048485
                return  "transaction not found";
            case STATUS_IDP_SAVE_FAILED: // 1048487
                return  "idp save failed";
            case STATUS_DUPLICATE_ARGUMENT: // 1048561
                return "duplicate argument";
            case STATUS_INTERNAL_ERROR: // 1048563 was "database failure"
                return  "internal error";
            case STATUS_SAVE_IDP_FAILED: // 1048565
                return  "saving idp failed";
            case STATUS_MALFORMED_INPUT : // 1048567
                return "malformed input";
            case STATUS_MISSING_ARGUMENT: // 1048569
                return  "missing argument";
            case STATUS_NO_REMOTE_USER: // 1048571
                return "no remote user";
            case STATUS_NO_IDENTITY_PROVIDER: // 1048573
                return "no identity provider";
            case STATUS_CLIENT_NOT_FOUND: // 1048575
                return "client not found";
            case STATUS_EPTID_MISMATCH: // 1048577
                return "EPTID mismatch";
            case STATUS_PAIRWISE_ID_MISMATCH: // 1048579
                return "pairwise id mismatch";
            case STATUS_SUBJECT_ID_MISMATCH: // 1048581
                return "subject id mismatch";
            default:
                return "unknown error 0x" + Integer.toHexString(status) + " = " + status;
        }


    }
}
