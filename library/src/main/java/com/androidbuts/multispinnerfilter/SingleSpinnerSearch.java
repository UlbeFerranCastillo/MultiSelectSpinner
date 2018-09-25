package com.androidbuts.multispinnerfilter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SingleSpinnerSearch extends android.support.v7.widget.AppCompatSpinner implements OnCancelListener {
	private static final String TAG = SingleSpinnerSearch.class.getSimpleName();
	private List<KeyPairBoolData> items;
	private String defaultText = "";
	private String spinnerTitle = "";
	private String searchText = getContext().getString(R.string.type_to_search);
	private String nullSearch = getContext().getString(R.string.null_search);
	private SpinnerListener listener;
	MyAdapter adapter;
	public static AlertDialog.Builder builder;
	public static AlertDialog ad;
	private List<KeyPairBoolData> selectedItems = new ArrayList<>();

	public SingleSpinnerSearch(Context context) {
		super(context);
	}

	public SingleSpinnerSearch(Context arg0, AttributeSet arg1) {
		super(arg0, arg1);
		TypedArray a = arg0.obtainStyledAttributes(arg1, R.styleable.MultiSpinnerSearch);
		final int N = a.getIndexCount();
		for (int i = 0; i < N; ++i) {
			int attr = a.getIndex(i);
			if (attr == R.styleable.MultiSpinnerSearch_hintText) {
				spinnerTitle = a.getString(attr);
				defaultText = spinnerTitle;
				break;
			}
		}
		Log.i(TAG, "spinnerTitle: "+spinnerTitle);
		a.recycle();
	}

	public SingleSpinnerSearch(Context arg0, AttributeSet arg1, int arg2) {
		super(arg0, arg1, arg2);
	}
	
	public void setDefaultText(String text){
		this.defaultText = text;
	}
	
	public void setTitle(String title){
		this.spinnerTitle = title;
	}

	public void setHintSearchText(String searchText){
		this.searchText = searchText;
	}

	public void setNullSearchText(String nullSearchText){
		this.nullSearch = nullSearchText;
	}

	public List<KeyPairBoolData> getSelectedItems() {
		selectedItems = new ArrayList<>();
		for(KeyPairBoolData item : items){
			if(item.isSelected()){
				selectedItems.add(item);
			}
		}
		return selectedItems;
	}

	public List<KeyPairBoolData> getSelectedItem() {
		return selectedItems;
	}

	public List<Long> getSelectedIds() {
		List<Long> selectedItemsIds = new ArrayList<>();
		for(KeyPairBoolData item : items){
			if(item.isSelected()){
				selectedItemsIds.add(item.getId());
			}
		}
		return selectedItemsIds;
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		// refresh text on spinner

		String spinnerText = null;
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).isSelected()) {
				spinnerText = items.get(i).getName();
				break;
			}
		}
		if (spinnerText==null){
			spinnerText = defaultText;
		}

		ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(getContext(), R.layout.textview_for_spinner, new String[] { spinnerText });
		setAdapter(adapterSpinner);

		if(adapter != null)
			adapter.notifyDataSetChanged();

		listener.onItemsSelected(items);
	}

	@Override
	public boolean performClick() {

		builder = new AlertDialog.Builder(getContext(), R.style.myDialog);
		builder.setTitle(spinnerTitle);

		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );

		View view = inflater.inflate(R.layout.alert_dialog_listview_search, null);
		builder.setView(view);

		final View viewBelow = (View) view.findViewById(R.id.viewBelow);
        final View viewBelowlist = (View) view.findViewById(R.id.viewBelowlist);

		final ListView listView = (ListView) view.findViewById(R.id.alertSearchListView);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		listView.setFastScrollEnabled(false);
		adapter = new MyAdapter(getContext(), items);

		listView.setAdapter(adapter);
		for(int i =0;i<items.size();i++){
			if(items.get(i).isSelected()){
				listView.setSelection(i);
				break;
			}
		}

		final TextView emptyText = (TextView) view.findViewById(R.id.empty);
		emptyText.setText(nullSearch);
		listView.setEmptyView(emptyText);

		if(listView.getCount() == 0){
            viewBelow.setVisibility(View.GONE);
            viewBelowlist.setVisibility(View.GONE);
        }

		EditText editText = (EditText) view.findViewById(R.id.alertSearchEditText);
		editText.setHint(searchText);
		editText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				adapter.getFilter().filter(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				Log.i(TAG, " ITEMS : " + items.size() );
				dialog.cancel();
			}
		});

		builder.setOnCancelListener(this);
		ad = builder.show();
		return true;
	}

	public void setItems(List<KeyPairBoolData> items, int position, SpinnerListener listener) {

		this.items = items;
		this.listener = listener;

		for (KeyPairBoolData item : items){
			if(item.isSelected()){
				defaultText = item.getName();
				break;
			}
		}

		ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(getContext(), R.layout.textview_for_spinner, new String[] { defaultText });
		setAdapter(adapterSpinner);

		if(position != -1)
		{
			items.get(position).setSelected(true);
			//listener.onItemsSelected(items);
			onCancel(null);
		}
	}



	//Adapter Class
	public class MyAdapter extends BaseAdapter implements Filterable {

		List<KeyPairBoolData> arrayList;
		List<KeyPairBoolData> mOriginalValues; // Original Values
		LayoutInflater inflater;

		public MyAdapter(Context context, List<KeyPairBoolData> arrayList) {
			this.arrayList = arrayList;
			inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return arrayList.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		private class ViewHolder {
			TextView textView;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			Log.i(TAG, "getView() enter");
			ViewHolder holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.item_listview_single, parent, false);
			holder.textView = (TextView) convertView.findViewById(R.id.alertTextView);
			convertView.setTag(holder);
			holder.textView.setTextColor(ContextCompat.getColor(getContext(),R.color.text_color));
			convertView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));

			final KeyPairBoolData data = arrayList.get(position);

			holder.textView.setText(data.getName());
			holder.textView.setTypeface(null, Typeface.NORMAL);

			convertView.setOnClickListener(new OnClickListener()
			{
				public void onClick(View v)
				{
					int len = arrayList.size();
					for (int i = 0; i < len; i++)
					{
						data.setSelected(false);
					    items.get(i).setSelected(false);
						arrayList.get(i).setSelected(false);
						if (i == position)
						{
							data.setSelected(true);
							arrayList.get(i).setSelected(true);
							selectedItems.clear();
							selectedItems.add(arrayList.get(i));
							Log.i(TAG, "On Click Selected Item : " + arrayList.get(i).getName() + " : " + arrayList.get(i).isSelected());
						}
					}
					ad.dismiss();
					SingleSpinnerSearch.this.onCancel(ad);
				}
			});

			if(data.isSelected()){
				holder.textView.setTypeface(null, Typeface.BOLD);
				holder.textView.setTextColor(Color.WHITE);
				convertView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.list_selected));
			}else{
				holder.textView.setTypeface(null, Typeface.NORMAL);
				holder.textView.setTextColor(ContextCompat.getColor(getContext(),R.color.text_color));
				convertView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
			}
			return convertView;
		}

		@SuppressLint("DefaultLocale")
		@Override
		public Filter getFilter() {
			return new Filter() {

				@SuppressWarnings("unchecked")
				@Override
				protected void publishResults(CharSequence constraint,FilterResults results) {

					arrayList = (List<KeyPairBoolData>) results.values; // has the filtered values
					notifyDataSetChanged();  // notifies the data with new filtered values
				}

				@Override
				protected FilterResults performFiltering(CharSequence constraint) {
					FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
					List<KeyPairBoolData> FilteredArrList = new ArrayList<>();

					if (mOriginalValues == null) {
						mOriginalValues = new ArrayList<>(arrayList); // saves the original data in mOriginalValues
					}

					/********
					 * 
					 *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
					 *  else does the Filtering and returns FilteredArrList(Filtered)  
					 *
					 ********/
					if (constraint == null || constraint.length() == 0) {

						// set the Original result to return  
						results.count = mOriginalValues.size();
						results.values = mOriginalValues;
					} else {
						constraint = constraint.toString().toLowerCase();
						for (int i = 0; i < mOriginalValues.size(); i++) {
							Log.i(TAG, "Filter : " + mOriginalValues.get(i).getName() + " -> " + mOriginalValues.get(i).isSelected());
							String data = mOriginalValues.get(i).getName();
							if (data.toLowerCase().contains(constraint.toString())) {
								FilteredArrList.add(mOriginalValues.get(i));
							}
						}
						// set the Filtered result to return
						results.count = FilteredArrList.size();
						results.values = FilteredArrList;
					}
					return results;
				}
			};
		}
	}
}