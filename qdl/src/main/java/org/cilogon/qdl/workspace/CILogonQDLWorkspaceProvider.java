package org.cilogon.qdl.workspace;

import org.oa4mp.server.qdl.OA2LibLoader2;
import org.oa4mp.server.qdl.OA4MPQDLWorkspace;
import org.oa4mp.server.qdl.OA4MPQDLWorkspaceCommands;
import org.qdl_lang.workspace.QDLWorkspace;
import org.qdl_lang.workspace.WorkspaceCommands;
import org.qdl_lang.workspace.WorkspaceProvider;

public class CILogonQDLWorkspaceProvider implements WorkspaceProvider {
    @Override
    public QDLWorkspace getWorkspace(WorkspaceCommands workspaceCommands) {
        return new CILogonQDLWorkspace(workspaceCommands);
    }
}
