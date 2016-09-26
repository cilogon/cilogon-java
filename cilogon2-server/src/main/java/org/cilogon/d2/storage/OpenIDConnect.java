package org.cilogon.d2.storage;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 5/23/14 at  4:05 PM
 */
public class OpenIDConnect extends PersonName {
    public OpenIDConnect(String name) {
        super(name);
    }

    @Override
      public boolean equals(Object obj) {
          if(!(obj instanceof OpenIDConnect)) return false;
          return super.equals(obj);
      }
}
