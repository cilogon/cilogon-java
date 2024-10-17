package org.cilogon.qdl.workspace;

import edu.uiuc.ncsa.security.util.cli.IOInterface;
import org.cilogon.qdl.module.CILLibLoader;
import org.oa4mp.server.qdl.OA4MPQDLWorkspaceCommands;
import org.qdl_lang.state.LibLoader;
import org.qdl_lang.workspace.WorkspaceCommands;

import java.util.ArrayList;
import java.util.List;

public class CILogonQDLWorkspaceCommands extends OA4MPQDLWorkspaceCommands {
    public CILogonQDLWorkspaceCommands() {}

    public CILogonQDLWorkspaceCommands(IOInterface ioInterface) {
        super(ioInterface);
    }

    @Override
    public WorkspaceCommands newInstance() {
        return new CILogonQDLWorkspaceCommands();
    }

    @Override
    public WorkspaceCommands newInstance(IOInterface ioInterface) {
        return new CILogonQDLWorkspaceCommands(ioInterface);
    }
    @Override
    public List<LibLoader> getLibLoaders() {
        if(loaders == null) {
            loaders = new ArrayList<>();
            // Fix https://github.com/cilogon/cilogon-java/issues/47
            loaders.add(new CILLibLoader());
        }
        return loaders;
    }
}
