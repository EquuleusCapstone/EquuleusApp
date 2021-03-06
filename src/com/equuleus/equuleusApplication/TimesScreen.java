package com.equuleus.equuleusApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.TimePicker;

public class TimesScreen extends Fragment {
	private View v;
	private int hour, startHour, endHour;
	private int minute, startMin, endMin;
	private int day, startDay;
	private int month, startMonth;
	private int year, startYear;
	private int timesCounter = 0;
	private String startTime, endTime, startDate;
	private SlidingDrawer drawer;
	private ArrayList<String> timesArray;
	private Date start, end;

	private TableLayout timesTableLayout;

	private TextView timesStartTimeText, timesStartDateText, timesEndTimeText,
			timesTitle;
	private Button timesStartTimeButton, timesEndTimeButton,
			timesStartDateButton, timesConfirmButton;

	private int userid;

	private void showErrorDialog(String msg) {
		AlertDialog.Builder err = new AlertDialog.Builder(this.getActivity());
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

	@SuppressWarnings("deprecation")
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		userid = getActivity().getIntent().getExtras().getInt("userID");
		v = inflater.inflate(R.layout.times_screen, null);

		drawer = (SlidingDrawer) v.findViewById(R.id.timesSlidingDrawer);

		timesTableLayout = (TableLayout) v
				.findViewById(R.id.timesScrollTableLayout);

		timesConfirmButton = (Button) v
				.findViewById(R.id.timesDrawerConfirmButton);

		timesStartTimeButton = (Button) v
				.findViewById(R.id.timesStartTimeButton);
		timesEndTimeButton = (Button) v.findViewById(R.id.timesEndTimeButton);
		timesStartDateButton = (Button) v
				.findViewById(R.id.timesStartDateButton);

		timesStartTimeText = (TextView) v.findViewById(R.id.timesStartTimeView);
		timesEndTimeText = (TextView) v.findViewById(R.id.timesEndTimeTextView);
		timesStartDateText = (TextView) v
				.findViewById(R.id.timesStartDateTextView);
		timesTitle = (TextView) v.findViewById(R.id.timesTitleTextView);

		final Calendar c = Calendar.getInstance();
		hour = c.get(Calendar.HOUR_OF_DAY);
		minute = c.get(Calendar.MINUTE);
		day = c.get(Calendar.DAY_OF_MONTH);
		month = c.get(Calendar.MONTH);
		year = c.get(Calendar.YEAR);

		updateTimeScrollView();
		drawer.setOnDrawerOpenListener(new OnDrawerOpenListener() {

			@Override
			public void onDrawerOpened() {
				timesTitle.setText("");

			}

		});

		drawer.setOnDrawerCloseListener(new OnDrawerCloseListener() {

			@Override
			public void onDrawerClosed() {
				timesTitle.setText("UnAvailable Times");

			}

		});

		timesConfirmButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.e("TAG", "Here");
				// TODO Check If EndTime and Date is after Start Time and Date
				// Can do this checking in the other listeners if you want
				if (timesStartTimeText.getText().toString().equals("Not Set")
						|| timesEndTimeText.getText().toString()
								.equals("Not Set")
						|| timesStartDateText.getText().toString()
								.equals("Not Set")) {

					showErrorDialog("Not All Time Slots Filled Out");
				} else {
					if (endHour < startHour) {
						showErrorDialog("EndTime After StartTime");
					} else if (endHour == startHour && endMin <= startMin)
						showErrorDialog("EndTime After StartTime");
					else {
						addTime(startTime, endTime, startDate);
						drawer.close();
					}
				}
			}

		});

		// Opens Dialog And Stores Selected Start Time
		timesStartTimeButton.setOnClickListener(new OnClickListener() {

			@Override
			// Opens New Time Picker Dialog For Picking Start Time
			public void onClick(View v) {
				TimePickerDialog f = new TimePickerDialog(v.getContext(),
						timePickerStartListener, hour, minute, false);
				f.show();
			}

		});

		// Opens Dialog And Stores Selected End Time
		timesEndTimeButton.setOnClickListener(new OnClickListener() {

			@Override
			// Opens New Time Picker Dialog For Picking Start Time
			public void onClick(View v) {
				TimePickerDialog d = new TimePickerDialog(v.getContext(),
						timePickerEndListener, hour, minute, false);
				d.show();
			}

		});

		// Opens Dialog And Stores Selected Start Date
		timesStartDateButton.setOnClickListener(new OnClickListener() {

			@Override
			// Opens New Time Picker Dialog For Picking Start Time
			public void onClick(View v) {
				DatePickerDialog e = new DatePickerDialog(v.getContext(),
						datePickerStartListener, year, month, day);
				e.show();
			}

		});

		//
		return v;
	}

	// Listens For Selected Start Date
	private DatePickerDialog.OnDateSetListener datePickerStartListener = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {

			startDay = dayOfMonth;
			startMonth = monthOfYear + 1;
			startYear = year;

			startDate = startYear + "";
			if (startMonth < 10)
				startDate = startYear + "-0" + startMonth;
			else
				startDate = startYear + "-" + startMonth;

			if (startDay < 10)
				startDate += "-0" + startDay;
			else
				startDate += "-" + startDay;

			timesStartDateText.setText(startDate);
		}
	};

	// Listens For Selected Start Time
	private TimePickerDialog.OnTimeSetListener timePickerStartListener = new TimePickerDialog.OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minuteIn) {
			startHour = hourOfDay;
			startMin = minuteIn;

			if (startMin > 9)
				startTime = startHour + ":" + startMin;
			else
				startTime = startHour + ":0" + startMin;

			timesStartTimeText.setText(startTime);

		}
	};

	// Listens For Selected End Time
	private TimePickerDialog.OnTimeSetListener timePickerEndListener = new TimePickerDialog.OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minuteIn) {

			endHour = hourOfDay;
			endMin = minuteIn;

			if (endMin > 9)
				endTime = endHour + ":" + endMin;
			else
				endTime = endHour + ":0" + endMin;
			timesEndTimeText.setText(endTime);

		}
	};

	private void updateTimeScrollView() {
		timesTableLayout.removeAllViews();
		timesCounter = 0;

		// Pulls New Times List From Database
		new updateTimesArrayList() {
			protected void onPostExecute(ArrayList<String> result) {
				timesArray = result;
				for (int count = 0; count < timesArray.size(); count = count + 2) {
					String startDateTime = timesArray.get(count);
					String endDateTime = timesArray.get(count + 1);
					String[] startDateTimeInsert = stringParser(startDateTime);
					String[] endDateTimeInsert = stringParser(endDateTime);
					insertTimesInScroll(startDateTimeInsert, endDateTimeInsert);
				}
			}
		}.execute();
	}

	private void insertTimesInScroll(final String[] start, final String[] end) {
		final View newTimesRow = v.inflate(v.getContext(),
				R.layout.times_scroll_row, null);

		// This is where times would be formatted. However, it looks like the
		// delete method
		// Relies on these times being formatted in a certain way, and it
		// wouldn't be safe to
		// update. If Drew is able to, the deleteTime function should be changed
		// so that it can
		// be called without relying on the TextView being set up in a specific
		// way.
		// I would do this by using a Meeting object to store the start and end
		// times of each
		// time slice instead of parsing them directly from the textview.

		final TextView newTimesTextView = (TextView) newTimesRow
				.findViewById(R.id.timesScrollTextView);
		newTimesTextView.setText(Meeting.formatTimeRange(start[0] + " "
				+ start[1] + ":00", end[0] + " " + end[1] + ":00"));

		ImageButton contactDeleteButton = (ImageButton) newTimesRow
				.findViewById(R.id.timesDeleteButton);
		contactDeleteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				timesCounter--;
				String newStartTime = start[1];
				String newEndTime = end[1];
				String newDateIn = start[0];
				String newDateEnd = end[0];
				deleteTime(newStartTime, newEndTime, newDateIn);
			}

		});

		timesTableLayout.addView(newTimesRow, timesCounter);
		timesCounter++;
	}

	private String[] stringParser(String input) {
		String[] returnThis = new String[2];
		String day, month, year;
		String time;
		day = input.substring(8, 10);
		month = input.substring(5, 7);
		year = input.substring(0, 4);
		time = input.substring(11);
		returnThis[0] = year + "-" + month + "-" + day;
		returnThis[1] = time;

		return returnThis;

	}

	// Pulls Contact Information From Database Saves In Array List
	private class updateTimesArrayList extends
			AsyncTask<Void, Void, ArrayList<String>> {
		@Override
		protected ArrayList<String> doInBackground(Void... params) {
			ArrayList<String> result = new ArrayList<String>();

			InputStream in = null;
			try {
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(
						"http://equuleuscapstone.fulton.asu.edu/Unavail.php?user_id="
								+ userid);
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
					result.add(line);
					line = reader.readLine();
				}

			} catch (Exception e) {
				Log.e("log_tag", "Error Converting String " + e.toString());
			}

			return result;
		}

	}

	// Adds A Time
	private void deleteTime(String start, String end, String date) {
		String[] deleteArray = new String[2];
		deleteArray[0] = date + "%20" + start + ":00";
		deleteArray[1] = date + "%20" + end + ":00";

		Log.e("TAG", deleteArray[0]);
		Log.e("TAG", deleteArray[1]);
		new deleteTimeConnection().execute(deleteArray);
		updateTimeScrollView();

	}

	// Calls PHP Script To Delete A Time
	private class deleteTimeConnection extends AsyncTask<String[], Void, Void> {
		@Override
		protected Void doInBackground(String[]... deleteArray) {
			InputStream in = null;
			String deleteURL = "http://equuleuscapstone.fulton.asu.edu/DeleteUnavail.php?user_id="
					+ userid
					+ "&start='"
					+ deleteArray[0][0]
					+ "'&end='"
					+ deleteArray[0][1] + "'";
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

	// Adds A Time
	private void addTime(String start, String end, String date) {
		String[] addArray = new String[2];
		addArray[0] = startDate + "%20" + start + ":00";
		addArray[1] = startDate + "%20" + end + ":00";

		Log.e("TAG", addArray[0]);
		Log.e("TAG", addArray[1]);
		new addTimeConnection().execute(addArray);
		updateTimeScrollView();

	}

	// Calls The PHP Script To Add A Time
	private class addTimeConnection extends AsyncTask<String[], Void, Void> {

		@Override
		protected Void doInBackground(String[]... addArray) {
			InputStream in = null;
			String addURL = "http://equuleuscapstone.fulton.asu.edu/AddUnavail.php?user_id="
					+ userid
					+ "&start='"
					+ addArray[0][0]
					+ "'&end='"
					+ addArray[0][1] + "'";
			try {
				Log.e("TAG", addArray[0][0]);
				Log.e("TAG", addArray[0][1]);
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
}
