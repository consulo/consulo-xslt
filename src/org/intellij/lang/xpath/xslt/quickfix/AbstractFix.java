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
package org.intellij.lang.xpath.xslt.quickfix;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.template.TemplateBuilderImpl;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.ide.DataManager;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.LocalTimeCounter;
import com.intellij.util.containers.ContainerUtil;

public abstract class AbstractFix implements IntentionAction {
  @Nonnull
  public String getFamilyName() {
    final String name = getClass().getSimpleName();
    return "XSLT " + name.replaceAll("Fix$", "").replaceAll("(\\p{Lower}+)(\\p{Upper})", "$1 $2");
  }

  public boolean startInWriteAction() {
    return true;
  }

  protected static void moveTo(Editor editor, XmlTag xmlTag) {
    editor.getCaretModel().moveToOffset(xmlTag.getTextRange().getStartOffset());
  }

  protected static void deleteFromDocument(Editor editor, PsiElement dummy) {
    editor.getDocument().deleteString(dummy.getTextRange().getStartOffset(), dummy.getTextRange().getEndOffset());
  }

  protected static TemplateBuilderImpl createTemplateBuilder(XmlTag xmlTag) {
    final PsiFile psiFile = PsiFileFactory.getInstance(xmlTag.getProject())
      .createFileFromText("dummy.xml", XmlFileType.INSTANCE, xmlTag.getText(), LocalTimeCounter.currentTime(), true, false);
    return new TemplateBuilderImpl(psiFile);
  }

  public final boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile file) {
    if (requiresEditor() && editor == null) return false;

    return isAvailableImpl(project, editor, file);
  }

  protected abstract boolean isAvailableImpl(@Nonnull Project project, @Nullable Editor editor, PsiFile file);

  protected abstract boolean requiresEditor();

  @Nullable
  public LocalQuickFix createQuickFix(boolean isOnTheFly) {
    final boolean requiresEditor = requiresEditor();
    if (requiresEditor && !isOnTheFly) return null;

    return new LocalQuickFix() {
      @Nonnull
      public String getName() {
        return AbstractFix.this.getText();
      }

      @Nonnull
      public String getFamilyName() {
        return AbstractFix.this.getFamilyName();
      }

      public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
        Editor editor;
        if (requiresEditor) {
          final DataContext dataContext = DataManager.getInstance().getDataContext();

          editor = dataContext.getData(LangDataKeys.EDITOR);
          if (editor == null) {
            if ((editor = FileEditorManager.getInstance(project).getSelectedTextEditor()) == null) {
              return;
            }
          }
        }
        else {
          editor = null;
        }

        final PsiFile psiFile = descriptor.getPsiElement().getContainingFile();
        if (!isAvailable(project, editor, psiFile)) {
          return;
        }
        if (!FileModificationService.getInstance().prepareFileForWrite(psiFile)) {
          return;
        }
        try {
          invoke(project, editor, psiFile);
        }
        catch (IncorrectOperationException e) {
          Logger.getInstance(getClass().getName()).error(e);
        }
      }
    };
  }

  public static LocalQuickFix[] createFixes(LocalQuickFix... fixes) {
    final List<LocalQuickFix> result = ContainerUtil.findAll(fixes, new Condition<LocalQuickFix>() {
      public boolean value(LocalQuickFix localQuickFix) {
        return localQuickFix != null;
      }
    });
    return result.toArray(new LocalQuickFix[result.size()]);
  }
}