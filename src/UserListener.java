/**
 * UserListener interface to listen for any users that login or logout of the
 * server.
 */

public interface UserListener {
	/**
	 * Callback when a user has logged in.
	 *
	 * @param username the username of the user that just logged in.
	 */
	public void online(String username);

	/**
	 * Callback when a user has logged out.
	 *
	 * @param username the username of the user that just logged out.
	 */
	public void offline(String username);
}
