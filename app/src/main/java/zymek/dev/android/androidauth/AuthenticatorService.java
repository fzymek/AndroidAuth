package zymek.dev.android.androidauth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AuthenticatorService extends Service {
	Authenticator authenticator;
	public AuthenticatorService() {
	}

	@Override
	public void onCreate() {
		super.onCreate();
		authenticator = new Authenticator(this);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return authenticator.getIBinder();
	}
}
