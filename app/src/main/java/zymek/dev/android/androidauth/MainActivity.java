package zymek.dev.android.androidauth;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.observables.AndroidObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class MainActivity extends Activity implements Observer<String> {

	private final static String TAG = MainActivity.class.getSimpleName();
	AccountManager accountManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		accountManager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
		Log.d(TAG, "requeting token");
		AccountManagerFuture<Bundle> authTokenByFeatures = accountManager.getAuthTokenByFeatures(AuthConfig.ACCOUNT_TYPE, AuthConfig.TOKEN_TYPE, null, this, null, null, new AccountManagerCallback<Bundle>() {
			@Override
			public void run(final AccountManagerFuture<Bundle> future) {
				getToken(future);
			}
		}, null);

	}

	private void getToken(final AccountManagerFuture<Bundle> future) {
		Log.d(TAG, "binding activity with observable");
		AndroidObservable.bindActivity(this, Observable.create(new Observable.OnSubscribe<String>() {
			@Override
			public void call(Subscriber<? super String> subscriber) {
				try {
					Bundle result = future.getResult();
					Log.d(TAG, "bundle from future: "+ result);
					final String authtoken = result.getString(AccountManager.KEY_AUTHTOKEN);
					subscriber.onNext(authtoken);
				} catch (OperationCanceledException | IOException | AuthenticatorException e) {
					subscriber.onError(e);
				}
				subscriber.onCompleted();
			}
		}))
				.subscribeOn(Schedulers.newThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(this);
		Log.d(TAG, "subscribed");
	}

	@Override
	public void onCompleted() {
		Log.d(TAG, "onCompleted");
	}

	@Override
	public void onError(Throwable e) {
		Log.d(TAG, "onError:", e);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Error getting token")
				.setMessage(e.getMessage())
				.setNeutralButton(getString(android.R.string.ok), null);

		builder.create().show();
	}

	@Override
	public void onNext(String token) {
		Log.d(TAG, "onNext: "+ token);
	}
}
