package org.cilogon.oauth2.servlet.storage.sequence;

import org.oa4mp.server.api.OA4MPConfigTags;

import javax.inject.Provider;

public interface SerialStringProviderInterface<T extends SerialStrings> extends Provider<T>, OA4MPConfigTags {
    @Override
    T get();
}
