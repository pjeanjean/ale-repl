/*
 * generated by Xtext 2.10.0
 */
package org.tetrabox.minijava.xtext.ui.wizard


import com.google.inject.Inject
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.resource.FileExtensionProvider

class MiniJavaNewProjectWizardInitialContents {
	@Inject
	FileExtensionProvider fileExtensionProvider

	def generateInitialContents(IFileSystemAccess2 fsa) {
		fsa.generateFile(
			"src/example." + fileExtensionProvider.primaryFileExtension,
			'''
			/*
			 * This is a MiniJava example
			 */
			package minijava.example;
			
			class C {
				public String toString() {
					return "Hello from class C!";
				}
			}
			'''
			)
	}
}
