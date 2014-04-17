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


import android.os.AsyncTask;
import android.util.Log;

class Node{
	String name;
	int index;
}
public class DataStructure {
	static int tailIndex;
	static ArrayList<Node> array;
	static int size;
	public DataStructure()
	{
		array = new ArrayList<Node>();
		tailIndex = -1;
		size = 0;
	}
	
	public void add(String nameIn)
	{
		tailIndex++;
		Node newNode = new Node();
		newNode.name = nameIn;
		newNode.index = tailIndex;
		array.add(newNode);
		size++;
	}
	
	public void delete(String nameIn)
	{
		int delIndex = search(nameIn);
		if(delIndex != -1){
		swap(delIndex, tailIndex);
		array.remove(tailIndex);
		tailIndex--;
		size--;
		}
	}
	
	public int size()
	{
		return size;
	}
	public int search(String nameIn)
	{
		for(int count = 0; count <= tailIndex; count++)
		{
			if(array.get(count).name.equals(nameIn))
				return array.get(count).index;
		}
		
		return -1;
	}
	
	public void swap(int first, int second)
	{
		array.set(first, array.get(second));
		array.get(first).index = first;
	}
	
	public String pop()
	{
		final String[] ID = new String[2];
		if(tailIndex >= 0){
			String name = array.get(tailIndex).name;
			array.remove(tailIndex);
			tailIndex--;
			Log.e("NAME", name);
			return name;
		}else{return null;}
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
			Log.e("TAG", ID);
			return ID;
		}

	}
}
