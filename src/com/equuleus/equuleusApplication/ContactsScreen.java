package com.equuleus.equuleusApplication;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TextView;

public class ContactsScreen extends Fragment {
	private TableLayout contactsScrollView;
	private Button addNewContactButton;
	private int contactCounter = 0;
	private View v;
	private String userEmail;
	private ArrayList<String> contactArray = null;
	private Fragment thisScreen = this;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.contacts_screen, container, false);
		contactsScrollView = (TableLayout) v
				.findViewById(R.id.contactsScrollTableLayout);
		addNewContactButton = (Button) v
				.findViewById(R.id.contactsAddNewButton);

		// TODO TEMPORARY EMAIL
		userEmail = "abc@gmail.com";

		// Populates Scroll View Initially
		updateScrollView();

		// Prompts an Alert Dialog For New Contact Email
		addNewContactButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				final EditText input = new EditText(getActivity());
				AlertDialog.Builder builder = new AlertDialog.Builder(
						getActivity());
				builder.setView(input);
				builder.setTitle("Add A New Contact");

				builder.setPositiveButton(R.string.dialogConfirmButton,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								// add new contact to database
								addContact(input.getText().toString());
							}
						});
				builder.setNegativeButton(R.string.dialogCancelButton,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
							}
						});

				builder.setMessage("Contacts Email");
				builder.show();
			}

		});

		return v;
	}

	// Updates Scroll View
	private void updateScrollView() {
		contactsScrollView.removeAllViews();
		contactCounter = 0;

		// Pulls New Contact List From Database
		new updateContactArrayList() {
			protected void onPostExecute(ArrayList<String> result) {
				contactArray = result;
				for (int count = 0; count < contactArray.size(); count++) {
					insertContactInScroll(contactArray.get(count));
				}
			}
		}.execute();
	}

	// Pulls Contact Information From Database Saves In Array List
	private class updateContactArrayList extends
			AsyncTask<Void, Void, ArrayList<String>> {
		@Override
		protected ArrayList<String> doInBackground(Void... params) {
			ArrayList<String> result = new ArrayList<String>();

			InputStream in = null;
			try {
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(
						"http://equuleuscapstone.fulton.asu.edu/contacts.php?user_id=1");
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

				while (!((line.charAt(0) + "").equals("}"))) {
					String userId = line;
					String email = reader.readLine();
					String fName = reader.readLine();
					String lName = reader.readLine();
					result.add(email);
					line = reader.readLine();
				}

			} catch (Exception e) {
				Log.e("log_tag", "Error Converting String " + e.toString());
			}
			return result;
		}

	}

	// Inserts A Single Contact Into Scroll Panel
	private void insertContactInScroll(String name) {
		final View newContactRow = v.inflate(v.getContext(),
				R.layout.contacts_scroll_row, null);
		final TextView newContactTextView = (TextView) newContactRow
				.findViewById(R.id.contactsScrollTextView);
		newContactTextView.setText(name);

		ImageButton contactDeleteButton = (ImageButton) newContactRow
				.findViewById(R.id.contactsDeleteButton);
		contactDeleteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				contactCounter--;
				contactsScrollView.removeAllViews();
				deleteContact(newContactTextView.getText().toString());
				Log.e("TAG", newContactTextView.getText().toString());
			}

		});

		contactsScrollView.addView(newContactRow, contactCounter);
		contactCounter++;
	}

	// Deletes A Contact From The Database
	private void deleteContact(final String name) {
		final String[] deleteArray = new String[2];

		// Gets The User's ID
		new getID() {
			protected void onPostExecute(String result) {
				deleteArray[0] = result;
			}
		}.execute(userEmail);

		// Gets The Friends ID
		new getID() {
			protected void onPostExecute(String result) {
				deleteArray[1] = result;
			}
		}.execute(name);

		// Passes Both IDs and Updates Scroll
		new deleteContactConnection().execute(deleteArray);	
		updateScrollView();

	}

	//Takes An Email And Returns That Emails User ID
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

				// TODO Error Checking Here

			} catch (Exception e) {
				Log.e("log_tag", "Error Converting String " + e.toString());
			}
			return ID;
		}

	}

	//Calls PHP Script To Delete A Contact
	private class deleteContactConnection extends
			AsyncTask<String[], Void, Void> {

		// TODO PHP Script Not Yet Built For This?
		@Override
		protected Void doInBackground(String[]... deleteArray) {
			InputStream in = null;
			String deleteURL = "http://equuleuscapstone.fulton.asu.edu/DeleteContact.php?user_id="
					+ deleteArray[0][0] + "&friend_id=" + deleteArray[0][1];
			try {
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(deleteURL);
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

				// TODO Error Checking Here

			} catch (Exception e) {
				Log.e("log_tag", "Error Converting String " + e.toString());
			}
			return null;
		}

	}

	//Adds A Contact 
	private void addContact(final String name) {
		final String[] addArray = new String[2];
		// Gets The User's ID

		addArray[0] = name;
		// Passes Both IDs and Updates Scroll
		new addContactConnection().execute(addArray);
		updateScrollView();

	}

	//Calls The PHP Script To Add A Contact
	private class addContactConnection extends AsyncTask<String[], Void, Void> {

		@Override
		protected Void doInBackground(String[]... addArray) {
			InputStream in = null;
			String addURL = "http://equuleuscapstone.fulton.asu.edu/AddContact.php?user_id=1&email='" + addArray[0][0] + "'";
			try {
				Log.e("TAG", addArray[0][0]);
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(addURL);
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

				// TODO Error Checking Here

			} catch (Exception e) {
				Log.e("log_tag", "Error Converting String " + e.toString());
			}
			return null;
		}

	}
}
