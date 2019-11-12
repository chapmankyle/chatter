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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client {

	// globals
	private String username;
	private String hostname;

	private int port;

	private Socket client;

	private DataInputStream dis;
	private DataOutputStream dos;

	/**
	 * Constructor for a client.
	 *
	 * @param hostname the hostname to connect to.
	 * @param port the port to connect with
	 */
	public Client(String hostname, int port) {
		this.hostname = hostname;
		this.port = port;

		this.username = "";

		this.client = null;
		this.dis = null;
		this.dos = null;

		setup();
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
	 * Sets up the client and all relevant streams.
	 *
	 * @return {@code true} if setup is successful, {@code false} otherwise.
	 */
	public boolean setup() {
		// create client socket
		try {
			this.client = new Socket(this.hostname, this.port);
		} catch (IOException e) {
			error("Unable to connect to server.", e);
			return false;
		}

		// get input and output streams
		try {
			this.dos = new DataOutputStream(this.client.getOutputStream());
			this.dis = new DataInputStream(this.client.getInputStream());
		} catch (IOException e) {
			error("Unable to obtain data streams.", e);
			return false;
		}

		return true;
	}

	/**
	 * Attemps to log a specified user in.
	 *
	 * @param user the user to log in.
	 * @return {@code true} if user is accepted, {@code false} otherwise.
	 */
	public boolean login(String user) {
		user = user.trim();

		// check for no username
		if (user.length() < 1) {
			return false;
		}

		// send login packet
		try {
			this.dos.writeUTF("login#" + user);
			this.dos.flush();
		} catch (IOException e) {
			error("Unable to send login packet.", e);
			return false;
		}

		String response = "";

		// attempt to read response from server
		try {
			response = this.dis.readUTF().trim();
		} catch (IOException e) {
			error("Unable to read server response.", e);
			return false;
		}

		// login unsuccessful
		if (!response.equals("login#s")) {
			return false;
		}

		// login successful
		this.username = user;
		return true;
	}

	/**
	 *    _____  _____  _______      __  _______ ______
	 *   |  __ \|  __ \|_   _\ \    / /\|__   __|  ____|
	 *   | |__) | |__) | | |  \ \  / /  \  | |  | |__
	 *   |  ___/|  _  /  | |   \ \/ / /\ \ | |  |  __|
	 *   | |    | | \ \ _| |_   \  / ____ \| |  | |____
	 *   |_|    |_|  \_\_____|   \/_/    \_\_|  |______|
	 */

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
	 * Main function.
	 *
	 * @param args the command-line arguments.
	 */
	public static void main(String[] args) {
		Client cli = new Client("localhost", 8080);
	}
}
