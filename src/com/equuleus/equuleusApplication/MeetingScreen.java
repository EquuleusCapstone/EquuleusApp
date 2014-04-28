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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Fragment;
import android.net.ParseException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateFormat;
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
	private ArrayList<String> contactArray = null, meetingArray = null, inviteArray = null;
	private DataStructure struct;
	private ArrayList<Date> myTimesArray, combinedTimesArray,
			contactsTimesArray;

	private int userid;
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		userid = getActivity().getIntent().getExtras().getInt("userID");
		
		myTimesArray = new ArrayList<Date>();
		contactsTimesArray = new ArrayList<Date>();
		combinedTimesArray = new ArrayList<Date>();
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

		updateScrollViews();
		meetingConfirmButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//TODO Check If Meeting Duration Can Be Parsed To Integer / Is Filled Out
				meetingDuration = Integer.parseInt(meetingDurationEditText
						.getText().toString());
				
				//TODO Check If Title Is Filled Out
				meetingTitle = meetingTitleEditText.getText().toString();
				calculateMeetingTime(struct);
				meetingDurationEditText.setText("");
				meetingTitleEditText.setText("");
				drawer.close();
				updateContactsScrollViews();
				
			}

		});
		return v;
	}

	private void addMeeting(Date start) {
		new addMeetingConnection().execute(start);
		updateMeetingScrollViews();
	}

	private class addMeetingConnection extends AsyncTask<Date, Void, Void> {

		@Override
		protected Void doInBackground(Date... arg) {
			long temp = arg[0].getTime();
			Date endTime = new Date(temp + (meetingDuration * 60000));

			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
					"yyyy-MM-dd%20HH:mm:ss");

			InputStream in = null;
			String addURL = "http://equuleuscapstone.fulton.asu.edu/AddMeeting.php?owner=1&start='"
					+ simpleDateFormat.format(arg[0])
					+ "'&end='"
					+ simpleDateFormat.format(endTime)
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
			return null;
		}

	}

	private void calculateMeetingTime(DataStructure in) {
		final DataStructure contacts = in;
		final int max = contacts.size();
		for(int k = 0; k < max; k++)
		{
			inviteArray.set(k, contacts.pop());
		}
		new updateTimesArrayList() {
			protected void onPostExecute(final ArrayList<Date> myTimes) {
				combinedTimesArray = myTimes;
				Log.e("SIZEMAX", max + "");
				for (int k = 0; k < max; k++) {
					new getID() {
						protected void onPostExecute(String contactID) {
							new updateTimesArrayList() {
								protected void onPostExecute(
										final ArrayList<Date> result2) {
									combinedTimesArray = updateCombinedTimesArray(
											combinedTimesArray, result2);
									for (int i = 0; i < combinedTimesArray
											.size(); i++)
										Log.e("ENTRY " + i, ": "
												+ combinedTimesArray.get(i)
														.toString());
								}
							}.execute(Integer.parseInt(contactID));
						}
					}.execute(inviteArray.get(k));
				}

				Date sDate = pickTimeSlice(combinedTimesArray, meetingDuration);
				Log.e("Mutual Start: ", sDate + "");
				addMeeting(sDate);
			}
		}.execute(userid);
		
		for(int k = 0; k < max; k++)
		{
			//TODO invite each user's email (inviteArray.get(k)) to the meeting
		}

	}

	private Date pickTimeSlice(ArrayList<Date> combined, int dura) {

		long duration = dura * 60000; // covert to milliseconds;
		/*
		 * Calendar calStart = new GregorianCalendar(); calStart.setTime(new
		 * Date()); calStart.set(Calendar.HOUR_OF_DAY, 7); //no meetings before
		 * 7 calStart.set(Calendar.MINUTE, 0); calStart.set(Calendar.SECOND, 0);
		 * calStart.set(Calendar.MILLISECOND, 0); Date startOfTime =
		 * calStart.getTime();
		 * 
		 * Calendar calEnd = new GregorianCalendar(); calEnd.setTime(new
		 * Date()); calEnd.set(Calendar.DAY_OF_YEAR,
		 * calEnd.get(Calendar.DAY_OF_YEAR)); calEnd.set(Calendar.HOUR_OF_DAY,
		 * 22); //no meetings after 10 calEnd.set(Calendar.MINUTE, 0);
		 * calEnd.set(Calendar.SECOND, 0); calEnd.set(Calendar.MILLISECOND, 0);
		 * Date endofTime = calEnd.getTime();
		 */
		// if(combined.get(0).getTime() - startOfTime.getTime() > duration)
		// return startOfTime;
		// else
		// {
		for (int i = 1; i < combined.size() - 1; i = i + 2) {
			if (combined.get(i + 1).getTime()
					- (combined.get(i).getTime() + 60000) > duration)
				return new Date(combined.get(i).getTime() + 600000);
		}

		// if(endofTime.getTime() - (combined.get(combined.size()-1).getTime() +
		// 60000) > duration)
		// {
		// return new Date(combined.get(combined.size()-1).getTime() + 600000);
		// }
		// else
		return null;
		// }
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

				// TODO Make Sure Database Returned A Value

			} catch (Exception e) {
				Log.e("log_tag", "Error Converting String " + e.toString());
			}
			return ID;
		}

	}

	private class updateTimesArrayList extends
			AsyncTask<Integer, Void, ArrayList<Date>> {
		@Override
		protected ArrayList<Date> doInBackground(Integer... nde) {
			ArrayList<Date> result = new ArrayList<Date>();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			InputStream in = null;
			try {
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(
						"http://equuleuscapstone.fulton.asu.edu/Unavail.php?user_id="
								+ nde[0]);

				Log.e("ID", nde[0] + "");
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
					Log.e("LINE", line);
					Date temp = simpleDateFormat.parse(line);
					result.add(temp);
					line = reader.readLine();
				}

			} catch (Exception e) {
				Log.e("log_tag", "Error Converting String " + e.toString());
			}

			Log.e("SIZE", result.size() + "");
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
				for (int count = 0; count < meetingArray.size(); count = count + 3) {
					insertMeetingInScroll(meetingArray.get(count),meetingArray.get(count+1),
							meetingArray.get(count + 2));
				}
			}
		}.execute();
	}

	private void insertMeetingInScroll(String meetingID, String startDateTime, String endDateTime) {
		final View newMeetingRow = v.inflate(v.getContext(),
				R.layout.meeting_scroll_row, null);
		final TextView newMeetingTextView = (TextView) newMeetingRow
				.findViewById(R.id.meetingScrollTextView);
		final String meetingid = meetingID;
		//TextView is formatted using static method from Meeting.java
		//Should be revised if/when this method uses a Meeting object.
		newMeetingTextView.setText(Meeting.formatTimeRange(startDateTime, endDateTime));
		ImageButton meetingDeleteButton = (ImageButton) newMeetingRow
				.findViewById(R.id.meetingDeleteButton);
		meetingDeleteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				meetingCounter--;
				Log.e("TEST", meetingid);
				//NEEDS TO BE CHANGED! This currently deletes the entire meeting
				//from the database, which should only occur if the meeting is owned by
				//the person calling the delete. Otherwise, this should call a method
				//that uses DeclineMeeting.php, which uses the same arguments as DeleteMeeting.
				deleteMeeting(meetingid); 
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
					String meetingID = line;
					String fName = reader.readLine();
					String lName = reader.readLine();
					String email = reader.readLine();
					String startDateTime = reader.readLine();
					String endDateTime = reader.readLine();
					String timeStamp = reader.readLine();
					String description = reader.readLine();
					line = reader.readLine();
					result.add(meetingID);
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
						"http://equuleuscapstone.fulton.asu.edu/contacts.php?user_id=" + userid);
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
				if (contactCheckBox.isChecked()) {
					struct.add(newContactTextView.getText().toString());
				} else
					struct.delete(newContactTextView.getText().toString());
			}

		});

		drawerScrollLayout.addView(newContactRow, contactCounter);
		contactCounter++;
	}

	private void deleteMeeting(final String meetingID) {
		new deleteMeetingConnection().execute(Integer.parseInt(meetingID));
		updateMeetingScrollViews();

	}

	private class deleteMeetingConnection extends
			AsyncTask<Integer, Void, Void> {
		protected Void doInBackground(Integer... meetingId) {
			InputStream in = null;
			String deleteURL = "http://equuleuscapstone.fulton.asu.edu/DeleteMeeting.php?meeting_id="
					+ meetingId[0];
			try {
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(deleteURL);
				HttpResponse response = client.execute(post);
				HttpEntity entity = response.getEntity();
				in = entity.getContent();
			} catch (Exception e) {
				Log.e("log_tag", "Error In HTTP Connection" + e.toString());
			}
			return null;
		}
	}

}
