import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import javax.swing.*;

/**
 * Client class for handling everything that isn't to do with the server.
 *
 * Each new client is handled through the ClientHandler class, where Client-Server
 * interaction takes place.
 *
 * @since 27 July 2019
 * @version 1.0.0
 * @author Kyle Chapman, Noah Atkins
 */

public class Client {

	// globals
	private final String hostname;
	private final int port;

	private String username;
	private Socket client;
	private DataInputStream dis;
	private DataOutputStream dos;

	private ArrayList<UserListener> userListeners;
	private ArrayList<String> commands;

	private TextArea globalTxa;
	private TextArea whisperTxa;
	private ListView<String> lstOnline;
	private ListView<String> lstOffline;
	private ComboBox<String> cmbWhisperTo;

	// default constructor
	public Client(String hostname, int port) {
		this.hostname = hostname;
		this.port = port;
		this.username = "";
		this.userListeners = new ArrayList<>();
		this.commands = new ArrayList<>();

		// add commands
		this.commands.add("login");
		this.commands.add("logout");
		this.commands.add("msg");
		this.commands.add("online");
		this.commands.add("offline");
	}

	/**
	 * Try connect to the server, exit program if fails.
	 *
	 * @return {@code true} if connection is successful, {@code false} otherwise.
	 */
	public boolean connect() {
		try {
			this.client = new Socket(this.hostname, this.port);

			// get input / output streams
			this.dis = new DataInputStream(this.client.getInputStream());
			this.dos = new DataOutputStream(this.client.getOutputStream());
		} catch (Exception e) {
			System.out.printf("No connection available for %s:%d\n", this.hostname, this.port);
			JOptionPane.showMessageDialog(null, "No server listening on port " + this.port);
			return false;
		}

		return true;
	}

	/**
	 * Logs a user into the server.
	 *
	 * @param username the username of the user to log in.
	 */
	public boolean login(String username) {
		String cmd = "login " + username;
		String resp = "";

		try {
			this.dos.writeUTF(cmd);
			resp = this.dis.readUTF();
		} catch (IOException e) {
			System.err.println("Cannot send login command.");
		}

		if (resp.equals("login success")) {
			this.username = username;
			return true;
		}

		return false;
	}

	/**
	 * Send logout command to server.
	 *
	 * @param usrname the username of the user to log out.
	 */
	public void logout(String usrname) {
		String cmd = "logout " + username;

		try {
			this.dos.writeUTF(cmd);
		} catch (IOException e) {
			System.err.println("Cannot send logout command.");
		}

		this.username = "";
		closeAll();
		System.exit(0);
	}

	/**
	 * Sends a message from the client to the server.
	 *
	 * @param message the message to send.
	 */
	public void send(String message) {
		// if client wants to disconnect
		if (message.equals("quit")) {
			logout(this.username);
			return;
		}

		// push client message to server
		try {
			this.dos.writeUTF("msg " + message);
		} catch (Exception e) {
			System.err.println("Server has been shutdown.");
			this.globalTxa.appendText("[ above message has not been sent ]");
		}
	}

	/**
	 * Whispers a message to a user.
	 *
	 * @param toUser user to send whisper to.
	 * @param message the message to send.
	 */
	public void whisper(String toUser, String message) {
		// if client wants to disconnect
		if (message.equals("quit")) {
			System.out.println("\nClosing connection");
			logout(this.username);
			return;
		}

		// push client message to server
		try {
			this.dos.writeUTF("whsp " + toUser + " " + message);
		} catch (Exception e) {
			System.err.println("Server has been shutdown.");
			this.globalTxa.appendText("[ above message has not been sent ]");
		}
	}

	/**
	 * Reads all messages from server and prints them to the textarea.
	 *
	 * @param globalTxa textarea to print server messages to.
	 */
	public void readServerMsgs(TextArea globalTxa, TextArea whisperTxa,
			ListView<String> lstOnline, ListView<String> lstOffline,
			ComboBox<String> cmbWhisperTo) {
		this.globalTxa = globalTxa;
		this.whisperTxa = whisperTxa;
		this.lstOnline = lstOnline;
		this.lstOffline = lstOffline;
		this.cmbWhisperTo = cmbWhisperTo;

		// start new thread to read messages from server
		Thread t = new Thread() {
			@Override
			public void run() {
				loopMessages();
			}
		};

		t.start();
	}

	/**
	 * Loops infinitely to get messages.
	 */
	public void loopMessages() {
		String msg = "";
		this.lstOnline.getItems().add(this.username);

		// continually get input from server
		while (true) {
			try {
				msg = this.dis.readUTF();

				// ignore invalid messages
				if (!msg.contains(" ")) {
					System.out.println(msg);
					continue;
				}

				int idx = msg.indexOf(" ");
				String cmd = msg.substring(0, idx);
				String body = msg.substring(idx + 1);

				// ignore messages such as "login success"
				if (cmd.equals("login") || cmd.equals("logout")) {
					continue;
				}

				if (cmd.equals("online")) {
					idx = body.indexOf(" ");
					String user = body.substring(0, idx);

					// stage the change for update in main GUI thread
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							lstOffline.getItems().remove(user);
							lstOnline.getItems().add(user);
							cmbWhisperTo.getItems().add(user);
						}
					});

					Thread.sleep(500);
					continue;
				}

				if (cmd.equals("offline")) {
					idx = body.indexOf(" ");
					String user = body.substring(0, idx);

					// stage the change for update in main GUI thread
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							lstOnline.getItems().remove(user);
							lstOffline.getItems().add(user);
							cmbWhisperTo.getItems().remove(user);
						}
					});

					Thread.sleep(500);
					continue;
				}

				// put in different text areas for global and whispers
				if (cmd.equals("msg")) {
					this.globalTxa.appendText("\n" + body + "\n");
				} else if (cmd.equals("whsp")) {
					this.whisperTxa.appendText("\n" + body + "\n");
				}
			} catch (Exception e) {
				break;
			}
		}

		closeAll();
	}

	/**
	 * Closes all connections.
	 */
	public void closeAll() {
		try {
			this.client.close();
			this.dis.close();
			this.dos.close();
		} catch (IOException e) {
			System.err.println("Error closing connections: " + e);
		}
	}

	/**
	 * Adds a user listener to the list.
	 *
	 * @param ul the userlistener to add.
	 */
	public void addUserListener(UserListener ul) {
		this.userListeners.add(ul);
	}

	/**
	 * Removes a user listener from the list.
	 *
	 * @param ul the userlistener to remove.
	 */
	public void removeUserListener(UserListener ul) {
		this.userListeners.remove(ul);
	}

	/**
	 * Gets the current state of the DataOutputStream.
	 *
	 * @return current DataOutputStream.
	 */
	public DataOutputStream getDos() {
		return this.dos;
	}

	/**
	 * Sets the current username to the one specified.
	 *
	 * @param username the new username.
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Gets the current clients username.
	 *
	 * @return the username of the client.
	 */
	public String getUsername() {
		return this.username;
	}

	/**
	 * Returns the commands accepted by the server.
	 *
	 * @return accepted commands.
	 */
	public ArrayList<String> getCommands() {
		return this.commands;
	}
}
