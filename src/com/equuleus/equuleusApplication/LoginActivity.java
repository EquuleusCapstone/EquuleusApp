package com.equuleus.equuleusApplication;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class LoginActivity extends Activity {

	private EditText emailField;
	private Button loginButton;

	private String lastName, firstName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_screen);

		emailField = (EditText) findViewById(R.id.Login);
		loginButton = (Button) findViewById(R.id.loginButton);

		final LayoutInflater factory = LayoutInflater.from(this);
		final View dialogView = factory.inflate(R.layout.dialoglayout, null);
		final EditText fName = (EditText) dialogView
				.findViewById(R.id.firstNameDialog);
		final EditText lName = (EditText) dialogView
				.findViewById(R.id.lastNameDialog);
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(dialogView);
		builder.setTitle("Email Not Found: Register?");
		builder.setPositiveButton(R.string.dialogConfirmButton,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						lastName = lName.getText().toString();
						firstName = fName.getText().toString();
						// TODO ADD This user to the database

					}
				});
		builder.setNegativeButton(R.string.dialogCancelButton,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

		loginButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				final String email = emailField.getText().toString();
				if (email
						.matches("[a-zA-Z0-9\\.]+@[a-zA-Z0-9\\-\\_\\.]+\\.[a-zA-Z0-9]{3}")) {

					new getID() {
						protected void onPostExecute(String contactID) {
							if (contactID.equals("")) {
								builder.show();
							} else {
								int id = Integer.parseInt(contactID);
								Intent intent = new Intent(getBaseContext(),
										MainActivity.class);
								intent.putExtra("userID", id);
								startActivity(intent);
							}
						}

					}.execute(email);
				}
				else
				{
					showErrorDialog("Invalid Email");
				}

			}

		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	private void showErrorDialog(String msg){
		AlertDialog.Builder err = new AlertDialog.Builder(this);
		err.setTitle(msg);
		err.setCancelable(false);
		err.setPositiveButton(R.string.dialogConfirmButton,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
		
		err.show();
	}
	
	private class getID extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {

			InputStream in = null;
			String idURL = "http://equuleuscapstone.fulton.asu.edu/getID.php?email='"
					+ params[0] + "'";
			String ID = null;
			try {
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(idURL);
				HttpResponse response = client.execute(post);
				HttpEntity entity = response.getEntity();
				in = entity.getContent();
			} catch (Exception e) {
				Log.e("log_tag", "Error In HTTP Connection" + e.toString());
			}

			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(in));
				String line = reader.readLine();
				ID = line;

				// TODO Make Sure Database Returned A Value

			} catch (Exception e) {
				Log.e("log_tag", "Error Converting String " + e.toString());
			}
			return ID;
		}

	}

}
