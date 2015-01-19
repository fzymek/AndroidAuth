package zymek.dev.android.androidauth;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by Filip on 2015-01-19.
 */
public class Authenticator extends AbstractAccountAuthenticator {

	private final static String TAG = Authenticator.class.getSimpleName();

	Context context;
	ServerAuthenticator serverAuth;

	public Authenticator(Context context) {
		super(context);
		this.context = context;
		serverAuth = new OfflineServerAuth();
	}

	@Override
	public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
		Log.d(TAG, "addAccount");
		final Intent intent = new Intent(context, SignUpActivity.class);
		intent.putExtra(SignUpActivity.ACCOUNT_TYPE, accountType);
		intent.putExtra(SignUpActivity.AUTH_TYPE, authTokenType);
		intent.putExtra(SignUpActivity.IS_ADDING_NEW_ACCOUNT, true);
		intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

		final Bundle bundle = new Bundle();
		bundle.putParcelable(AccountManager.KEY_INTENT, intent);
		return bundle;
	}

	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
		Log.d(TAG, "getAuthToken");
		// If the caller requested an authToken type we don't support, then
		// return an error
		if (!authTokenType.equals(AuthConfig.TOKEN_TYPE)) {
			Log.d(TAG, "Invalid token type");
			final Bundle result = new Bundle();
			result.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType");
			return result;
		}

		// Extract the username and password from the Account Manager, and ask
		// the server for an appropriate AuthToken.
		final AccountManager am = AccountManager.get(context);

		String authToken = am.peekAuthToken(account, authTokenType);
		Log.d(TAG, "peek token: "+ authToken);

		// Lets give another try to authenticate the user
		if (TextUtils.isEmpty(authToken)) {
			final String password = am.getPassword(account);
			if (password != null) {
				try {
					Log.d(TAG, "trying with server");
					authToken = serverAuth.signIn(account.name, password, authTokenType);
					Log.d(TAG, "token from server: "+ authToken);
				} catch (Exception e) {
					Log.d(TAG, "exception ", e);
					e.printStackTrace();
				}
			}
		}

		// If we get an authToken - we return it
		if (!TextUtils.isEmpty(authToken)) {
			Log.d(TAG, "token not empty: " + authToken);
			final Bundle result = new Bundle();
			result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
			result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
			result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
			return result;
		}

		// If we get here, then we couldn't access the user's password - so we
		// need to re-prompt them for their credentials. We do that by creating
		// an intent to display our AuthenticatorActivity.
		Log.d(TAG, "error, returingn account intent");
		final Intent intent = new Intent(context, SignUpActivity.class);
		intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
		intent.putExtra(SignUpActivity.ACCOUNT_TYPE, account.type);
		intent.putExtra(SignUpActivity.AUTH_TYPE, authTokenType);
		intent.putExtra(SignUpActivity.ACCOUNT_NAME, account.name);
		final Bundle bundle = new Bundle();
		bundle.putParcelable(AccountManager.KEY_INTENT, intent);
		return bundle;
	}

	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
		Log.d(TAG, "hasFeatures");
		final Bundle result = new Bundle();
		result.putBoolean(AuthConfig.HAS_FEATURES, false);
		return result;
	}

	@Override
	public String getAuthTokenLabel(String authTokenType) {
		return authTokenType;
	}

	@Override
	public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
		return null;
	}

	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
		return null;
	}

	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
		return null;
	}
}
