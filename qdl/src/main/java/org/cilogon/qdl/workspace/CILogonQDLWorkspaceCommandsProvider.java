package org.cilogon.qdl.workspace;

import edu.uiuc.ncsa.security.util.cli.IOInterface;
import org.oa4mp.server.qdl.OA4MPQDLWorkspaceCommands;
import org.qdl_lang.workspace.WorkspaceCommands;
import org.qdl_lang.workspace.WorkspaceCommandsProvider;

public class CILogonQDLWorkspaceCommandsProvider extends WorkspaceCommandsProvider {
    public CILogonQDLWorkspaceCommandsProvider() {}
    @Override
    public WorkspaceCommands get() {
        return new CILogonQDLWorkspaceCommands();
    }

    @Override
    public WorkspaceCommands get(IOInterface ioInterface) {
        return new CILogonQDLWorkspaceCommands(ioInterface);
    }
}
