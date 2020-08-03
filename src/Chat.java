import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Chat class to handle the main chatting application.
 *
 * Allows the client to connect to the server and send messages back and forth.
 *
 * @since 27 July 2019
 * @version 1.0.0
 * @author Kyle Chapman, Noah Atkins
 */

public class Chat {

	// globals
	public static final int WIDTH = 925;
	public static final int HEIGHT = 615;
	private static final int LIMIT = 255;

	public Label lblUsername, lblWhisperTo;
	public TextField txfMessage;
	public TextArea txaMessages, txaWhispers;
	public Button btnSend, btnConnectStatus;
	public Tab tabGlobal, tabWhispers;
	public ComboBox<String> cmbWhisperTo;
	public ListView<String> lstOnlineUsers, lstOfflineUsers;

	private int port;
	private String ip, username;

	private Client client;

	// default constructor
	public Chat() {
		this.lblUsername = new Label();
		this.lblWhisperTo = new Label();
		this.txfMessage = new TextField();
		this.txaMessages = new TextArea();
		this.txaWhispers = new TextArea();
		this.btnSend = new Button();
		this.btnConnectStatus = new Button();
		this.tabGlobal = new Tab();
		this.tabWhispers = new Tab();
		this.cmbWhisperTo = new ComboBox<>();
		this.lstOnlineUsers = new ListView<>();
		this.lstOfflineUsers = new ListView<>();
	}

	/**
	 * Starts the chat program.
	 *
	 * @param ip the ip address of client connecting
	 * @param port the port number of client connecting
	 * @param username the username of client connection
	 * @param client the current state of the client object
	 */
	public void runChat(String ip, int port, String username, Client client) {
		this.ip = ip;
		this.port = port;
		this.username = username;
		this.client = client;

		txfMessage.requestFocus();
		Main.window.setWidth(WIDTH);
		Main.window.setHeight(HEIGHT);

		// make sure all connections closed on exit
		Main.window.setOnCloseRequest(event -> {
			this.client.logout(this.username);
			System.exit(0);
		});

		tabGlobal.setContent(txaMessages);

		System.out.printf("Connected to %s:%d with username %s\n", this.ip, this.port, this.username);
		this.client.readServerMsgs(this.txaMessages, this.txaWhispers,
			this.lstOnlineUsers, this.lstOfflineUsers, this.cmbWhisperTo);
	}

	/**
	 * Sends a message to the global chat.
	 *
	 * @param msg the message to send.
	 */
	public void message(String msg) {
		if (msg.equals("")) {
			return;
		}

		String fullMsg = String.format("\n%s (You) : %s\n", this.username, msg);

		txaMessages.appendText(fullMsg);
		tabGlobal.setContent(txaMessages);
		this.client.send(msg);
	}

	/**
	 * Sends a direct message (whisper) to a specific user.
	 *
	 * @param toUser the user to send the message to.
	 * @param msg the message to send the user.
	 */
	public void whisper(String toUser, String msg) {
		if (msg.equals("")) {
			return;
		}

		if (toUser == null) {
			txaWhispers.appendText("\n-- no user selected --");
			return;
		}

		String fullMsg = String.format("\n%s (You) : %s\n", this.username, msg);

		txaWhispers.appendText(fullMsg);
		this.client.whisper(toUser, msg);
	}

	/**
	 * Sends a message to the text area.
	 *
	 * @param msg message to send.
	 */
	public void send(String msg) {
		if (tabGlobal.isSelected()) {
			message(msg);
		} else if (tabWhispers.isSelected()) {
			String selected = cmbWhisperTo.getSelectionModel().getSelectedItem();
			whisper(selected, msg);
		}

		txfMessage.setText("");
		Main.window.show();
	}

	/**
	 * Handles keyboard events.
	 *
	 * @param event key pressed by user.
	 */
	public void enter(KeyEvent event) {
		String currText = txfMessage.getText();

		if (currText.length() >= LIMIT) {
			txfMessage.setText(currText.substring(0, LIMIT-1));
			txfMessage.positionCaret(255);
		}

		if (event.getCode() == KeyCode.ENTER) {
			String message = currText;
			send(message);
		}
	}

	/**
	 * Sends a message and displays message sent.
	 *
	 * @param event button clicked to send message.
	 */
	public void sendMsg(ActionEvent event) {
		String message = txfMessage.getText();
		send(message);
	}

	/**
	 * Changes the connection status of the current user.
	 *
	 * @param event when button is clicked.
	 */
	public void changeConnectStatus(ActionEvent event) {
		String currStatus = btnConnectStatus.getText();

		if (currStatus.equals("Disconnect")) {
			btnConnectStatus.setText("Connect");
			this.client.logout(this.username);
			System.exit(0);
		}
	}

	/**
	 * Sets the username label.
	 *
	 * @param username username of client.
	 */
	public void setName(String username) {
		lblUsername.setText(username);
	}
}
