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
			error("Unable to start the server.", e);
			quit();
		}

		System.out.println("\nServer started \033[32msuccessfully\033[0m!");
		System.out.printf("Now accepting clients on port \033[33m%d\033[0m\n\n",
			this.port);

		// client
		Socket client = null;

		// wait for a client to connect
		while (true) {
			try {
				// accept a client
				client = server.accept();

				ClientHandler ch = new ClientHandler(this, client);

				// add to online clients
				clients.add(ch);
				ch.start();
			} catch (IOException e) {
				error("Error trying to connect client.", e);
			}
		}
	}



	/**
	 * Prints a formatted exception message to standard output.
	 *
	 * @param message the message to display.
	 * @param e the exception that was thrown.
	 */
	private void error(String message, Exception e) {
		String errorMessage = e.toString();
		String cause = "";
		String error = "";

		if (!errorMessage.contains(":")) {
			System.out.println("\n\033[33m" + message + "\033[0m\n" + errorMessage + "\n");
			return;
		}

		int idx = errorMessage.indexOf(":");
		cause = errorMessage.substring(0, idx);
		error = errorMessage.substring(idx + 2);

		System.out.println("\n[\033[31m" + cause + "\033[0m] " + "\033[33m" + message + "\033[0m\n" +
			error + "\n");
	}

	/**
	 * Quits the server and closes all open connections.
	 */
	public void quit() {
		// perform clean-up then exit
		try {
			if (server != null) {
				server.close();
			}
		} catch (IOException e) {
			error("Unable to close all connections.", e);
		}

		System.out.println("\nGoodbye!");
		System.exit(0);
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
