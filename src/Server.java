/**
 * Chatter is a peer-to-peer communication program written in Java.
 * Copyright (C) 2019 Kyle Chapman
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;

public class Server {

	// globals
	private static ServerSocket server;

	private static HashSet<String> onlineUsers;
	private static HashSet<String> offlineUsers;
	private static HashSet<String> seenUsers;

	private static HashSet<ClientHandler> clients;

	private int port;
	private int numOnline;
	private int numOffline;

	/**
	 * Constructor for the server.
	 *
	 * @param port the port on which to listen.
	 */
	public Server(int port) {
		this.port = port;
		server = null;

		onlineUsers = new HashSet<>();
		offlineUsers = new HashSet<>();
		seenUsers = new HashSet<>();

		clients = new HashSet<>();

		this.numOnline = 0;
		this.numOffline = 0;
	}

	/**
	 * Start the server and listen for clients connecting.
	 */
	public void start() {
		// attempt to start server
		try {
			server = new ServerSocket(this.port);
		} catch (IOException e) {
			KError.printError("Unable to start the server.", e);
			quit();
		}

		System.out.println("\nServer started \033[32msuccessfully\033[0m!");
		System.out.printf("Now accepting clients on port \033[33m%d\033[0m\n\n",
			this.port);

		// check for 'quit' signal
		Thread q = new Thread() {
			@Override
			public void run() {
				checkQuit();
			}
		};
		q.start();

		Socket client = null;

		// wait for a client to connect
		while (true) {
			try {
				// accept the new client
				client = server.accept();

				ClientHandler ch = new ClientHandler(this, client);

				// add to online clients and start handler
				clients.add(ch);
				ch.start();
			} catch (IOException e) {
				KError.printError("Error trying to connect client.", e);
			}
		}
	}

	/**
	 * Checks for 'quit' signal to be typed.
	 */
	private void checkQuit() {
		Scanner scan = new Scanner(System.in);
		String text = scan.nextLine();

		// loop until 'quit' is typed
		while (!text.equals("quit")) {
			text = scan.nextLine();
		}

		// close all connections
		for (ClientHandler ch : clients) {
			ch.close();
		}

		// close scanner and exit
		scan.close();
		quit();
	}

	/**
	 * Quits the server and closes all open connections.
	 */
	private void quit() {
		// perform clean-up then exit
		try {
			if (server != null) {
				server.close();
			}
		} catch (IOException e) {
			KError.printError("Unable to close all connections.", e);
		}

		System.out.println("\nGoodbye!");
		System.exit(0);
	}

	/**
	 *    _____  _    _ ____  _      _____ _____
	 *   |  __ \| |  | |  _ \| |    |_   _/ ____|
	 *   | |__) | |  | | |_) | |      | || |
	 *   |  ___/| |  | |  _ <| |      | || |
	 *   | |    | |__| | |_) | |____ _| || |____
	 *   |_|     \____/|____/|______|_____\_____|
	 */

	/**
	 * Checks whether or not a user is currently online.
	 *
	 * @param username the username to check.
	 * @return {@code true} if username is online, {@code false} otherwise.
	 */
	public boolean isUserOnline(String username) {
		boolean online = false;

		// ensure thread-safe read
		synchronized (onlineUsers) {
			online = onlineUsers.contains(username);
		}

		return online;
	}

	/**
	 * Checks whether or not a user has been seen by the server.
	 *
	 * @param username the username of the user.
	 * @return {@code true} if the user has been seen before, {@code false} otherwise.
	 */
	public boolean hasUserBeenSeen(String username) {
		boolean seen = false;

		// ensure thread-safe read
		synchronized (seenUsers) {
			seen = seenUsers.contains(username);
		}

		return seen;
	}

	/**
	 * Updates user status to online.
	 *
	 * @param username the user that is online.
	 */
	public void addUser(String username) {
		// only add user if not already online
		synchronized (onlineUsers) {
			if (!onlineUsers.contains(username)) {
				onlineUsers.add(username);
				this.numOnline = onlineUsers.size();
			}
		}

		// remove user from offline list
		synchronized (offlineUsers) {
			if (offlineUsers.contains(username)) {
				offlineUsers.remove(username);
				this.numOffline = offlineUsers.size();
			}
		}

		// add user to all users if not already in list
		synchronized (seenUsers) {
			if (!seenUsers.contains(username)) {
				seenUsers.add(username);
			}
		}
	}

	/**
	 * Adds a user when the ClientHandler is specified.
	 *
	 * @param user the client handler for the user.
	 */
	public void addUser(ClientHandler user) {
		addUser(user.getUsername());
	}

	/**
	 * Updates user status to offline.
	 *
	 * @param user the user that is offline.
	 */
	public void removeUser(ClientHandler user) {
		// get client username
		String username = user.getUsername();

		// remove user from online user list
		synchronized (onlineUsers) {
			if (onlineUsers.contains(username)) {
				onlineUsers.remove(username);
				this.numOnline = onlineUsers.size();
			}
		}

		// add user to offline list if not already in list
		synchronized (offlineUsers) {
			if (!offlineUsers.contains(username)) {
				offlineUsers.add(username);
				this.numOffline = offlineUsers.size();
			}
		}

		// remove from ClientHandler list
		synchronized (clients) {
			clients.remove(user);
		}
	}

	/**
	 *     _____ ______ _______
	 *    / ____|  ____|__   __|
	 *   | |  __| |__     | |
	 *   | | |_ |  __|    | |
	 *   | |__| | |____   | |
	 *    \_____|______|  |_|
	 */

	/**
	 * Gets the number of currently online users.
	 *
	 * @return number of users currently online.
	 */
	public int getNumOnline() {
		return this.numOnline;
	}

	/**
	 * Gets the number of currently offline users.
	 *
	 * @return number of users currently offline.
	 */
	public int getNumOffline() {
		return this.numOffline;
	}

	/**
	 * Gets the total number of users either online or offline.
	 *
	 * @return the total number of users either online or offline.
	 */
	public int getTotalUserCount() {
		return this.numOnline + this.numOffline;
	}

	/**
	 * Gets all the currently online users.
	 *
	 * @return all users currently online.
	 */
	public HashSet<String> getOnlineUsers() {
		return onlineUsers;
	}

	/**
	 * Gets all the currently offline users.
	 *
	 * @return all users currently offline.
	 */
	public HashSet<String> getOfflineUsers() {
		return offlineUsers;
	}

	/**
	 * Gets all the users that have been seen by the server.
	 *
	 * @return all users that have been seen by the server.
	 */
	public HashSet<String> getSeenUsers() {
		return seenUsers;
	}

	/**
	 * Gets the currently connected clients.
	 *
	 * @return the list of all the currently connected clients.
	 */
	public HashSet<ClientHandler> getClients() {
		return clients;
	}

	/**
	 * Main function to run the server.
	 *
	 * @param args the command-line arguments.
	 */
	public static void main(String[] args) {
		Server serv = new Server(8080);
		serv.start();
	}
}
