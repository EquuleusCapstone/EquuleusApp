package com.equuleus.equuleusApplication;

import java.util.ArrayList;

class Node{
	String name;
	int index;
}
public class DataStructure {
	static int tailIndex = 0;
	static ArrayList<Node> array = new ArrayList<Node>();
	public DataStructure()
	{
	
	}
	
	public void add(String nameIn)
	{
		Node newNode = new Node();
		newNode.name = nameIn;
		newNode.index = tailIndex;
		tailIndex++;
		array.add(newNode);		
	}
	
	public void delete(String nameIn)
	{
		int delIndex = search(nameIn);
		swap(delIndex, tailIndex);
		array.remove(tailIndex);
		tailIndex--;
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
		if(tailIndex >= 0){
			String name = array.get(tailIndex).name;
			array.remove(tailIndex);
			tailIndex--;
			return name;
		}else{return null;}
	}
}
