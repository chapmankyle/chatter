import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

/**
 * Server class to handle all requests and posts to server.
 *
 * Handles multiple requests from clients using ClientHandler class.
 * Each new client that is connected, is passed onto the ClientHandler class.
 *
 * @since 27 July 2019
 * @version 1.0.0
 * @author Kyle Chapman, Noah Atkins
 */

public class Server {

	// globals
	private final int PORT;

	private int numOnlineUsers;  /*<< number of users currently online */
	private int numOfflineUsers; /*<< number of users currently offline */

	private String currMsg;  /*<< most recent message */
	private String currUser; /*<< user who sent most recent message */

	private ArrayList<String> onlineUsers;  /*<< currently online users */
	private ArrayList<String> offlineUsers; /*<< currently offline users */
	private ArrayList<String> allUsers;     /*<< all users that have connected */

	private ArrayList<ClientHandler> clients;
	private SimpleDateFormat sdf;
	private Date date;

	// default constructor
	public Server(int port) {
		this.PORT = port;

		this.numOnlineUsers = 0;
		this.numOfflineUsers = 0;

		this.currMsg = "";
		this.currUser = "";

		this.onlineUsers = new ArrayList<>();
		this.offlineUsers = new ArrayList<>();
		this.allUsers = new ArrayList<>();
		this.clients = new ArrayList<>();
		sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");
	}

	// starts the server to listen on specified port
	public void start() throws IOException {
		ServerSocket server = new ServerSocket(this.PORT);
		this.date = new Date();
		String currDate = "[" + sdf.format(this.date) + "]";

		System.out.printf("%s Server currently accepting clients on port [\033[32m %d \033[0m]\n",
			currDate, this.PORT);

		// start new thread for checking if user types "quit" in server
		Thread q = new Thread() {
			@Override
			public void run() {
				checkQuit();
			}
		};

		q.start();

		// infinite loop for accepting clients
		while (true) {
			Socket client = null;

			try {
				// accept new client
				client = server.accept();

				// create new thread for each client
				ClientHandler clientThread = new ClientHandler(this, client);

				// add to client and start thread
				this.clients.add(clientThread);
				clientThread.start();
			} catch (Exception e) {
				// close both client and server
				client.close();
				server.close();

				System.err.println("Error (server): " + e);
			}
		}
	}

	/**
	 * Checks if the user wants to quit the server.
	 */
	public void checkQuit() {
		Scanner q = new Scanner(System.in);
		String line = q.nextLine();

		// loop until "quit" is typed
		while (!line.equals("quit")) {
			line = q.nextLine();
		}

		for (ClientHandler currClient : clients) {
			currClient.closeAll();
		}

		q.close();
		System.exit(0);
	}

	/**
	 * Returns if a given username is valid or not.
	 *
	 * @param username username to check.
	 *
	 * @return {@code true} if username is valid, {@code false} otherwise.
	 */
	public boolean isValidUsername(String username) {
		return (!this.onlineUsers.contains(username));
	}

	/**
	 * Returns if a specified user exists or not.
	 *
	 * @param username user to check if exists.
	 *
	 * @return {@code true} if user exists, {@code false} otherwise.
	 */
	public boolean userExists(String username) {
		return this.allUsers.contains(username);
	}

	/**
	 * Adds a user to the currently online users.
	 *
	 * @param username user to add.
	 */
	public void addUser(String username) {
		// add user if not already in list
		if (!this.onlineUsers.contains(username)) {
			this.onlineUsers.add(username);
			this.numOnlineUsers = this.onlineUsers.size();
		}

		// user went online
		if (this.offlineUsers.contains(username)) {
			this.offlineUsers.remove(username);
			this.numOfflineUsers = this.offlineUsers.size();
		}

		// add user to all users if not already in list
		if (!this.allUsers.contains(username)) {
			this.allUsers.add(username);
		}
	}

	/**
	 * Removes a user from the currently online users.
	 *
	 * @param client the client to remove.
	 */
	public void removeUser(ClientHandler client) {
		String username = client.getUsername();

		if (this.onlineUsers.contains(username)) {
			this.onlineUsers.remove(username);
			this.numOnlineUsers = this.onlineUsers.size();
		}

		// add user to offline list if not already in list
		if (!this.offlineUsers.contains(username)) {
			this.offlineUsers.add(username);
			this.numOfflineUsers = this.offlineUsers.size();
		}

		clients.remove(client);
	}

	/**
	 * Sets the most recently sent message.
	 *
	 * @param msg most recent message.
	 */
	public void setCurrMsg(String msg) {
		this.currMsg = msg;
	}

	/**
	 * Sets the username of the user who sent the most recent message.
	 *
	 * @param username username of user who sent most recent message.
	 */
	public void setCurrUser(String username) {
		this.currUser = username;
	}

	/**
	 * Gets the number of currently online users.
	 *
	 * @return number of users currently online.
	 */
	public int getNumOnlineUsers() {
		return this.numOnlineUsers;
	}

	/**
	 * Gets the number of currently offline users.
	 *
	 * @return number of users currently offline.
	 */
	public int getNumOfflineUsers() {
		return this.numOfflineUsers;
	}

	/**
	 * Gets the most recently sent message.
	 *
	 * @return most recent message.
	 */
	public String getCurrMsg() {
		return this.currMsg;
	}

	/**
	 * Gets the user who sent the most recent message.
	 *
	 * @return username of user who sent most recent message.
	 */
	public String getCurrUser() {
		return this.currUser;
	}

	/**
	 * Gets all the currently online users.
	 *
	 * @return all users currently online.
	 */
	public ArrayList<String> getOnlineUsers() {
		return this.onlineUsers;
	}

	/**
	 * Gets all the currently offline users.
	 *
	 * @return all users currently offline.
	 */
	public ArrayList<String> getOfflineUsers() {
		return this.offlineUsers;
	}

	/**
	 * Gets all the users that have connected to the server (even if currently
	 * disconnected from the server).
	 *
	 * @return all users that have connected to server.
	 */
	public ArrayList<String> getAllUsers() {
		return this.allUsers;
	}

	/**
	 * Gets all client threads.
	 *
	 * @return all client threads connected to server.
	 */
	public ArrayList<ClientHandler> getClients() {
		return this.clients;
	}

	/**
	 * Main function.
	 *
	 * @param args the command-line arguments.
	 * @throws IOException when server cannot be started.
	 */
	public static void main(String[] args) throws IOException {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(200);

					SimpleDateFormat sd = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");
					Date dt = new Date();
					String currDate = "[" + sd.format(dt) + "]";

					System.out.printf("\n%s Server shutting down ...\n\n", currDate);
				} catch (InterruptedException e) {
					System.err.println("Error in shutdown hook : " + e);
				}
			}
		});

		// connect and start server
		Server server = new Server(8080);
		server.start();
	}
}
