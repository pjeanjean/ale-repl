/**
 * generated by Xtext 2.11.0
 */
package org.tetrabox.minijava.xtext.ide;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.eclipse.xtext.util.Modules2;
import org.tetrabox.minijava.xtext.MiniJavaRuntimeModule;
import org.tetrabox.minijava.xtext.MiniJavaStandaloneSetup;
import org.tetrabox.minijava.xtext.ide.MiniJavaIdeModule;

/**
 * Initialization support for running Xtext languages as language servers.
 */
@SuppressWarnings("all")
public class MiniJavaIdeSetup extends MiniJavaStandaloneSetup {
  @Override
  public Injector createInjector() {
    MiniJavaRuntimeModule _miniJavaRuntimeModule = new MiniJavaRuntimeModule();
    MiniJavaIdeModule _miniJavaIdeModule = new MiniJavaIdeModule();
    return Guice.createInjector(Modules2.mixin(_miniJavaRuntimeModule, _miniJavaIdeModule));
  }
}
