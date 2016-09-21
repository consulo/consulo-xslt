package consulo.xslt.xpath.context;

import org.intellij.lang.xpath.context.NamespaceContext;
import org.intellij.lang.xpath.xslt.context.XsltNamespaceContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.psi.xml.XmlElement;
import consulo.xpath.context.NamespaceContextProvider;

/**
 * @author VISTALL
 * @since 21-Sep-16
 */
public class XsltNamespaceContextProvider implements NamespaceContextProvider
{
	@Nullable
	@Override
	public NamespaceContext getNamespaceContext(@NotNull XmlElement xmlElement)
	{
		return XsltNamespaceContext.NAMESPACE_CONTEXT;
	}
}
