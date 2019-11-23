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

public class KError {

	/**
	 * Prints a formatted exception message to standard output.
	 *
	 * @param message the message to display.
	 * @param e the exception that was thrown.
	 */
	public static void printError(String message, Exception e) {
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
}
