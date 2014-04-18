package com.equuleus.equuleusApplication;

import java.util.ArrayList;
/*
 * Meeting.java is able to store all the relevant info needed for a Meeting,
 * allowing it to easily be used to update info in it's associated textview.
 * There are no getters/setters, so information needs to be added through direct references.
 * 
 * A static method is included to allow pages that don't need meeting objects (like the
 * unavailable times page) to have their time slices formatted in the same manner as 
 * meetings. This method can also be used if you'd rather not store all the information in
 * meeting objects, and just want the time to be formatted.
 */

public class Meeting {
	String description, fname, lname, email, start, end, created;
	int meetingId;
	static ArrayList<DatePair> months;
	
	public Meeting () {
		months = new ArrayList<DatePair>();
		months.add(new DatePair("01","January"));
		months.add(new DatePair("02","February"));
		months.add(new DatePair("03","March"));
		months.add(new DatePair("04","April"));
		months.add(new DatePair("05","May"));
		months.add(new DatePair("06","June"));
		months.add(new DatePair("07","July"));
		months.add(new DatePair("08","August"));
		months.add(new DatePair("09","September"));
		months.add(new DatePair("10","October"));
		months.add(new DatePair("11","November"));
		months.add(new DatePair("12","December"));
		
		description = "";
		fname = "";
		lname = "";
		start ="";
		end = "";
		created ="";
		meetingId = 0;

	}
	
	//Static method for formatting the time, if you don't need the full object
	static String formatTimeRange(String startTime, String endTime) {
		Meeting temp = new Meeting();
		temp.start = startTime;
		temp.end = endTime;
		
		return temp.humanReadableTime();
	}
	
	//Will use date formatting to output an appropriate String
	public String humanReadableTime() {
		//Get the start months, days, years, and times
		String startMonth = matchName(start.substring(5, 7));
		String startDay = dateSuffix(start.substring(8, 10));
		String startYear = start.substring(0,4);
		String startTime = formatTime(start.substring(11, 13),
				start.substring(14, 16));
		String endMonth = matchName(end.substring(5, 7));
		String endDay = dateSuffix(end.substring(8, 10));
		String endYear = end.substring(0,4);
		String endTime = formatTime(end.substring(11, 13),
				end.substring(14, 16));
		
		//If the years of the two times are different
		if (!startYear.equalsIgnoreCase(endYear))
			//MONTH DAY, YEAR at TIME to MONTH DAY, YEAR at TIME
			return String.format("%s %s, %s at %s to"
					+ "%s %s, %s at %s", startMonth, startDay, startYear,
					startTime, endMonth, endDay, endYear, endTime);
		//If the months or days of the two times are different
		else if (!(startMonth.equalsIgnoreCase(endMonth)) ||
				!(startDay.equalsIgnoreCase(endDay)))
			//MONTH DAY at TIME to MONTH DAY at TIME
			return String.format("%s %s at %s to %s %s at %s",
					startMonth, startDay, startTime, endMonth, endDay, endTime);
		//Otherwise, event starts and ends on the same day
		else
			//MONTH DAY, from TIME to TIME
			return String.format("%s %s, from %s to %s", startMonth,
					startDay, startTime, endTime);
	}
	
	public String formatTime(String hour, String minute) {
		int formattedHour = Integer.parseInt(hour);
		boolean morning = true;
		
		//Put the hour in 12 hour format
		if (formattedHour >= 12) {
			formattedHour -= 12;
			morning = false;
		}
		if (formattedHour == 0)
			formattedHour += 12;
		
		if (morning)
			return String.format("%d:%s AM", formattedHour, minute);
		else
			return String.format("%d:%s PM", formattedHour, minute);
	}
	
	//Determine what suffix to add to the day of the month
	public String dateSuffix (String day) {
		int number = Integer.parseInt(day);
		switch (number) {
		case 1:return number+"st";
		case 2: return number+"nd";
		case 3: return number+"rd";
		case 21: return number+"st";
		case 22: return number+"nd";
		case 23: return number+"rd";
		default: return number+"th";
		}
	}
	
	//Get the name corresponding to a month's numerical code
	public String matchName (String number) {
		for (int i = 0; i< months.size(); i++) {
			if(months.get(i).monthNumber.equalsIgnoreCase(number))
				return months.get(i).monthName;
		}
		return "";
	}
	
	private class DatePair {
		private String monthNumber, monthName;
	
		public DatePair(String number, String name) {
			monthNumber = number;
			monthName = name;
		}
	}
}
