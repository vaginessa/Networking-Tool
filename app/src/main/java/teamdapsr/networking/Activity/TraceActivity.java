package teamdapsr.networking.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import teamdapsr.networking.R;
import teamdapsr.networking.TraceRoute.TracerouteContainer;
import teamdapsr.networking.TraceRoute.TracerouteWithPing;


/**
 * The main activity
 * 
 * @author Olivier Goutay
 * 
 */
public class TraceActivity extends Activity {

	public static final String EXTRA_domain= "domain";
	public static final String tag = "TraceroutePing";
	public static final String INTENT_TRACE = "INTENT_TRACE";
	String domain ;
	private Button buttonLaunch;
	private ProgressBar progressBarPing;
	private ListView listViewTraceroute;
	private TraceListAdapter traceListAdapter;
	private TracerouteWithPing tracerouteWithPing;
	private final int maxTtl = 40;
	private List<TracerouteContainer> traces;

	/**
	 * onCreate, init main components from view
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trace);

		/**
		 * Domain or IP Address to Ping
		 */
		Intent intent = getIntent();
		domain = intent.getStringExtra(EXTRA_domain);

		this.tracerouteWithPing = new TracerouteWithPing(this);
		this.traces = new ArrayList<TracerouteContainer>();

		this.buttonLaunch = (Button) this.findViewById(R.id.buttonLaunch);;
		this.listViewTraceroute = (ListView) this.findViewById(R.id.listViewTraceroute);
		this.progressBarPing = (ProgressBar) this.findViewById(R.id.progressBarPing);

		initView();
	}

	/**
	 * initView, init the main view components (action, adapter...)
	 */
	private void initView() {

		traceListAdapter = new TraceListAdapter(getApplicationContext());
		listViewTraceroute.setAdapter(traceListAdapter);

        if (domain.length() == 0) {
            Toast.makeText(TraceActivity.this, getString(R.string.no_text), Toast.LENGTH_SHORT).show();
        } else {
            traces.clear();
            traceListAdapter.notifyDataSetChanged();
            startProgressBar();
            //hideSoftwareKeyboard(editTextPing);
            tracerouteWithPing.executeTraceroute(domain, maxTtl);
        }

	}

	/**
	 * Allows to refresh the listview of traces
	 * 
	 * @param trace The list of traces to refresh
	 */
	public void refreshList(TracerouteContainer trace) {
		final TracerouteContainer fTrace = trace;
        runOnUiThread(new Runnable() {
			@Override
			public void run() {
				traces.add(fTrace);
				traceListAdapter.notifyDataSetChanged();
			}
		});
	}

	/**
	 * The adapter of the listview (build the views)
	 */
	public class TraceListAdapter extends BaseAdapter {

		private Context context;

		public TraceListAdapter(Context c) {
			context = c;
		}

		public int getCount() {
			return traces.size();
		}

		public TracerouteContainer getItem(int position) {
			return traces.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;

			// first init
			if (convertView == null) {
				LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = vi.inflate(R.layout.item_list_trace, null);

				TextView textViewNumber = (TextView) convertView.findViewById(R.id.textViewNumber);
				TextView textViewIp = (TextView) convertView.findViewById(R.id.textViewIp);
				TextView textViewTime = (TextView) convertView.findViewById(R.id.textViewTime);
				ImageView imageViewStatusPing = (ImageView) convertView.findViewById(R.id.imageViewStatusPing);

				// Set up the ViewHolder.
				holder = new ViewHolder();
				holder.textViewNumber = textViewNumber;
				holder.textViewIp = textViewIp;
				holder.textViewTime = textViewTime;
				holder.imageViewStatusPing = imageViewStatusPing;

				// Store the holder with the view.
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			TracerouteContainer currentTrace = getItem(position);

			if (position % 2 == 1) {
				convertView.setBackgroundResource(R.drawable.table_odd_lines);
			} else {
				convertView.setBackgroundResource(R.drawable.table_pair_lines);
			}

			if (currentTrace.isSuccessful()) {
				holder.imageViewStatusPing.setImageResource(R.drawable.check);
			} else {
				holder.imageViewStatusPing.setImageResource(R.drawable.cross);
			}

			holder.textViewNumber.setText(position + "");
			holder.textViewIp.setText(currentTrace.getHostname() + " (" + currentTrace.getIp() + ")");
			holder.textViewTime.setText(currentTrace.getMs() + "ms");

			return convertView;
		}

		// ViewHolder pattern
		class ViewHolder {
			TextView textViewNumber;
			TextView textViewIp;
			TextView textViewTime;
			ImageView imageViewStatusPing;
		}
	}

	/**
	 * Hides the keyboard
	 * 
	 * @param currentEditText
	 *            The current selected edittext
	 */
	public void hideSoftwareKeyboard(EditText currentEditText) {
		InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm.isActive()) {
			imm.hideSoftInputFromWindow(currentEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	public void startProgressBar() {
		progressBarPing.setVisibility(View.VISIBLE);
	}

	public void stopProgressBar() {
		progressBarPing.setVisibility(View.GONE);
	}

}
