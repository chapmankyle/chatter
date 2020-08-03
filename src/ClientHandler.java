import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

/**
 * ClientHandler class to handle all client connections to the server.
 *
 * Each new client that connects to the server is started as a new Thread
 * and processed individually.
 *
 * @since 27 July 2019
 * @version 1.0.0
 * @author Kyle Chapman, Noah Atkins
 */

public class ClientHandler extends Thread {

	// globals
	private final Socket client;
	private final Server server;

	private String username;
	private DataInputStream dis;
	private DataOutputStream dos;

	private ArrayList<String> commands;

	// default constructor
	public ClientHandler(Server server, Socket client) {
		this.server = server;
		this.client = client;
		this.username = "";
		this.commands = new ArrayList<>();

		// add all commands to list
		this.commands.add("login");
		this.commands.add("logout");
		this.commands.add("msg");
		this.commands.add("whsp");

		// getting data streams
		try {
			this.dis = new DataInputStream(this.client.getInputStream());
			this.dos = new DataOutputStream(this.client.getOutputStream());
		} catch (Exception e) {
			System.err.println("Error: " + e);
		}
	}

	// run function for overriding default Thread.run()
	@Override
	public void run() {
		// allow client to keep sending messages
		while (true) {
			try {
				// get a message
				String recv = this.dis.readUTF();

				if (!recv.contains(" ")) {
					System.out.println("Invalid message");
					continue;
				}

				int idx = recv.indexOf(" ");
				String cmd = recv.substring(0, idx);
				String body = recv.substring(idx + 1);

				if (!commands.contains(cmd)) {
					System.err.println("Invalid command");
					continue;
				}

				// handle different tokens
				switch (cmd) {
					case "login":
						login(body);
						break;
					case "logout":
						logout(body);
						break;
					case "msg":
						message(body);
						break;
					case "whsp":
						whisper(body);
						break;
				}

				if (cmd.equals("logout")) {
					break;
				}

				// set most recent message and user who sent message
			} catch (IOException e) {
				break;
			}
		}

		// close connections
		try {
			this.client.close();
			this.dis.close();
			this.dos.close();
		} catch (IOException e) {
			System.err.println("Error (clienthandler): " + e);
		}
	}

	/**
	 * Allows user to log in.
	 *
	 * @param body the rest of the message.
	 */
	public void login(String body) {
		String usr = body.trim();

		// check if user with same name is not online
		if (this.server.getOnlineUsers().contains(usr)) {
			try {
				this.dos.writeUTF("login failure");
			} catch (IOException e) {
				System.err.println("Could not send failure.\nError: " + e);
			}

			return;
		}

		// if username is unique, log user in
		try {
			this.dos.writeUTF("login success");
		} catch (IOException e) {
			System.err.println("Could not send success.\nError: " + e);
			return;
		}

		this.username = usr;
		this.server.addUser(this.username);

		System.out.println("\n-> \033[32m" + this.username + "\033[0m has joined the party!");
		System.out.println(this.server.getNumOnlineUsers() + " users currently online.\n");

		// get all clients connected
		ArrayList<ClientHandler> clients = this.server.getClients();

		String msg = "";
		String tail = " is online!";

		// send current user message saying other users are online
		for (ClientHandler currClient : clients) {
			String ccUsername = currClient.getUsername();

			if (this.username.equals(ccUsername) || ccUsername.equals("")) {
				continue;
			}

			msg = "online " + ccUsername + tail;
			sendToClient(msg);
		}

		// send all other online users the message that the current user is online
		msg = "online " + this.username + tail;
		for (ClientHandler currClient : clients) {
			if (this.username.equals(currClient.getUsername())) {
				continue;
			}

			currClient.sendToClient(msg);
		}
	}

	/**
	 * Allows user to log out.
	 *
	 * @param body the rest of the message.
	 */
	public void logout(String body) {
		String usr = body.trim();

		try {
			this.dos.writeUTF("logout success");
		} catch (IOException e) {
			System.err.println("Could not send logout success.");
			return;
		}

		this.server.removeUser(this);
		System.out.println("\n\033[31m" + usr + " has disconnected.\033[0m\n");
		this.username = "";

		// get all online clients
		ArrayList<ClientHandler> clients = this.server.getClients();

		String msg = "";
		String tail = " has disconnected :(";

		// send all other online users the message that the current user is offline
		msg = "offline " + usr + tail;
		for (ClientHandler currClient : clients) {
			if (usr.equals(currClient.getUsername())) {
				continue;
			}

			currClient.sendToClient(msg);
		}
	}

	/**
	 * Shows any message in global chat sent by the client.
	 *
	 * @param body the rest of the message.
	 */
	public void message(String body) {
		String msg = body.trim();

		this.server.setCurrMsg(msg);
		this.server.setCurrUser(this.username);

		System.out.println(this.username + " : " + msg);

		// get all connected clients
		ArrayList<ClientHandler> clients = this.server.getClients();

		String fullMsg = "";

		// send all other clients message that current user has typed
		fullMsg = "msg " + this.username + " : " + msg;
		for (ClientHandler currClient : clients) {
			if (this.username.equals(currClient.getUsername())) {
				continue;
			}

			currClient.sendToClient(fullMsg);
		}
	}

	/**
	 * Shows a whisper from the current user.
	 *
	 * @param body the rest of the message.
	 */
	public void whisper(String body) {
		int idx = body.indexOf(" ");
		String toUser = body.substring(0, idx);
		String message = body.substring(idx + 1);

		if (this.username.equals(toUser)) {
			System.out.println("\033[35m" + this.username +
				" tried to whisper to themself. Not allowed.\033[0m");
			return;
		}

		if (!this.server.getOnlineUsers().contains(toUser)) {
			System.out.println("\033[35mwhisper (" + this.username + " -> " + toUser +
				") unsuccessful; user not online.\033[0m");
			return;
		}

		System.out.println("\033[35m" + this.username + " -> " + toUser +
			" : \033[0m" + message);

		// get all connected clients
		ArrayList<ClientHandler> clients = this.server.getClients();

		String msg = "";

		// send all other clients message that current user has typed
		msg = "whsp " + this.username + " : " + message;
		for (ClientHandler currClient : clients) {
			if (!toUser.equals(currClient.getUsername())) {
				continue;
			}

			currClient.sendToClient(msg);
		}
	}

	/**
	 * Sends a message to the client.
	 *
	 * @param msg the message to send.
	 */
	public void sendToClient(String msg) {
		if (this.username.equals("")) {
			return;
		}

		try {
			this.dos.writeUTF(msg);
		} catch (IOException e) {
			System.err.println("Cannot send to client.\nError: " + e);
		}
	}

	/**
	 * Gets the current client's username.
	 *
	 * @return client's username.
	 */
	public String getUsername() {
		return this.username;
	}

	public void closeAll() {
		try {
			this.client.close();
			this.dis.close();
			this.dos.close();
		} catch (IOException e) {
			System.err.println("Error closing connections: " + e);
		}
	}
}
