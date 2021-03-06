package com.cornellsatech.o_week;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cornellsatech.o_week.models.Event;
import com.cornellsatech.o_week.util.NotificationCenter;

import org.joda.time.LocalDate;

/**
 * Displays a list of events, ordered chronologically. This is a {@link Fragment} so that it can be
 * easily swapped out with {@link ScheduleFragment} while keeping the same date picker up top.
 * Layout in {@link com.cornellsatech.o_week.R.layout#fragment_feed}
 */
public class FeedFragment extends Fragment
{
	private static final String TAG = FeedFragment.class.getSimpleName();
	private static final String DATE_BUNDLE_KEY = "date";
	private LocalDate date;
	private RecyclerView feedRecycler;
	private FeedAdapter feedAdapter;

	/**
	 * Create an instance of {@link FeedFragment} with the given date.
	 * This should be the only way you create instances of {@link FeedFragment}.
	 *
	 * It passes the given date as a Bundle to {@link FeedFragment}.
	 *
	 * @param date The date the feed will display.
	 * @return Instance of the feed.
	 */
	public static FeedFragment newInstance(LocalDate date)
	{
		FeedFragment feedFragment = new FeedFragment();

		Bundle args = new Bundle();
		args.putSerializable(DATE_BUNDLE_KEY, date);
		feedFragment.setArguments(args);

		return feedFragment;
	}
	/**
	 * Connects views, sets up recycler, registers as listener. Unregisters in {@link #onDestroyView()}.
	 * Listens for event clicks and opens {@link DetailsActivity} upon a click.
	 *
	 * Retrieves {@link #date} from the bundle.
	 */
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_feed, container, false);
		setHasOptionsMenu(true);

		//retrieve date from bundle
		if (getArguments() != null)
			date = (LocalDate) getArguments().getSerializable(DATE_BUNDLE_KEY);
		else
			Log.e(TAG, "onCreateView: date not found");

		feedRecycler = view.findViewById(R.id.feedRecycler);
		setUpRecycler(view.findViewById(R.id.emptyState));
		return view;
	}

	/**
	 * Unregisters as listener to prevent memory leaks. Also triggers {@link FeedAdapter}'s un-registration.
	 * Registered in {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
	 */
	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		NotificationCenter.DEFAULT.unregister(this);
		//detach the adapter so that its onDestroy methods trigger
		feedRecycler.setAdapter(null);
	}
	/**
	 * Connects {@link #feedRecycler} to {@link #feedAdapter}.
	 * @param emptyView the empty view the recycler should display when there are no elements
	 */
	private void setUpRecycler(View emptyView)
	{
		feedAdapter = new FeedAdapter(date, emptyView);
		feedRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
		feedRecycler.setAdapter(feedAdapter);
		scrollToNextEvent();
	}
	/**
	 * Scrolls {@link #feedRecycler} to the next upcoming event based on the current time and date.
	 */
	private void scrollToNextEvent()
	{
		long now = System.currentTimeMillis();

		if (UserData.selectedDate == null)
			return;
		if (!LocalDate.now().isEqual(UserData.selectedDate))
			return;
		for (int i = 0; i < feedAdapter.events.size(); i++)
		{
			Event event = feedAdapter.events.get(i);
			if (event.getStart() < now)
				continue;

			feedRecycler.scrollToPosition(i);
			return;
		}
	}
}
