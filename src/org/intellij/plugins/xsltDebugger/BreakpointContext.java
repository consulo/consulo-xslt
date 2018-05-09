package org.intellij.plugins.xsltDebugger;

import javax.annotation.Nonnull;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import org.intellij.lang.xpath.XPathFile;
import org.intellij.lang.xpath.context.VariableContext;
import org.intellij.lang.xpath.context.XPathQuickFixFactoryImpl;
import org.intellij.lang.xpath.psi.XPathElement;
import org.intellij.lang.xpath.psi.XPathVariableReference;
import org.intellij.lang.xpath.validation.inspections.quickfix.XPathQuickFixFactory;
import org.intellij.lang.xpath.xslt.context.XsltContextProvider;
import org.intellij.lang.xpath.xslt.context.XsltVariableContext;

import javax.annotation.Nullable;

public class BreakpointContext extends XsltContextProvider {
  public BreakpointContext(PsiElement contextElement) {
    super((XmlElement)contextElement);
  }

  @Override
  @Nonnull
  public XPathQuickFixFactory getQuickFixFactory() {
    return XPathQuickFixFactoryImpl.INSTANCE;
  }

  @Override
  public PsiFile[] getRelatedFiles(XPathFile file) {
    return PsiFile.EMPTY_ARRAY;
  }

  @Override
  @Nonnull
  public VariableContext getVariableContext() {
    return new XsltVariableContext() {
      @Override
      @Nullable
      protected XmlTag getContextTagImpl(XPathElement element) {
        return PsiTreeUtil.getParentOfType(getContextElement(), XmlTag.class, false);
      }

      @Override
      @Nonnull
      public IntentionAction[] getUnresolvedVariableFixes(XPathVariableReference reference) {
        return IntentionAction.EMPTY_ARRAY;
      }
    };
  }
}
