package org.cilogon.d2.storage;

import edu.uiuc.ncsa.security.storage.data.SerializationKeys;

/**  This is always used in conjunction with {@link org.cilogon.d2.util.UserKeys}
 *  This supplies the keys specific to archived users only -- a unique key and timestamp.
* <p>Created by Jeff Gaynor<br>
* on 4/26/12 at  10:04 AM
*/
public class ArchivedUserKeys extends SerializationKeys {
    public ArchivedUserKeys() {
        identifier(archivedUserIDColumn);
    }

    String archivedUserIDColumn = "archived_user_id";

    String archivedTimestampColumn = "archive_time";


    public String archivedUserIDColumn(String... x) {
        if (0 < x.length) archivedUserIDColumn = x[0];
        return archivedUserIDColumn;
    }

    public String archivedTimestampColumn(String... x) {
        if (0 < x.length) archivedTimestampColumn = x[0];
        return archivedTimestampColumn;
    }

}
