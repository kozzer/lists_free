package com.kozzer.lists_free;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ChildListActionsAdapter extends ArrayAdapter<String> {

	static String[] actions = { "Sort list alphabetically", "Push marked to bottom", "Unmark all items", "Delete marked items only", "Delete all items" };
	boolean isDarkTheme;
	Context context;
	ImageView rowIcon;
	TextView rowText;
	
	public ChildListActionsAdapter(Context context, boolean darkTheme){
		super(context, R.layout.list_actions_dialog_item, actions);
		isDarkTheme = darkTheme;
		this.context = context;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (convertView == null){
			convertView = inflater.inflate(R.layout.list_actions_dialog_item, null);
			rowIcon = (ImageView) convertView.findViewById(R.id.action_icon);
			rowText = (TextView) convertView.findViewById(R.id.action_item_text);
		} 
		
		switch (position){
			case 0:
				rowIcon.setImageResource(isDarkTheme ? R.drawable.dark_sort : R.drawable.light_sort);
				break;
			case 1:
				rowIcon.setImageResource(isDarkTheme ? R.drawable.dark_push_marked : R.drawable.light_push_marked);
				break;
			case 2:
				rowIcon.setImageResource(isDarkTheme ? R.drawable.dark_unmark : R.drawable.light_unmark);
				break;
			case 3:
				rowIcon.setImageResource(isDarkTheme ? R.drawable.dark_clear : R.drawable.light_clear);
				break;
			case 4:
				rowIcon.setImageResource(isDarkTheme ? R.drawable.dark_clear : R.drawable.light_clear);
				break;
			default:
				break;
		}
		rowText.setText(actions[position]);
		if (isDarkTheme){
			convertView.setBackgroundColor(context.getResources().getColor(R.color.context_dark));
			rowText.setTextColor(context.getResources().getColor(R.color.text_dark));
		} else {
			convertView.setBackgroundColor(context.getResources().getColor(R.color.context_light));
			rowText.setTextColor(context.getResources().getColor(R.color.text_light));
		}
		
		return convertView;
	}
}
