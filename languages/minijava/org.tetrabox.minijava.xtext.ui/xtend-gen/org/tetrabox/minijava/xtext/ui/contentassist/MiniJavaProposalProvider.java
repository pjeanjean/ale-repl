/**
 * generated by Xtext 2.10.0
 */
package org.tetrabox.minijava.xtext.ui.contentassist;

import com.google.common.base.Predicate;
import com.google.inject.Inject;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.xtext.AbstractElement;
import org.eclipse.xtext.Assignment;
import org.eclipse.xtext.CrossReference;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor;
import org.eclipse.xtext.xbase.lib.Extension;
import org.tetrabox.minijava.xtext.MiniJavaModelUtil;
import org.tetrabox.minijava.xtext.miniJava.Clazz;
import org.tetrabox.minijava.xtext.miniJava.Field;
import org.tetrabox.minijava.xtext.miniJava.Member;
import org.tetrabox.minijava.xtext.miniJava.Method;
import org.tetrabox.minijava.xtext.ui.contentassist.AbstractMiniJavaProposalProvider;
import org.tetrabox.minijava.xtext.validation.MiniJavaAccessibility;

/**
 * See https://www.eclipse.org/Xtext/documentation/304_ide_concepts.html#content-assist
 * on how to customize the content assistant.
 */
@SuppressWarnings("all")
public class MiniJavaProposalProvider extends AbstractMiniJavaProposalProvider {
  @Inject
  @Extension
  private MiniJavaModelUtil _miniJavaModelUtil;
  
  @Inject
  @Extension
  private MiniJavaAccessibility _miniJavaAccessibility;
  
  @Override
  public StyledString getStyledDisplayString(final EObject element, final String qualifiedNameAsString, final String shortName) {
    StyledString _xifexpression = null;
    if ((element instanceof Member)) {
      String _memberAsStringWithType = this._miniJavaModelUtil.memberAsStringWithType(((Member)element));
      StyledString _styledString = new StyledString(_memberAsStringWithType);
      EObject _eContainer = ((Member)element).eContainer();
      String _name = ((Clazz) _eContainer).getName();
      String _plus = (" - " + _name);
      StyledString _styledString_1 = new StyledString(_plus, StyledString.QUALIFIER_STYLER);
      _xifexpression = _styledString.append(_styledString_1);
    } else {
      _xifexpression = super.getStyledDisplayString(element, qualifiedNameAsString, shortName);
    }
    return _xifexpression;
  }
  
  @Override
  public void completeSelectionExpression_Method(final EObject model, final Assignment a, final ContentAssistContext context, final ICompletionProposalAcceptor acceptor) {
    AbstractElement _terminal = a.getTerminal();
    final Predicate<IEObjectDescription> _function = (IEObjectDescription description) -> {
      EObject _eObjectOrProxy = description.getEObjectOrProxy();
      return this._miniJavaAccessibility.isAccessibleFrom(((Method) _eObjectOrProxy), model);
    };
    this.lookupCrossReference(((CrossReference) _terminal), context, acceptor, _function);
  }
  
  @Override
  public void completeSelectionExpression_Field(final EObject model, final Assignment a, final ContentAssistContext context, final ICompletionProposalAcceptor acceptor) {
    AbstractElement _terminal = a.getTerminal();
    final Predicate<IEObjectDescription> _function = (IEObjectDescription description) -> {
      EObject _eObjectOrProxy = description.getEObjectOrProxy();
      return this._miniJavaAccessibility.isAccessibleFrom(((Field) _eObjectOrProxy), model);
    };
    this.lookupCrossReference(((CrossReference) _terminal), context, acceptor, _function);
  }
}