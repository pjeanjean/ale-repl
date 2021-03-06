/*
 * generated by Xtext 2.14.0-SNAPSHOT
 */
package fr.inria.diverse.logo.xtext.validation;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.xtext.validation.AbstractDeclarativeValidator;

public abstract class AbstractLogoValidator extends AbstractDeclarativeValidator {
	
	@Override
	protected List<EPackage> getEPackages() {
		List<EPackage> result = new ArrayList<EPackage>();
		result.add(EPackage.Registry.INSTANCE.getEPackage("http://www.example.org/logo"));
		result.add(EPackage.Registry.INSTANCE.getEPackage("http://www.example.org/logo/expression"));
		result.add(EPackage.Registry.INSTANCE.getEPackage("http://www.example.org/logo/statement"));
		result.add(EPackage.Registry.INSTANCE.getEPackage("http://www.example.org/logo/statement/control"));
		result.add(EPackage.Registry.INSTANCE.getEPackage("http://www.example.org/logo/expression/extended"));
		result.add(EPackage.Registry.INSTANCE.getEPackage("http://www.example.org/logo/expression/binary"));
		result.add(EPackage.Registry.INSTANCE.getEPackage("http://www.example.org/logo/expression/unary"));
		result.add(EPackage.Registry.INSTANCE.getEPackage("http://www.example.org/logo/expression/constant"));
		return result;
	}
}
