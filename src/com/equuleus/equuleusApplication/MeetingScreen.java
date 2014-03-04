package com.equuleus.equuleusApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Fragment;
import android.net.ParseException;
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
	private String meetingTitle, meetingStart, meetingEnd;
	private int meetingDuration, meetingCounter = 0, contactCounter = 0, ID;
	private SlidingDrawer drawer;
	private ArrayList<String> contactArray = null, meetingArray = null;
	private DataStructure struct;
	private ArrayList<Date> myTimesArray, combinedTimesArray, contactsTimesArray;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		struct = new DataStructure();

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

		// TODO TEMPORARY EMAIL

		updateScrollViews();
		meetingConfirmButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				meetingDuration = Integer.parseInt(meetingDurationEditText
						.getText().toString());
				meetingTitle = meetingTitleEditText.getText().toString();
				calculateMeetingTime(struct);
				drawer.close();
			}

		});
		return v;
	}

	private void calculateMeetingTime(DataStructure contacts) {
		new updateTimesArrayList() {
			protected void onPostExecute(ArrayList<Date> result) {
				myTimesArray = result;
			}
		}.execute(1); // TEMP OUR ID TODO

		combinedTimesArray = myTimesArray;

		String email = contacts.pop();
		while (email != null) {
			new getID() {
				protected void onPostExecute(String result) {
					ID = Integer.parseInt(result);
				}
			}.execute(email);

			new updateTimesArrayList() {
				protected void onPostExecute(ArrayList<Date> result) {
					contactsTimesArray = result;
				}
			}.execute(ID);

			combinedTimesArray = updateCombinedTimesArray(combinedTimesArray,
					contactsTimesArray);
			email = contacts.pop();
		}

		struct = new DataStructure();


	}
	
	private Date pickTimeSlice(int duration)
	{
		Date returnDate = null;
		long testTime;
		for(int count = 0; count < combinedTimesArray.size(); count = count + 2)
		{
			testTime = combinedTimesArray.get(count + 1).getTime() - combinedTimesArray.get(count).getTime();
			testTime = testTime / 1000;
			testTime = testTime / 60;
			if(testTime > duration)
			{
				returnDate = combinedTimesArray.get(count);
			}
		}
		
		return returnDate;
	}

	private ArrayList<Date> updateCombinedTimesArray(ArrayList<Date> first,
			ArrayList<Date> second) {
		ArrayList<Date> returnList = new ArrayList<Date>();
		int firstIndex = 0, indexSecond = 0;
		Date startDateFirst, startDateSecond, endDateFirst, endDateSecond;
		
		outerLoop:
		for (int count = 0; count < first.size(); count = count + 2) {
			startDateFirst = first.get(count);
			endDateFirst = first.get(count+1);
			
			innerLoop:
			for (int countsecond = indexSecond; countsecond < second.size(); countsecond = countsecond + 2) {
				startDateSecond = second.get(countsecond);
				endDateSecond = second.get(countsecond + 1);
				if(startDateFirst.before(startDateSecond))
				{
					if(endDateFirst.before(startDateSecond))
					{
						returnList.add(startDateFirst);
						returnList.add(endDateFirst);
						indexSecond = indexSecond + countsecond;
						break innerLoop;
					}
					else
					{
						returnList.add(startDateFirst);
						returnList.add(endDateSecond);
						indexSecond = indexSecond + countsecond;
						break innerLoop;
					}
				}
				else
				{
					if(endDateSecond.before(startDateFirst))
					{
						returnList.add(startDateSecond);
						returnList.add(endDateSecond);
					}
					else
					{
						returnList.add(startDateSecond);
						returnList.add(endDateFirst);
						indexSecond = indexSecond + countsecond;
						break innerLoop;
					}
				}
			}
			
			if(indexSecond == second.size())
			{
				firstIndex = count;
				break outerLoop;
			}
		}
		
		if(firstIndex == first.size())
			for(int c = indexSecond; c < second.size(); c = c + 2)
			{
				returnList.add(second.get(c));
				returnList.add(second.get(c+1));
			}
		else
			for(int c = firstIndex; c < first.size(); c = c + 2)
			{
				returnList.add(first.get(c));
				returnList.add(first.get(c+1));
			}

		return returnList;
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

				// TODO Error Checking Here

			} catch (Exception e) {
				Log.e("log_tag", "Error Converting String " + e.toString());
			}
			return ID;
		}

	}

	private class updateTimesArrayList extends
			AsyncTask<Integer, Void, ArrayList<Date>> {
		@Override
		protected ArrayList<Date> doInBackground(Integer... ID) {
			ArrayList<Date> result = new ArrayList<Date>();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			InputStream in = null;
			try {
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(
						"http://equuleuscapstone.fulton.asu.edu/Unavail.php?user_id="
								+ ID);
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
					Date temp = simpleDateFormat.parse(line);
					result.add(temp);
					line = reader.readLine();
				}

			} catch (Exception e) {
				Log.e("log_tag", "Error Converting String " + e.toString());
			}

			return result;
		}

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
				for (int count = 0; count < meetingArray.size(); count = count + 2) {
					insertMeetingInScroll(meetingArray.get(count),
							meetingArray.get(count + 1));
				}
			}
		}.execute();
	}

	private void insertMeetingInScroll(String startDateTime, String endDateTime) {
		final View newMeetingRow = v.inflate(v.getContext(),
				R.layout.meeting_scroll_row, null);
		final TextView newMeetingTextView = (TextView) newMeetingRow
				.findViewById(R.id.meetingScrollTextView);
		newMeetingTextView.setText("From: " + startDateTime + " To: "
				+ endDateTime);
		ImageButton contactDeleteButton = (ImageButton) newMeetingRow
				.findViewById(R.id.meetingDeleteButton);
		contactDeleteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				meetingCounter--;
				deleteMeeting("TEMP"); // TODO THIS DOES NOT WORK
			}

		});

		meetingScrollLayout.addView(newMeetingRow, meetingCounter);
		meetingCounter++;
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
					String fName = line;
					String lName = reader.readLine();
					String email = reader.readLine();
					String startDateTime = reader.readLine();
					String endDateTime = reader.readLine();
					String timeStamp = reader.readLine();
					String description = reader.readLine();
					line = reader.readLine();
					result.add(startDateTime);
					result.add(endDateTime);
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
						if (contactCheckBox.isSelected()) {
							struct.add(newContactTextView.getText().toString());
						} else
							struct.delete(newContactTextView.getText()
									.toString());

					}

				});

		drawerScrollLayout.addView(newContactRow, contactCounter);
		contactCounter++;
	}

	private void deleteMeeting(final String meetingName) {
		// magic here to get Meeting ID
		int meetingId = 0; // temp!!
		new deleteMeetingConnection().execute(meetingId);
		updateMeetingScrollViews();

	}

	private class deleteMeetingConnection extends
			AsyncTask<Integer, Void, Void> {
		protected Void doInBackground(Integer... meetingId) {
			InputStream in = null;
			String deleteURL = "http://equuleuscapstone.fulton.asu.edu/DeleteMeeting.php?meeting_id="
					+ meetingId;
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

}
