package fr.inria.diverse.ale.repl.scoping

import org.eclipse.xtext.scoping.impl.ImportUriGlobalScopeProvider
import org.eclipse.xtext.scoping.impl.ImportUriGlobalScopeProvider.URICollector
import org.eclipse.emf.ecore.resource.ResourceSet
import java.util.Set
import org.eclipse.emf.common.util.URI
import org.eclipse.xtext.util.IAcceptor
import org.eclipse.emf.ecore.resource.Resource

class Visitor2replGlobalScopeProvider extends ImportUriGlobalScopeProvider {
	
	static class Visitor2replURICollector extends URICollector {
		new(ResourceSet resourceSet, Set<URI> result) {
			super(resourceSet, result)
		}
		
		override URI resolve(String uriAsString) throws IllegalArgumentException {
			super.resolve(uriAsString.replaceAll("'*", ""))
		}
	}
	
	override IAcceptor<String> createURICollector(Resource resource, Set<URI> collectInto) {
		val resourceSet = resource.getResourceSet()
		return new Visitor2replURICollector(resourceSet, collectInto)
	}
	
}