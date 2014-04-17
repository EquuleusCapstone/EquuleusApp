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
	private ArrayList<Date> myTimesArray, combinedTimesArray,
			contactsTimesArray;

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
				addMeeting(pickTimeSlice(meetingDuration));
				drawer.close();
			}

		});
		return v;
	}

	private void addMeeting(Date start) {
		new addMeetingConnection().execute(start);
	}

	private class addMeetingConnection extends AsyncTask<Date, Void, Void> {

		@Override
		protected Void doInBackground(Date... arg) {
			long temp = arg[0].getTime();
			Date endTime = new Date(temp + (meetingDuration * 60000));
			InputStream in = null;
			String addURL = "http://equuleuscapstone.fulton.asu.edu/AddMeeting.php?owner=1&start='"
					+ arg[0]
					+ "'&end='"
					+ endTime
					+ "'&description='"
					+ meetingTitle + "'";
			try {

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

	private Date pickTimeSlice(int duration) {
		Date returnDate = null;
		long testTime;
		for (int count = 0; count < combinedTimesArray.size(); count = count + 2) {
			testTime = combinedTimesArray.get(count + 1).getTime()
					- combinedTimesArray.get(count).getTime();
			testTime = testTime / 1000;
			testTime = testTime / 60;
			if (testTime > duration) {
				returnDate = combinedTimesArray.get(count);
			}
		}

		return returnDate;
	}

	private ArrayList<Date> updateCombinedTimesArray(ArrayList<Date> first,
			ArrayList<Date> second) {
		ArrayList<Date> returnList = new ArrayList<Date>();
		Date fStart, fEnd, sStart, sEnd;

		// runs the loop until one of the arraylists is empty
		while (first.size() > 0 && second.size() > 0) {
			fStart = first.get(0);
			fEnd = first.get(1);
			sStart = second.get(0);
			sEnd = second.get(1);

			// checks to see which beginning element comes first
			if (fStart.before(sStart)) { // fStart is smallest time
				returnList.add(fStart);
				// checks if second time block is contained
				// or overlaps first time block
				if (fEnd.after(sStart) || fEnd.equals(sStart)) {
					// loops to find all blocks that are contained within
					// fStart-fEnd in second array
					if (sEnd.before(fEnd)) {
						while (sEnd.before(fEnd)) {
							// remove irrelevant block from arraylist
							second.remove(0);
							second.remove(0);

							// store in next block from first array
							sStart = second.get(0);
							sEnd = second.get(1);

							// check to see if there is an overlap in new time
							// blocks
							if (fEnd.after(sStart) || fEnd.equals(sStart)) {
								if (sEnd.before(fEnd)) {
									// empty to go back into loop
								} else {
									// sEnd is bigger than fEnd
									returnList.add(sEnd);
									first.remove(0);
									first.remove(0);
									second.remove(0);
									second.remove(0);
									break;
								}

							} else {
								// fEnd is smaller than the new block
								returnList.add(fEnd);
								first.remove(0);
								first.remove(0);
								break;
							}
						}
					} else {
						// sEnd is larger than the block
						returnList.add(sEnd);
						first.remove(0);
						first.remove(0);
						second.remove(0);
						second.remove(0);
					}
				} else {
					// fEnd is smaller than the second block
					returnList.add(fEnd);
					first.remove(0);
					first.remove(0);
				}

			} else {
				// sStart is smallest time
				returnList.add(sStart);

				// checks if second time block ends after or same time as first
				// time block
				if (sEnd.after(fStart) || sEnd.equals(fStart)) {
					// loops to find all blocks that are contained within
					// fStart-fEnd in second array
					if (fEnd.before(sEnd)) {
						while (fEnd.before(sEnd)) {
							// remove irrelevant block from arraylist
							first.remove(0);
							first.remove(0);

							// store in next block from first array
							fStart = first.get(0);
							fEnd = first.get(1);

							// check to see if there is an overlap in new time
							// blocks
							if (sEnd.after(fStart) || sEnd.equals(fStart)) {
								if (fEnd.before(sEnd)) {
									// empty
								} else {
									// fEnd is bigger than sEnd
									returnList.add(fEnd);
									first.remove(0);
									first.remove(0);
									second.remove(0);
									second.remove(0);
									break;
								}

							} else {
								// sEnd is smaller than the first block
								returnList.add(sEnd);
								second.remove(0);
								second.remove(0);
							}
						}
					} else {
						// fEnd is larger than the second block
						returnList.add(fEnd);
						first.remove(0);
						first.remove(0);
						second.remove(0);
						second.remove(0);
					}
				} else {
					// sEnd is smaller than the first block
					returnList.add(sEnd);
					second.remove(0);
					second.remove(0);
				}
			}
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
					String meetingId = line;
					String fName = reader.readLine();
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
		contactCheckBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				if (contactCheckBox.isChecked()) {
					struct.add(newContactTextView.getText().toString());
				} else
					struct.delete(newContactTextView.getText().toString());
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
