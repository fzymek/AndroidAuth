package zymek.dev.android.androidauth;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.observables.AndroidObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import zymek.dev.android.androidauth.R;

public class SignUpActivity extends AccountAuthenticatorActivity implements Observer<Intent> {

	private final static String TAG = SignUpActivity.class.getSimpleName();

	public static final String ACCOUNT_TYPE = "account_type";
	public static final String AUTH_TYPE = "auth_type";
	public static final String IS_ADDING_NEW_ACCOUNT = "is_adding_new_account";
	public static final String ACCOUNT_NAME = "account_name";
	public static final String ACCOUNT_PASSWORD = "account_password";
	public static final String ERROR_MESSAGE = "error_message";

	EditText email;
	EditText password;
	Button signup;
	ProgressBar progress;
	ServerAuthenticator serverAuthenticator;
	AccountManager accountManager;
	String mAuthTokenType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		assignViews();
		assignListeners();
		serverAuthenticator = new OfflineServerAuth();
		accountManager = AccountManager.get(this);
		mAuthTokenType = getIntent().getStringExtra(AUTH_TYPE);
	}

	private void assignViews() {
		email = (EditText) findViewById(R.id.email);
		password = (EditText) findViewById(R.id.password);
		signup = (Button) findViewById(R.id.email_sign_in_button);
		progress = (ProgressBar) findViewById(R.id.login_progress);
	}

	private void assignListeners() {
		signup.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				handleSignupClick();
			}
		});

		password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				switch (actionId) {
					case EditorInfo.IME_ACTION_DONE:
						handleSignupClick();
						return true;
					default:
						return false;
				}
			}
		});
	}

	private void handleSignupClick() {
		Log.d(TAG, "binding observable");
		AndroidObservable.bindActivity(this, Observable.create(new Observable.OnSubscribe<Intent>() {
			@Override
			public void call(Subscriber<? super Intent> subscriber) {
				String authtoken = null;
				Bundle data = new Bundle();
				try {
					Log.d(TAG, "getting auth token from server");
					authtoken = serverAuthenticator.signIn(email.getText().toString(), password.getText().toString(), AuthConfig.TOKEN_TYPE);
					Log.d(TAG, "auth token = " + authtoken);
					data.putString(AccountManager.KEY_ACCOUNT_NAME, email.getText().toString());
					data.putString(AccountManager.KEY_ACCOUNT_TYPE, AuthConfig.ACCOUNT_TYPE);
					data.putString(AccountManager.KEY_AUTHTOKEN, authtoken);
					data.putString(ACCOUNT_PASSWORD, password.getText().toString());
				} catch (Exception e) {
					Log.d(TAG, "error: ", e);
					subscriber.onError(e);
				}

				final Intent res = new Intent();
				res.putExtras(data);
				subscriber.onNext(res);
				subscriber.onCompleted();
			}
		}))
				.subscribeOn(Schedulers.newThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(this);
		Log.d(TAG, "observable subscrived");
	}

	@Override
	public void onCompleted() {
		Log.d(TAG, "onCompleted");
		finish();
	}

	@Override
	public void onError(Throwable e) {
		Log.d(TAG, "onError");
		setResult(RESULT_CANCELED);
	}

	@Override
	public void onNext(Intent intent) {
		Log.d(TAG, "onNext: "+ intent);
		String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
		String accountPassword = intent.getStringExtra(ACCOUNT_PASSWORD);
		final Account account = new Account(accountName, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));

		if (getIntent().getBooleanExtra(IS_ADDING_NEW_ACCOUNT, false)) {
			String authtoken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
			String authtokenType = mAuthTokenType;

			// Creating the account on the device and setting the auth token we got
			// (Not setting the auth token will cause another call to the server to authenticate the user)
			accountManager.addAccountExplicitly(account, accountPassword, null);
			accountManager.setAuthToken(account, authtokenType, authtoken);
		} else {
			accountManager.setPassword(account, accountPassword);
		}

		setAccountAuthenticatorResult(intent.getExtras());
		setResult(RESULT_OK, intent);
		finish();
	}
}
