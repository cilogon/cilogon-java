package org.cilogon.qdl.workspace;

import org.cilogon.qdl.module.CILLibLoader;
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
        CILogonQDLWorkspace workspace = (CILogonQDLWorkspace) init(args);
        // Fix https://github.com/cilogon/cilogon-java/issues/47
        CILLibLoader cilLibLoader = new CILLibLoader();
        cilLibLoader.add(workspace.getWorkspaceCommands().getState());
        if (workspace != null) {
            workspace.mainLoop();
        }
    }
}
