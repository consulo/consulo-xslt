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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.IncorrectOperationException;

abstract class SimpleAttributeReference implements PsiReference {
    protected final XmlAttribute myAttribute;

    protected SimpleAttributeReference(XmlAttribute attribute) {
        myAttribute = attribute;
    }

    @Nonnull
    public String getCanonicalText() {
        return getTextRange().substring(myAttribute.getValue());
    }

    public PsiElement getElement() {
        final XmlAttributeValue value = myAttribute.getValueElement();
        assert value != null;
        return value;
    }

    public TextRange getRangeInElement() {
        return getTextRange().shiftRight(1);
    }

    public boolean isReferenceTo(PsiElement element) {
        if (this instanceof PsiPolyVariantReference) {
            final PsiPolyVariantReference reference = (PsiPolyVariantReference)this;
            final ResolveResult[] results = reference.multiResolve(false);
            for (ResolveResult result : results) {
                if (Comparing.equal(result.getElement(), element)) return true;
            }
            return false;
        } else {
            return Comparing.equal(resolve(), element);
        }
    }

    public PsiElement bindToElement(@Nonnull PsiElement element) throws IncorrectOperationException {
        throw new UnsupportedOperationException();
    }

    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        myAttribute.setValue(getTextRange().replace(myAttribute.getValue(), newElementName));
        final XmlAttributeValue value = myAttribute.getValueElement();
        assert value != null;
        return value;
    }

    @Nullable
    public final PsiElement resolve() {
        return ResolveCache.getInstance(myAttribute.getProject()).resolveWithCaching(this, new ResolveCache.Resolver() {
            @Nullable
            public PsiElement resolve(@Nonnull PsiReference psiReference, boolean b) {
                return resolveImpl();
            }
        }, false, false);
    }

    @Nullable
    protected abstract PsiElement resolveImpl();

    @Nonnull
    protected abstract TextRange getTextRange();
}