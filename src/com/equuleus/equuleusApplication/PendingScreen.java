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
import android.graphics.Color;
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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class PendingScreen extends Fragment {
	private View v;
	private TableLayout pendingScrollLayout;
	private String meetingTitle, meetingStart, meetingEnd;
	private int meetingCounter = 0, ID;
	private ArrayList<String> meetingArray = null;
	private ArrayList<Integer> meetingIdsByIndex = null;
	private int userid;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		userid = getActivity().getIntent().getExtras().getInt("userID");
		v = inflater.inflate(R.layout.pending_screen, null);
		pendingScrollLayout = (TableLayout) v.findViewById(R.id.pendingScrollTableLayout);
		meetingIdsByIndex = new ArrayList<Integer>();
		//Populate the Table with pending meetings
		updatePendingScrollViews();
		
		return v;
		
	}//end onCreateView
	
	private void updatePendingScrollViews() {
		//Clear the table and the variables used to track it
		pendingScrollLayout.removeAllViews();
		meetingCounter = 0;
		meetingIdsByIndex = new ArrayList<Integer>();
		
		//Get the meeting invitations from the database
		new updatePendingArrayList() {
			protected void onPostExecute(ArrayList<String> result) {
				meetingArray = result;
				//The times to insert come in pairs, with a meeting id afterwards
				for (int count = 0; count < meetingArray.size(); count += 3) {
					insertMeetingInScroll(meetingArray.get(count), meetingArray.get(count+1),
							meetingArray.get(count+2));
				}
			}
		}.execute();
		
	}//end updatePendingScrollViews
	
	//Add a new meeting into our scroll view
	private void insertMeetingInScroll(String startDateTime, String endDateTime, String meetingId) {
		
		
		final View newMeetingRow = v.inflate(v.getContext(), R.layout.pending_scroll_row, null);
		final TextView newMeetingTextView = (TextView) newMeetingRow
				.findViewById(R.id.pendingScrollTV);
		
		//Change id of the textbox to allow us to identify it later
		pendingScrollLayout.addView(newMeetingRow, meetingCounter);
		newMeetingTextView.setId(meetingCounter);
		meetingIdsByIndex.add(Integer.parseInt(meetingId));
		meetingCounter++;

		String formattedTime = Meeting.formatTimeRange(startDateTime, endDateTime);
		newMeetingTextView.setText(formattedTime);

		
		ImageButton declineMeetingBtn = (ImageButton) newMeetingRow.findViewById(R.id.pendingDeleteButton);
		ImageButton acceptMeetingBtn = (ImageButton) newMeetingRow.findViewById(R.id.pendingConfirmButton);
		
		//The decline button listener will make a call to the asynchronous class that 
		// handles declining meetings, then interpret the result
		declineMeetingBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Integer meetingKey = newMeetingTextView.getId();
				
				//Create a thread to decline a meeting
				new declineAMeeting() {
					protected void onPostExecute(Boolean result) {
						if (result) {
							meetingCounter--;
							updatePendingScrollViews();
						}
						//else
							//Some error message should go here.
					}
				}.execute(meetingKey);
			}
			
		}); //End decline button listener
		
		//The accept button listener will make a call to the Asynchronous class
		//that handles confirming meetings, then interpret the result
		acceptMeetingBtn.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				Integer meetingKey = newMeetingTextView.getId();
				new confirmAMeeting() {
					protected void onPostExecute(Boolean result) {
						if (result) {
							meetingCounter--;
							updatePendingScrollViews();
						}
						//else
							//Some type of error message should go here. 
					}
				}.execute(meetingKey);
			}
		}); //End accept button listener
	}//end InsertMeeting
	
	private class declineAMeeting extends AsyncTask<Integer, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Integer... params) {
			// TODO Auto-generated method stub
			InputStream in = null;
			//Call DeclineMeeting.php after getting the meeting Id
			try {
				
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(
					"http://equuleuscapstone.fulton.asu.edu/DeclineMeeting.php?user_id=" + userid + "&"
					+"meeting_id="+meetingIdsByIndex.get(params[0]));
			HttpResponse response = client.execute(post);
			HttpEntity entity = response.getEntity();
			in = entity.getContent();
			}
			catch (Exception e) {
				Log.e("Pending_Screen", "Error in HTTP Connection "+e.toString());
				return false;
			}
			return true;
		}
		
	} //end decline a meeting private class (thread)
	
	private class confirmAMeeting extends AsyncTask<Integer, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Integer... params) {
			// TODO Auto-generated method stub
			InputStream in = null;
			//Call DeclineMeeting.php after getting the meeting Id
			try {
				
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(
					"http://equuleuscapstone.fulton.asu.edu/AttendMeeting.php?user_id=" + userid + "&"
					+"meeting_id="+meetingIdsByIndex.get(params[0]));
			HttpResponse response = client.execute(post);
			HttpEntity entity = response.getEntity();
			in = entity.getContent();
			}
			catch (Exception e) {
				Log.e("Pending_Screen", "Error in HTTP Connection "+e.toString());
				return false;
			}
			return true;
		}	
	} //end confirm a meeting private class (thread)
	
	private class updatePendingArrayList extends 
		AsyncTask<Void, Void, ArrayList<String>> {
		@Override
		protected ArrayList<String> doInBackground(Void... params) {
			ArrayList<String> result = new ArrayList<String>();
			InputStream in = null;
			//Attempt to query the database for the meeting invitations
			try {
				HttpClient client = new DefaultHttpClient();
				//TODO Remove hardcoded user id 
				HttpPost post = new HttpPost(
						"http://equuleuscapstone.fulton.asu.edu/PendingMeetings.php?user_id=" + userid);
				HttpResponse response = client.execute(post);
				HttpEntity entity = response.getEntity();
				in = entity.getContent();
			}
			catch (Exception e) {
				Log.e("Pending_Screen", "Error in HTTP Connection " + e.toString());
			}
			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(in));
				String line = reader.readLine();
				
				//Parse input until we reach }, which signifies end of results
				while(!((line.charAt(0) + "").equals("}"))) {
					String meetingId = line;
					String fName = reader.readLine();
					String lName = reader.readLine();
					String email = reader.readLine();
					String startDateTime = reader.readLine();
					String endDateTime = reader.readLine();
					String timeStamp = reader.readLine();
					String description = reader.readLine();
					
					//Get the next line of input. Could be beginning of new meeting, or }
					line = reader.readLine();
					result.add(startDateTime);
					result.add(endDateTime);
					result.add(meetingId);
				}
				
			}
			catch (Exception e) {
				Log.e("log_tag", "Error Converting String " + e.toString());
			}
			return result;
		}
	}// end updatePendingArrayList
	
}//end PendingScreen class
