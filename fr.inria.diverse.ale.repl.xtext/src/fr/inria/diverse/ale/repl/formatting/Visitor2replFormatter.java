package fr.inria.diverse.ale.repl.formatting;

import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.formatting.impl.AbstractDeclarativeFormatter;
import org.eclipse.xtext.formatting.impl.FormattingConfig;
import org.eclipse.xtext.util.Pair;

import fr.inria.diverse.ale.repl.services.Visitor2replGrammarAccess;

public class Visitor2replFormatter extends AbstractDeclarativeFormatter {
	
	@Override
	protected void configureFormatting(FormattingConfig c) {
		Visitor2replGrammarAccess f = (Visitor2replGrammarAccess) getGrammarAccess();
			
		for (Keyword comma : f.findKeywords(",")) {
			c.setNoLinewrap().before(comma);
			c.setNoSpace().before(comma);
		}
		
		for (Keyword semicolon : f.findKeywords(";")) {
			c.setNoLinewrap().before(semicolon);
			c.setNoSpace().before(semicolon);
			c.setLinewrap().after(semicolon);
		}
		
		for (Keyword dot : f.findKeywords(".")) {
			c.setNoSpace().before(dot);
			c.setNoSpace().after(dot);
		}
		
		for (Keyword dot : f.findKeywords("::")) {
			c.setNoSpace().before(dot);
			c.setNoSpace().after(dot);
		}
		
		for (Pair<Keyword, Keyword> pair : f.findKeywordPairs("(", ")")) {
			c.setNoSpace().before(pair.getFirst());
			c.setNoSpace().after(pair.getFirst());
			c.setNoSpace().before(pair.getSecond());
			c.setNoLinewrap().before(pair.getFirst());
			c.setNoLinewrap().after(pair.getSecond());
		}
		
		for (Pair<Keyword, Keyword> pair : f.findKeywordPairs("{", "}")) {
			c.setIndentation(pair.getFirst(), pair.getSecond());
			c.setLinewrap().after(pair.getFirst());
			c.setLinewrap().before(pair.getSecond());
			c.setLinewrap().after(pair.getSecond());
		}

		for (Keyword model : f.findKeywords("model")) {
			c.setIndentationIncrement().after(model);
		}
		c.setIndentationDecrement().after(f.getModelRule());
		
		for (Keyword colon : f.findKeywords(":")) {
			c.setNoSpace().before(colon);
			c.setNoLinewrap().before(colon);
			c.setLinewrap().after(colon);
		}
		
		c.setIndentation(f.getInstructionAccess().getColonKeyword_2(),
				f.getInstructionAccess().getSemicolonKeyword_9());
		c.setLinewrap().after(f.getInstructionAccess().getEqualsSignGreaterThanSignKeyword_8_0());
		
		c.setLinewrap(2).after(f.getModelRule());
		c.setLinewrap(2).after(f.getInterpreterRule());
	}	
}
