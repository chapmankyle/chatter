/**
 * Chatter is a peer-to-peer communication program written in Java. Copyright
 * (C) 2019 Kyle Chapman
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <https://www.gnu.org/licenses/>.
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler extends Thread {

	// globals
	private static Server server;

	private String username;
	private Socket client;

	private DataInputStream dis;
	private DataOutputStream dos;

	/**
	 * Constructor for the handling of a client.
	 *
	 * @param serv   the originating server.
	 * @param client the client that needs to be handled.
	 */
	public ClientHandler(Server serv, Socket client) {
		server = serv;
		this.client = client;

		this.username = "";
		getDataStreams();
	}

	/**
	 * Gets the data streams from the given client.
	 */
	private void getDataStreams() {
		try {
			this.dos = new DataOutputStream(this.client.getOutputStream());
			this.dis = new DataInputStream(this.client.getInputStream());
		} catch (IOException e) {
			KError.printError("Unable to obtain data streams.", e);
		}
	}

	/**
	 * Run method inherited from Thread.
	 */
	@Override
	public void run() {

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
	 * Closes all client connections.
	 */
	public void close() {
		try {
			if (client != null) {
				client.close();
			}

			if (this.dis != null) {
				this.dis.close();
			}

			if (this.dos != null) {
				this.dos.close();
			}
		} catch (IOException e) {
			KError.printError("Unable to close all connections for '" +
				this.username + "'.", e);
		}
	}

	/**
	 * Gets the username of the current client.
	 *
	 * @return the username of the handled client.
	 */
	public String getUsername() {
		return this.username;
	}
}
