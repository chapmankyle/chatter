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

public enum Command {
	// enum values
	USERS("users"), LOGIN("login"), LOGOUT("logout"), MSG("msg"), WHSP("whsp"),
	INVALID;

	// enum key
	private String key;

	/**
	 * Constructor for when no key given.
	 */
	private Command() {
		this.key = "";
	}

	/**
	 * Constructor for when key is given.
	 *
	 * @param key the key to assign enum value to.
	 */
	private Command(String key) {
		this.key = key;
	}

	/**
	 * Gets the key linking to the enum value.
	 *
	 * @return the key linked to the enum value.
	 */
	public String getKey() {
		return this.key;
	}

	/**
	 * Gets the value of the enum associated with the given key.
	 *
	 * @param key the key of the enum to find the value of.
	 * @return {@code INVALID} if no value found for key, otherwise the value associated
	 * with the key.
	 */
	public Command getValue(String key) {
		Command value = INVALID;

		// find value for key
		for (Command cmd : Command.values()) {
			if (cmd.getKey().equals(key)) {
				value = cmd;
				break;
			}
		}

		return value;
	}
}
