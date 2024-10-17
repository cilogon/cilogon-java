package org.cilogon.qdl.workspace;

import org.qdl_lang.workspace.QDLWorkspace;
import org.qdl_lang.workspace.WorkspaceCommands;
import org.qdl_lang.workspace.WorkspaceProvider;

public class CILogonQDLWorkspace extends QDLWorkspace{
    public CILogonQDLWorkspace(WorkspaceCommands workspaceCommands) {
        super(workspaceCommands);
    }
    public static void main(String[] args) throws Throwable {
// Fixes https://github.com/cilogon/cilogon-java/issues/47
        WorkspaceProvider workspaceProvider = new CILogonQDLWorkspaceProvider();
        QDLWorkspace.setWorkspaceProvider(workspaceProvider);
        WorkspaceCommands.setWorkspaceCommandsProvider(new CILogonQDLWorkspaceCommandsProvider());
        CILogonQDLWorkspace workspace = (CILogonQDLWorkspace) init(args);
        if (workspace != null) {
            workspace.mainLoop();
        }
    }
}
