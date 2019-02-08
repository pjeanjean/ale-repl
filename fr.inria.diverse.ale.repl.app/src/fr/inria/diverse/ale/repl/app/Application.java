package fr.inria.diverse.ale.repl.app;

import java.util.Scanner;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import fr.inria.diverse.ale.repl.REPLInterpreter;

public class Application implements IApplication {

	@Override
	public Object start(IApplicationContext context) throws Exception {
		String args[] = (String[]) context.getArguments().get("application.args");
		
		if (args.length != 3) {
			System.err.println("Expecting 3 parameters: <path_to_ecore_file> <path_to_ale_file> <xtext_extension>");
			return null;
		}
		
		REPLInterpreter repl = new REPLInterpreter(args[0], args[1], args[2]);
		
		Scanner scanner = new Scanner(System.in);
		String read = "";
		while (true) {
			System.out.print("~ ");
			System.out.flush();
				
			if ((read = scanner.nextLine()).equals("exit")) {
				break;
			}
		
			if (repl.interpret(read)) {
				System.out.println(repl.getOutput());
			} else {
				System.err.println(repl.getErrors());
			}
		}
		scanner.close();
		
		return null;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

}
