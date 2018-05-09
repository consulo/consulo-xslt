
/*
 * Created by IntelliJ IDEA.
 * User: sweinreuter
 * Date: 14.12.2005
 * Time: 18:31:50
 */
package org.intellij.lang.xpath.xslt.psi;

import javax.annotation.Nonnull;

public interface XsltTemplateInvocation extends XsltElement {
    @Nonnull
    XsltWithParam[] getArguments();
}