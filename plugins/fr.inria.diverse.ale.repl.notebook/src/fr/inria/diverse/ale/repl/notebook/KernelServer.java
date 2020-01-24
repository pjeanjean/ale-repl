package fr.inria.diverse.ale.repl.notebook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecoretools.ale.core.parser.Dsl;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import fr.inria.diverse.ale.repl.REPLInterpreter;
import fr.inria.diverse.ale.repl.lsp4j.CompletionClient;
import fr.inria.diverse.ale.repl.lsp4j.Helper;

public abstract class KernelServer {
	
	private Dsl environment;
	private String xtextExtension;
	private int gemocPort;
	private int lspPort;
	
	private REPLInterpreter interpreter;
	private Socket lspClientSocket;
	private PrintStream lspServerInput;
	private BufferedReader lspServerOutput;
	private MessageJsonHandler lspHandler;
	
	private HttpServer server;

	public KernelServer(Dsl environment, String xtextExtension, int gemocPort, int lspPort) {
		this.environment = environment;
		this.xtextExtension = xtextExtension;
		this.gemocPort = gemocPort;
		this.lspPort = lspPort;
	}
	
	public KernelServer(String ecorePath, String alePath, String xtextExtension,
			int gemocPort, int lspPort) {
		this(new Dsl(Arrays.asList(URI.createFileURI(ecorePath).toString()),
				Arrays.asList(alePath)), xtextExtension, gemocPort, lspPort);
	}
	
	public void start() {
		try {
			this.server = HttpServer.create(new InetSocketAddress(gemocPort), 0);
			HttpContext interpretContext = this.server.createContext("/interpret");
			interpretContext.setHandler(new HttpHandler() {	
				@Override
				public void handle(HttpExchange exchange) throws IOException {
					if (exchange.getRequestMethod().equals("PUT")) {
						try {
							String toExe = new String(exchange.getRequestBody().readAllBytes());
							interpreter.interpret(toExe);
							String errors = interpreter.getErrors();
							if (!errors.equals("")) {
								exchange.sendResponseHeaders(400, errors.getBytes().length);
								OutputStream os = exchange.getResponseBody();
								os.write(errors.getBytes());
								os.close();
							} else {
								String output = interpreter.getOutput();
								exchange.sendResponseHeaders(200, output.getBytes().length);
								OutputStream os = exchange.getResponseBody();
								os.write(output.getBytes());
								os.close();
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			});
			HttpContext completeContext = server.createContext("/complete");
			completeContext.setHandler(new HttpHandler() {	
				@Override
				public void handle(HttpExchange exchange) throws IOException {
					if (exchange.getRequestMethod().equals("PUT")) {
						try {
							String request = new String(exchange.getRequestBody().readAllBytes());
							String splitted[] = request.split("\\|\\|\\|");
							
							lspServerInput.print(Helper.createNotification(lspHandler, "textDocument/didOpen",
									new DidOpenTextDocumentParams(new TextDocumentItem("inmemory:/model." + xtextExtension,
											xtextExtension, 1, splitted[1]))));
							lspServerInput.flush();
							
							int length = Integer.parseInt(lspServerOutput.readLine().substring(16));
							lspServerOutput.readLine();
							char read[] = new char[length];
							lspServerOutput.read(read);
							
							String lines[] = splitted[1].split("\\n");
							for (int i = 0; i < lines.length; i++) {
								lines[i] += "\n";
							}
							int pos = Integer.parseInt(splitted[0]);
							
							int linePos = 0;
							while (lines[linePos].length() < pos) {
								pos -= lines[linePos++].length();
							}
							int charPos = pos;
							
							lspServerInput.print(Helper.createRequest(lspHandler, "textDocument/completion",
									new CompletionParams(new TextDocumentIdentifier("inmemory:/model." + xtextExtension),
											new Position(linePos, charPos))));
							lspServerInput.flush();
							
							String message = lspServerOutput.readLine();
							length = Integer.parseInt(message.substring(16));
							message += "\r\n" + lspServerOutput.readLine() + "\r\n";
							read = new char[length];
							lspServerOutput.read(read);
							message += new String(read);
							
							List<CompletionItem> completions = new ArrayList<>();
							Object result = Helper.createResponse(message, lspHandler);
							if (result instanceof Either<?, ?>) {
								Either<List<CompletionItem>, CompletionList> either =
									(Either<List<CompletionItem>, CompletionList>) result;
								if (either.isLeft()) {
									completions = either.getLeft();
								} else if (either.isRight()) {
									completions = either.getRight().getItems();
								}
							}
							
							String output = completions.stream().map(c -> c.getTextEdit())
									.map(t -> {
										Range range = t.getRange();
										int startPos = 0;
										for (int i = 0; i < range.getStart().getLine(); i++) {
											startPos += lines[i].length();
										}
										startPos += range.getStart().getCharacter();
										int endPos = 0;
										for (int i = 0; i < range.getEnd().getLine(); i++) {
											endPos += lines[i].length();
										}
										endPos += range.getEnd().getCharacter();
										return t.getNewText() + "\n" + startPos + "\n" + endPos;
									}).collect(Collectors.joining("\n"));
							exchange.sendResponseHeaders(200, output.getBytes().length);
							OutputStream os = exchange.getResponseBody();
							os.write(output.getBytes());
							os.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			});
			this.interpreter = new REPLInterpreter(this.environment, this.xtextExtension);
			this.lspClientSocket = new Socket("127.0.0.1", this.lspPort);
			this.lspServerInput = new PrintStream(this.lspClientSocket.getOutputStream());
			this.lspServerOutput = new BufferedReader(new InputStreamReader(this.lspClientSocket.getInputStream()));
			this.lspHandler = Helper.createHandler(CompletionClient.class);
			
			server.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void stop() {
		this.server.stop(0);
	}
	
	public abstract void install(String kernelLocation);
	
	public abstract void uninstall(String kernelLocation);
	
}
