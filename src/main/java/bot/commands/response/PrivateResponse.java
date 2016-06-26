package bot.commands.response;

/**
 * Created by Dominic Hauton on 04/06/2016.
 *
 * Data class for a private message
 */
class PrivateResponse {
    private final String username;
    private final String message;

    public PrivateResponse(String username, String message) {
        this.username = username;
        this.message = message;
    }
}
