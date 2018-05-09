/*
 * Copyright 2005 Sascha Weinreuter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.intellij.lang.xpath.xslt.impl.references;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import org.intellij.lang.xpath.completion.NamespaceLookup;
import org.intellij.lang.xpath.xslt.psi.impl.ResolveUtil;
import org.intellij.lang.xpath.xslt.XsltSupport;
import org.intellij.lang.xpath.xslt.context.XsltNamespaceContext;
import org.intellij.lang.xpath.xslt.impl.XsltChecker;
import org.intellij.lang.xpath.xslt.psi.impl.ImplicitModeElement;
import org.intellij.lang.xpath.xslt.util.MatchTemplateMatcher;
import org.intellij.lang.xpath.xslt.util.QNameUtil;
import org.intellij.lang.xpath.xslt.util.XsltCodeInsightUtil;
import com.intellij.codeInsight.completion.CompletionInitializationContext;
import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;

class ModeReference extends SimpleAttributeReference implements PsiPolyVariantReference, EmptyResolveMessageProvider {
    private final boolean myIsDeclaration;
    private final ImplicitModeElement myImplicitModeElement;

    public ModeReference(XmlAttribute attribute, boolean isDeclaration) {
        super(attribute);
        if (isDeclaration) {
            myIsDeclaration = true;
        } else {
            final PsiFile file = attribute.getContainingFile();
            if (file != null && XsltSupport.getXsltLanguageLevel(file) == XsltChecker.LanguageLevel.V2) {
              final String value = attribute.getValue();
              myIsDeclaration = "#current".equals(value) || "#default".equals(value);
            } else {
                myIsDeclaration = false;
            }
        }
        myImplicitModeElement = new ImplicitModeElement(attribute);
    }

    @Override
    @Nonnull
    protected TextRange getTextRange() {
        return myImplicitModeElement.getModeRange();
    }

    @Nonnull
    public Object[] getVariants() {
        final PsiFile containingFile = myAttribute.getContainingFile();
        if (containingFile instanceof XmlFile && XsltSupport.isXsltFile(containingFile)) {
            final List<Object> l = new ArrayList<Object>();
            if (!myImplicitModeElement.hasPrefix()) {
              final Object[] prefixes = getPrefixCompletions(myAttribute);
              ContainerUtil.addAll(l, prefixes);
            }

            if (myImplicitModeElement.getQName() != null) {
              final PsiElement[] modes = ResolveUtil.collect(getMatcher().variantMatcher());
              ContainerUtil.addAll(l, modes);
            }
          return ArrayUtil.toObjectArray(l);
        }
        return ArrayUtil.EMPTY_OBJECT_ARRAY;
    }

    public boolean isSoft() {
        return myIsDeclaration;
    }

    @Override
    @Nullable
    public PsiElement resolveImpl() {
        if (myIsDeclaration) {
            return myImplicitModeElement;
        } else {
            final ResolveResult[] results = multiResolve(false);
            return results.length == 1 ? results[0].getElement() : null;
        }
    }

    @Nonnull
    public ResolveResult[] multiResolve(final boolean incompleteCode) {
        final PsiFile containingFile = myAttribute.getContainingFile();
        if (containingFile instanceof XmlFile && XsltSupport.isXsltFile(containingFile) && myImplicitModeElement.getQName() != null) {
          return PsiElementResolveResult.createResults(ResolveUtil.collect(getMatcher()));
        }
        return ResolveResult.EMPTY_ARRAY;
    }

    private MyModeMatcher getMatcher() {
        return new MyModeMatcher(myAttribute, myImplicitModeElement.getQName());
    }

    public static PsiReference[] create(XmlAttribute attribute, boolean isDeclaration) {
        final String value = attribute.getValue();
        if (value == null) {
            return PsiReference.EMPTY_ARRAY;
        }
        if (value.trim().indexOf(' ') != -1) {
            // TODO: fix for xslt 2.0
            return PsiReference.EMPTY_ARRAY;
        }
        return createImpl(attribute, isDeclaration, value);
    }

    private static PsiReference[] createImpl(XmlAttribute attribute, boolean isDeclaration, String value) {
        final int p = value.indexOf(':');
        if (p == -1) {
            return new PsiReference[]{ new ModeReference(attribute, isDeclaration) };
        } else if (p == value.length() - 1) {
            return new PsiReference[]{new MyPrefixReference(attribute)};
        } else {
            return new PsiReference[]{
                    new MyPrefixReference(attribute),
                    new ModeReference(attribute, isDeclaration)
            };
        }
    }

  static Object[] getPrefixCompletions(XmlAttribute attribute) {
    final ModeReference.MyModeMatcher matcher = new ModeReference.MyModeMatcher(attribute, QNameUtil.ANY);
    final PsiElement[] modes = ResolveUtil.collect(matcher);
    final Collection<String> prefixes = XsltNamespaceContext.getPrefixes(attribute);
    final Set<NamespaceLookup> lookups = new HashSet<NamespaceLookup>(prefixes.size());

    for (PsiElement mode : modes) {
      final QName qName = ((ImplicitModeElement)mode).getQName();
      if (qName == null) continue;
      final String prefix = qName.getPrefix();
      if (!prefixes.contains(prefix)) continue;

      lookups.add(new NamespaceLookup(prefix));
    }

    return ArrayUtil.toObjectArray(lookups);
  }

  private static class MyModeMatcher extends MatchTemplateMatcher {
        public MyModeMatcher(XmlDocument document, QName mode) {
            super(document, mode);
        }
        
        public MyModeMatcher(XmlElement element, QName mode) {
            super(XsltCodeInsightUtil.getDocument(element), mode);
        }

        protected PsiElement transform(XmlTag element) {
            return new ImplicitModeElement(element.getAttribute("mode", null));
        }

        public boolean matches(XmlTag element) {
            final String s = element.getAttributeValue("mode");
            return myMode != null &&
                    s != null && s.indexOf(CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED) == -1 &&
                    super.matches(element);
        }

        @Override
        protected ResolveUtil.Matcher changeDocument(XmlDocument document) {
            return new MyModeMatcher(document, myMode);
        }

        public ResolveUtil.Matcher variantMatcher() {
            return new MyModeMatcher(myDocument, myMode != null ? QNameUtil.createAnyLocalName(myMode.getNamespaceURI()) : null);
        }
    }

    @Nonnull
    public String getUnresolvedMessagePattern() {
        final QName qName = myImplicitModeElement.getQName();
        if (qName != null && qName != QNameUtil.UNRESOLVED) {
            return "Undefined mode '" + qName.toString() + "'";
        } else {
            return "Undefined mode ''{0}''";
        }
    }

  private static class MyPrefixReference extends PrefixReference {
    public MyPrefixReference(XmlAttribute attribute) {
      super(attribute);
    }

    @Nonnull
    @Override
    public Object[] getVariants() {
      return getPrefixCompletions(myAttribute);
    }
  }
}
