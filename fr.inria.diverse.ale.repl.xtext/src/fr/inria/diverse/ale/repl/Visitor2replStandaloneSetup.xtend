/*
 * generated by Xtext 2.14.0
 */
package fr.inria.diverse.ale.repl


/**
 * Initialization support for running Xtext languages without Equinox extension registry.
 */
class Visitor2replStandaloneSetup extends Visitor2replStandaloneSetupGenerated {

	def static void doSetup() {
		new Visitor2replStandaloneSetup().createInjectorAndDoEMFRegistration()
	}
}
