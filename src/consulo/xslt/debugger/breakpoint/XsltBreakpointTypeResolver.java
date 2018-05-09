package consulo.xslt.debugger.breakpoint;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.intellij.lang.xpath.xslt.XsltSupport;
import org.intellij.plugins.xsltDebugger.XsltBreakpointType;
import consulo.annotations.RequiredReadAction;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import consulo.xdebugger.breakpoints.XLineBreakpointTypeResolver;

/**
 * @author VISTALL
 * @since 5/8/2016
 */
public class XsltBreakpointTypeResolver implements XLineBreakpointTypeResolver
{
	@Nullable
	@Override
	@RequiredReadAction
	public XLineBreakpointType<?> resolveBreakpointType(@Nonnull Project project, @Nonnull VirtualFile virtualFile, int line)
	{
		final Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
		if(document == null)
		{
			return null;
		}

		final PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
		if(psiFile == null)
		{
			return null;
		}
		final FileType fileType = psiFile.getFileType();
		if(fileType != XmlFileType.INSTANCE || !XsltSupport.isXsltFile(psiFile))
		{
			return null;
		}
		return XsltBreakpointType.getInstance();
	}
}
