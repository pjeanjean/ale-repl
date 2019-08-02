/*
 * generated by Xtext 2.11
 */
package org.eclipse.xtext.parser.antlr;

import com.google.inject.Inject;
import org.eclipse.xtext.parser.antlr.internal.InternalXtextParser;
import org.eclipse.xtext.services.XtextGrammarAccess;

public class XtextParser extends AbstractAntlrParser {

	@Inject
	private XtextGrammarAccess grammarAccess;

	@Override
	protected void setInitialHiddenTokens(XtextTokenStream tokenStream) {
		tokenStream.setInitialHiddenTokens("RULE_WS", "RULE_ML_COMMENT", "RULE_SL_COMMENT");
	}
	

	@Override
	protected InternalXtextParser createParser(XtextTokenStream stream) {
		return new InternalXtextParser(stream, getGrammarAccess());
	}

	@Override 
	protected String getDefaultRuleName() {
		return "Grammar";
	}

	public XtextGrammarAccess getGrammarAccess() {
		return this.grammarAccess;
	}

	public void setGrammarAccess(XtextGrammarAccess grammarAccess) {
		this.grammarAccess = grammarAccess;
	}
}
