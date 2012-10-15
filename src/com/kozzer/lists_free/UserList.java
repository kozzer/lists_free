package com.kozzer.lists_free;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class UserList {
	
	public String ListName;
	public int ListID;
	public ArrayList<UserListItem> ListItems;
	public int ListIndex;
	
	public UserList(int listID, String listName, int listIndex){
		ListName = listName;
		ListID = listID;
		ListIndex = listIndex;
	}

	public void SortListItems()
	{
		Collections.sort(ListItems, new Comparator<UserListItem>(){
			public int compare(UserListItem lhs, UserListItem rhs) {
				return lhs.ListItem.compareTo(rhs.ListItem);
			}
		});
	}
	
	public void PushMarkedToBottom(){
		Collections.sort(ListItems, new Comparator<UserListItem>(){
			public int compare(UserListItem lhs, UserListItem rhs) {
				if (lhs.ItemCleared && !rhs.ItemCleared){
					return 1;
				} else if (!lhs.ItemCleared && rhs.ItemCleared){
					return -1;
				} else {
					return 0;
				}
			}
		});		
	}
}
