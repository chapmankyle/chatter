import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import javax.swing.JOptionPane;

import java.util.ArrayList;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Main class that handles the displaying of the GUIs.
 *
 * Handles switching between the Login GUI and the Chat GUI, if the login specified
 * in the Login GUI is accepted.
 *
 * @since 27 July 2019
 * @version 1.0.0
 * @author Kyle Chapman, Noah Atkins
 */

public class Main extends Application implements EventHandler<ActionEvent> {

	// globals
	public int port;
	public String ip, username;

	public Button btnLogin;
	public TextField txfIP, txfPort, txfUsername, txfChat;

	public static Stage window;

	private Scene scLogin;
	private ArrayList<String> commands;

	private static Client client;

	/**
	 * Initializes all the global variables.
	 */
	private void initGlobals() {
		this.port = 8080;
		this.ip = "localhost";
		this.username = "";

		this.btnLogin = new Button();
		this.txfIP = new TextField();
		this.txfPort = new TextField();
		this.txfUsername = new TextField();
		this.txfChat = new TextField();

		this.scLogin = null;
		this.commands = new ArrayList<>();
		client = null;
	}

	/**
	 * Displays the Login GUI.
	 *
	 * @param primaryStage main stage to currently view.
	 * @throws IOException when the fxml file cannot be loaded.
	 */
	@Override
	public void start(Stage primaryStage) throws IOException {
		initGlobals();

		window = primaryStage;

		// find login fxml file
		FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
		Parent root = loader.load();
		scLogin = new Scene(root);

		window.setTitle("Dispeak Chat");
		window.setScene(scLogin);
		window.show();
	}

	/**
	 * Loads a scene specified by the fxmlPath parameter.
	 *
	 * @param fxmlPath path to the scene to load.
	 * @throws IOException when the fxml file cannot be loaded.
	 */
	private void loadScene(String fxmlPath) throws IOException {
		String hostname = "", portTxt = "", usrname = "";

		// get input from text fields
		hostname = txfIP.getText();
		portTxt = txfPort.getText();
		usrname = txfUsername.getText();

		boolean validIP = hostname.matches("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");

		// check if valid IPv4 address
		if (hostname.length() == 0) {
			hostname = "localhost";
		} else if (!hostname.equals("localhost") && !validIP) {
			JOptionPane.showMessageDialog(null, "Invalid IPv4 Address");
			return;
		}

		// user hasn't filled in all the text fields
		if (portTxt.length() == 0) {
			portTxt = "8080";
		}

		// not allowed to have no username
		if (usrname.length() == 0 || usrname.contains(" ")) {
			JOptionPane.showMessageDialog(null, "Invalid Username");
			return;
		}

		// check if valid port number
		boolean validPort = portTxt.matches("^[0-9]{4}$");
		if (!validPort) {
			JOptionPane.showMessageDialog(null, "Invalid Port Number");
			return;
		}

		this.port = Integer.parseInt(portTxt);
		this.username = usrname;

		// establish client-server connection
		this.ip = hostname;
		client = new Client(this.ip, this.port);

		// if client connection fails, retry
		if (!client.connect()) {
			return;
		}

		// add user listener
		client.addUserListener(new UserListener() {
			@Override
			public void online(String username) {
				System.out.println("-> " + username + " has joined!");
			}

			@Override
			public void offline(String username) {
				System.out.println("-> " + username + " has left!");
			}
		});

		// find if username corresponds to command
		this.commands = client.getCommands();

		if (this.commands.contains(this.username)) {
			this.username = "";
			JOptionPane.showMessageDialog(null, "Invalid username");
			return;
		}

		if (!client.login(this.username)) {
			JOptionPane.showMessageDialog(null, "Username already exists");
			return;
		}

		// get scene
		FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
		Parent root = loader.load();
		Chat controller = loader.getController();

		controller.setName(this.username);
		btnLogin.getScene().setRoot(root);
		controller.runChat(this.ip, this.port, this.username, client);
	}

	/**
	 * Handles KeyEvents by the user.
	 *
	 * @param event key pressed by the user.
	 * @throws IOException when the fxml file cannot be loaded.
	 */
	public void enter(KeyEvent event) throws IOException {
		if (event.getCode() == KeyCode.ENTER) {
			loadScene("Chat.fxml");
		}
	}

	/**
	 * Handles the user logging in to the server.
	 *
	 * @param event any action performed by the user.
	 * @throws IOException when the fxml file cannot be loaded.
	 */
	public void login(ActionEvent event) throws IOException {
		loadScene("Chat.fxml");
	}

	@Override
	public void handle(ActionEvent event) { }

	/**
	 * Main function to run the GUI.
	 *
	 * @param args command-line arguments.
	 * @throws Exception when the JavaFX application cannot be started.
	 */
	public static void main(String[] args) throws Exception {
		// adds a hook to check for a Ctrl-C
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(200);

					// if client connection hasn't been established
					if (client == null) {
						System.out.println("\n\033[31mShutting down ...\033[0m\n");
						return;
					}

					System.err.println("\n\033[31mShutting down client ...\033[0m\n");
					String cmd = "logout " + client.getUsername();

					try {
						DataOutputStream dos = client.getDos();

						// if dos is null, all connections have already been closed
						if (dos == null) {
							return;
						}

						dos.writeUTF(cmd);
					} catch (IOException e) { }

					client.setUsername("");
					client.closeAll();
				} catch (InterruptedException e) {
					System.err.println("Error in shutdown hook : " + e);
				}
			}
		});

		launch(args);
	}
}
