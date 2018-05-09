package org.intellij.plugins.xsltDebugger;

import javax.annotation.Nonnull;

import org.intellij.lang.xpath.xslt.run.XsltCommandLineState;
import org.intellij.lang.xpath.xslt.run.XsltRunConfiguration;
import org.intellij.plugins.xsltDebugger.impl.XsltDebugProcess;
import org.jetbrains.annotations.NonNls;
import consulo.annotations.RequiredDispatchThread;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.DefaultProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;

/*
* Created by IntelliJ IDEA.
* User: sweinreuter
* Date: 16.11.10
*/
public class XsltDebuggerRunner extends DefaultProgramRunner
{
	static final ThreadLocal<Boolean> ACTIVE = new ThreadLocal<Boolean>();

	@NonNls
	private static final String ID = "XsltDebuggerRunner";


	@Nonnull
	@Override
	public String getRunnerId()
	{
		return ID;
	}

	@Override
	public boolean canRun(@Nonnull String executorId, @Nonnull RunProfile profile)
	{
		return executorId.equals("Debug") && profile instanceof XsltRunConfiguration;
	}

	@Override
	@RequiredDispatchThread
	protected RunContentDescriptor doExecute(@Nonnull RunProfileState state, @Nonnull ExecutionEnvironment env) throws ExecutionException
	{
		FileDocumentManager.getInstance().saveAllDocuments();
		return createContentDescriptor(state, env);
	}

	protected RunContentDescriptor createContentDescriptor(final RunProfileState runProfileState, final ExecutionEnvironment executionEnvironment) throws ExecutionException
	{
		Project project = executionEnvironment.getProject();
		final XDebugSession debugSession = XDebuggerManager.getInstance(project).startSession(executionEnvironment, new XDebugProcessStarter()
		{
			@Override
			@Nonnull
			public XDebugProcess start(@Nonnull final XDebugSession session) throws ExecutionException
			{
				ACTIVE.set(Boolean.TRUE);
				try
				{
					final XsltCommandLineState c = (XsltCommandLineState) runProfileState;
					final ExecutionResult result = runProfileState.execute(executionEnvironment.getExecutor(), XsltDebuggerRunner.this);
					return new XsltDebugProcess(session, result, c.getExtensionData().getUserData(XsltDebuggerExtension.VERSION));
				}
				finally
				{
					ACTIVE.remove();
				}
			}
		});
		return debugSession.getRunContentDescriptor();
	}
}