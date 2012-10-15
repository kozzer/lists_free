package com.kozzer.lists_free;

import com.kozzer.lists_free.UserListItem;

@SuppressWarnings("rawtypes")
public class UserListItem implements Comparable {
	public int ItemID;
	public int ParentListID;
	public String ListItem;
	public boolean ItemCleared;
	public int ItemIndex;
	
	public UserListItem(int itemID, int parentID, String listItem, boolean itemCleared, int itemIndex){
		ItemID = itemID;
		ParentListID = parentID;
		ListItem = listItem;
		ItemCleared = itemCleared;
		ItemIndex = itemIndex;
	}
	
	public int compareTo(Object other){
		int index1 = this.ItemIndex;
		int index2 = ((UserListItem) other).ItemIndex;
		if (index1 > index2){
			return 1;
		} else {
			return -1;
		}
	}

}
