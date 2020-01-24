package fr.inria.diverse.ale.repl.notebook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;

import org.eclipse.emf.ecoretools.ale.core.parser.Dsl;
import org.eclipse.gemoc.ale.interpreted.engine.Helper;
import org.eclipse.gemoc.executionframework.engine.commons.DslHelper;

import fr.inria.diverse.ale.repl.REPLInterpreter;
import io.github.spencerpark.jupyter.channels.JupyterConnection;
import io.github.spencerpark.jupyter.channels.JupyterSocket;
import io.github.spencerpark.jupyter.kernel.BaseKernel;
import io.github.spencerpark.jupyter.kernel.KernelConnectionProperties;
import io.github.spencerpark.jupyter.kernel.LanguageInfo;
import io.github.spencerpark.jupyter.kernel.display.DisplayData;

public class AleKernel extends BaseKernel {

	public static void run(Path connectionFile, String languageName)
			throws InvalidKeyException, NoSuchAlgorithmException, IOException {
        if (!Files.isRegularFile(connectionFile))
            throw new IllegalArgumentException("Connection file '" + connectionFile + "' isn't a file.");

        String contents = new String(Files.readAllBytes(connectionFile));

        JupyterSocket.JUPYTER_LOGGER.setLevel(Level.WARNING);

        KernelConnectionProperties connProps = KernelConnectionProperties.parse(contents);
        JupyterConnection connection = new JupyterConnection(connProps);

        AleKernel kernel = new AleKernel(languageName);
        kernel.becomeHandlerForConnection(connection);

        connection.connect();
        connection.waitUntilClose();

        kernel = null;
	}
	
	
	private String languageName;

	private Dsl environment;
	private String xtextExtension;
	private LanguageInfo languageInfo;

	private REPLInterpreter interpreter;
	
	public AleKernel(String languageName) {
		this.languageName = languageName;
		this.init();
	}
	
	private void init() {
		this.environment = Helper.gemocDslToAleDsl(DslHelper.load("Logo_repl"));
		this.xtextExtension = languageName.toLowerCase();
		this.languageInfo = new LanguageInfo.Builder(this.languageName)
				.fileExtension(this.xtextExtension)
				.build();
		this.interpreter = new REPLInterpreter(this.environment, this.xtextExtension);
	}
	
	@Override
	public DisplayData eval(String expr) throws Exception {
		this.interpreter.interpret(expr);
		String errors = interpreter.getErrors();
		if (!errors.equals("")) {
			throw new Exception(errors);
		} else {
			return this.getRenderer().render(this.interpreter.getErrors());
		}
	}

	@Override
	public LanguageInfo getLanguageInfo() {
		return this.languageInfo;
	}

}
