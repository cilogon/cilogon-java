package test.cilogon;

import edu.uiuc.ncsa.myproxy.oa4mp.TestUtils;
import edu.uiuc.ncsa.security.core.util.DateUtils;
import edu.uiuc.ncsa.security.core.util.DoubleHashMap;
import org.cilogon.oauth2.servlet.storage.user.User;
import org.cilogon.oauth2.servlet.util.SerialStrings;

import java.net.URI;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/8/12 at  11:09 AM
 */
public abstract class ServiceTestUtils extends TestUtils {

    public static SerialStrings getSerialStrings() {
        if (serialStrings == null) {
            DoubleHashMap<URI, String> prefixMap = new DoubleHashMap<URI, String>();
            prefixMap.put(URI.create("http://cilogon.org/serverQ/users/"), "Q");
            prefixMap.put(URI.create("http://cilogon.org/serverT/users/"), "T");
            prefixMap.put(URI.create("http://cilogon.org/serverA/users/"), "A");
            prefixMap.put(URI.create("http://cilogon.org/serverB/users/"), "B");
            serialStrings = new SerialStrings(prefixMap);
        }
        return serialStrings;
    }

    static SerialStrings serialStrings;

    public static boolean checkTimestamp(User user, User oldUser) {
        return DateUtils.compareDates(user.getCreationTS(), oldUser.getCreationTS());
    }
}
