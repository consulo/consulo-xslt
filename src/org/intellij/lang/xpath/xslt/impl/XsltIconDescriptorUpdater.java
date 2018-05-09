/*
 * Copyright 2013 Consulo.org
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
package org.intellij.lang.xpath.xslt.impl;

import org.intellij.lang.xpath.xslt.XsltSupport;
import javax.annotation.Nonnull;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import consulo.ide.IconDescriptor;
import consulo.ide.IconDescriptorUpdater;
import icons.XsltIcons;

/**
 * @author VISTALL
 * @since 15:02/20.07.13
 */
public class XsltIconDescriptorUpdater implements IconDescriptorUpdater
{
  @Override
  public void updateIcon(@Nonnull IconDescriptor iconDescriptor, @Nonnull PsiElement element, int flags) {
    if (element instanceof PsiFile && XsltSupport.isXsltFile((PsiFile) element)) {
      iconDescriptor.addLayerIcon(XsltIcons.Xslt_filetype_overlay);
    }
  }
}
