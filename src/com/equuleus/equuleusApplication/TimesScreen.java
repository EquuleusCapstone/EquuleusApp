package com.equuleus.equuleusApplication;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
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

	private TableLayout timesTableLayout;

	private HttpURLConnection connection;

	private TextView timesStartTimeText, timesStartDateText, timesEndTimeText,
			timesTitle;
	private Button timesStartTimeButton, timesEndTimeButton,
			timesStartDateButton, timesConfirmButton;

	@SuppressWarnings("deprecation")
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
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
				final View newTimesRow = v.inflate(v.getContext(),
						R.layout.times_scroll_row, null);
				TextView newTimesTextView = (TextView) newTimesRow
						.findViewById(R.id.timesScrollTextView);
				newTimesTextView.setText(startTime + " to " + endTime + " on "
						+ startDate);

				ImageButton timesDeleteButton = (ImageButton) newTimesRow
						.findViewById(R.id.timesDeleteButton);

				// Deletes The Contact From Friends List and From Database
				timesDeleteButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						timesCounter--;
						// TODO Figure Out Delete URL
						connection = null;
						String urlDelete = "";
						try {
							URL url = new URL(urlDelete);
							connection = (HttpURLConnection) url
									.openConnection();
							connection.connect();

						} catch (MalformedURLException e1) {
							e1.printStackTrace();
						} catch (IOException e1) {
							e1.printStackTrace();
						} finally {
							if (null != connection) {
								connection.disconnect();
							}
						}

						updateTimeScrollView();

					}

				});

				timesTableLayout.addView(newTimesRow, timesCounter);
				timesCounter++;

				connection = null;
				String urlAdd = "";
				try {
					// TODO Figure Out ADD Url
					URL url = new URL(urlAdd);
					connection = (HttpURLConnection) url.openConnection();
					connection.connect();

				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				} finally {
					if (null != connection) {
						connection.disconnect();
					}
				}

				drawer.close();

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

			startDate = startMonth + "-" + startDay + "/" + startYear;

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
		connection = null;
		String urlUpdate = "";
		try {
			// TODO Figure Out Update URL
			URL url = new URL(urlUpdate);
			connection = (HttpURLConnection) url.openConnection();
			connection.connect();
			InputStream in = connection.getInputStream();

			// TODO Parse Page Content

		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if (null != connection) {
				connection.disconnect();
			}
		}
		drawer.close();
	}

}
