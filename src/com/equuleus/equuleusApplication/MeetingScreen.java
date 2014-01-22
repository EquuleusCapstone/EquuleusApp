package com.equuleus.equuleusApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SlidingDrawer;
import android.widget.TableLayout;
import android.widget.TextView;

public class MeetingScreen extends Fragment {
	private View v;
	private TableLayout meetingScrollLayout, drawerScrollLayout;
	private Button meetingConfirmButton;
	private EditText meetingDurationEditText;
	private EditText meetingTitleEditText;
	private String meetingTitle;
	private int meetingDuration, meetingCounter = 0, contactCounter = 0,
			contactUseCounter = 0;
	private HttpURLConnection connection;
	private SlidingDrawer drawer;
	private ArrayList<String> contactArray = null, meetingArray = null,
			contactsInUseArray;
	private String userEmail;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		v = inflater.inflate(R.layout.meeting_screen, null);
		drawer = (SlidingDrawer) v.findViewById(R.id.meetingSlidingDrawer);
		meetingScrollLayout = (TableLayout) v
				.findViewById(R.id.meetingScrollTableLayout);
		drawerScrollLayout = (TableLayout) v
				.findViewById(R.id.meetingDrawerScrollTableLayout);
		meetingConfirmButton = (Button) v
				.findViewById(R.id.meetingDrawerConfirmButton);
		meetingDurationEditText = (EditText) v
				.findViewById(R.id.meetingDrawerDurationEditText);
		meetingTitleEditText = (EditText) v
				.findViewById(R.id.meetingDrawerTitleEditText);

		contactsInUseArray = new ArrayList<String>();
		// TODO TEMPORARY EMAIL
		userEmail = "abc@gmail.com";
		updateScrollViews();
		meetingConfirmButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				meetingTitle = meetingTitleEditText.getText().toString();
				meetingDuration = Integer.parseInt(meetingDurationEditText
						.getText().toString());

				final View newMeetingRow = v.inflate(v.getContext(),
						R.layout.meeting_scroll_row, null);
				TextView newMeetingTextView = (TextView) newMeetingRow
						.findViewById(R.id.meetingScrollTextView);
				newMeetingTextView.setText(meetingTitle + "-" + meetingDuration
						+ " minutes ");

				ImageButton meetingDeleteButton = (ImageButton) newMeetingRow
						.findViewById(R.id.meetingDeleteButton);
				// Deletes The Meeting From View and From Database
				meetingDeleteButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						meetingCounter--;
						// TODO Figure Out Delete URL
						updateScrollViews();
					}

				});

				meetingScrollLayout.addView(newMeetingRow, meetingCounter);
				meetingCounter++;
				drawer.close();
			}

		});
		return v;
	}

	private void updateScrollViews() {
		updateMeetingScrollViews();
		updateContactsScrollViews();
	}

	private void updateMeetingScrollViews() {
		meetingScrollLayout.removeAllViews();
		meetingCounter = 0;

		// Pulls New Meeting List From Database
		new updateMeetingArrayList() {
			protected void onPostExecute(ArrayList<String> result) {
				meetingArray = result;
				for (int count = 0; count < meetingArray.size(); count++) {
					//TODO MAGIC HERE
					insertMeetingInScroll(null, null, null);
				}
			}
		}.execute();
	}
	
	private void insertMeetingInScroll(String name, String length, ArrayList<String> contacts){
		//TODO MAGIC HERE
	}

	// Pulls Contact Information From Database Saves In Array List
	private class updateMeetingArrayList extends
			AsyncTask<Void, Void, ArrayList<String>> {
		@Override
		protected ArrayList<String> doInBackground(Void... params) {
			ArrayList<String> result = new ArrayList<String>();
			InputStream in = null;
			try {
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(
						"http://equuleuscapstone.fulton.asu.edu/GetMeetings.php?user_id=1");
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
					// TODO NOT YET IMPLEMENTED CANT PARSE
				}

			} catch (Exception e) {
				Log.e("log_tag", "Error Converting String " + e.toString());
			}
			return result;
		}

	}

	private void updateContactsScrollViews() {
		drawerScrollLayout.removeAllViews();
		contactCounter = 0;
		contactUseCounter = 0;

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
		Log.e("Insert", name);
		final int index = contactUseCounter;
		contactUseCounter++;
		final View newContactRow = v.inflate(v.getContext(),
				R.layout.meeting_scroll_contacts_row, null);
		final TextView newContactTextView = (TextView) newContactRow
				.findViewById(R.id.meetingScrollTextView);
		newContactTextView.setText(name);

		final CheckBox contactCheckBox = (CheckBox) newContactRow
				.findViewById(R.id.meetingCheckBox);
		contactCheckBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean arg1) {
						if (contactCheckBox.isChecked())
							contactsInUseArray.add(index, newContactTextView
									.getText().toString());
						else
							contactsInUseArray.remove(index);
					}

				});

		drawerScrollLayout.addView(newContactRow, contactCounter);
		contactCounter++;
	}

}
