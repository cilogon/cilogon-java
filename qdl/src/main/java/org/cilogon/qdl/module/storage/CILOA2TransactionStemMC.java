package org.cilogon.qdl.module.storage;

import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import edu.uiuc.ncsa.security.core.util.StringUtils;
import edu.uiuc.ncsa.security.storage.data.MapConverter;
import org.cilogon.oauth2.servlet.storage.transaction.CILOA2ServiceTransaction;
import org.cilogon.oauth2.servlet.storage.transaction.CILOA2TransactionKeys;
import org.oa4mp.delegation.server.storage.ClientStore;
import org.oa4mp.server.loader.oauth2.storage.transactions.OA2ServiceTransaction;
import org.qdl_lang.variables.QDLStem;
import org.oa4mp.server.qdl.storage.TransactionStemMC;
/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/11/22 at  1:10 PM
 */
public class CILOA2TransactionStemMC extends TransactionStemMC {
    public CILOA2TransactionStemMC(MapConverter mapConverter, ClientStore clientStore) {
        super(mapConverter, clientStore);
    }
      protected CILOA2TransactionKeys getK(){
        return (CILOA2TransactionKeys) getKeys();
      }
    @Override
    public OA2ServiceTransaction fromMap(QDLStem stem, OA2ServiceTransaction oa2ServiceTransaction) {
        CILOA2ServiceTransaction t = (CILOA2ServiceTransaction) super.fromMap(stem, oa2ServiceTransaction);
        if(stem.containsKey(getK().affiliation())){t.setAffiliation(stem.getString(getK().affiliation()));               }
        if(stem.containsKey(getK().displayName())){t.setDisplayName(stem.getString(getK().displayName()));              }
        if(stem.containsKey(getK().LOA())){t.setLoa(stem.getString(getK().LOA()));                                     }
        if(stem.containsKey(getK().organizationalUnit())){t.setOrganizationalUnit(stem.getString(getK().organizationalUnit()));}
        if(stem.containsKey(getK().userUID())){t.setUserUID(BasicIdentifier.newID(stem.getString(getK().userUID()))); }
        return t;
    }

    @Override
    public QDLStem toMap(OA2ServiceTransaction oa2ServiceTransaction, QDLStem stem) {
        CILOA2ServiceTransaction t = (CILOA2ServiceTransaction)oa2ServiceTransaction;
        stem = super.toMap(t, stem);
        if(!StringUtils.isTrivial(t.getAffiliation())){stem.put(getK().affiliation(), t.getAffiliation());}
        if(!StringUtils.isTrivial(t.getDisplayName())){stem.put(getK().displayName(), t.getDisplayName());}
        if(!StringUtils.isTrivial(t.getLoa())){stem.put(getK().LOA(), t.getLoa());}
        if(!StringUtils.isTrivial(t.getOrganizationalUnit())){stem.put(getK().organizationalUnit(), t.getOrganizationalUnit());}
        if(t.getUserUID()!=null){stem.put(getK().userUID(), t.getUserUID().toString());}
        return stem;
    }
}
