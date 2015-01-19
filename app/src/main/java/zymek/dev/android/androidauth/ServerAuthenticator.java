package zymek.dev.android.androidauth;

/**
 * Created by Filip on 2015-01-19.
 */
public interface ServerAuthenticator {
	String signIn(String name, String password, String authTokenType);
}
