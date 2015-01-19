package zymek.dev.android.androidauth;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * Created by Filip on 2015-01-19.
 */
public class OfflineServerAuth implements ServerAuthenticator {
	@Override
	public String signIn(String name, String password, String authTokenType) {
		return name + "(" + authTokenType + ")-" + new BigInteger(256, new SecureRandom()).toString(32);
	}
}
