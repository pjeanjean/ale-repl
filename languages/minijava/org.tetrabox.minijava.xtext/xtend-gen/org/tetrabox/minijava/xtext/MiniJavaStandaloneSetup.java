/**
 * generated by Xtext 2.10.0
 */
package org.tetrabox.minijava.xtext;

import org.tetrabox.minijava.xtext.MiniJavaStandaloneSetupGenerated;

/**
 * Initialization support for running Xtext languages without Equinox extension registry.
 */
@SuppressWarnings("all")
public class MiniJavaStandaloneSetup extends MiniJavaStandaloneSetupGenerated {
  public static void doSetup() {
    new MiniJavaStandaloneSetup().createInjectorAndDoEMFRegistration();
  }
}
