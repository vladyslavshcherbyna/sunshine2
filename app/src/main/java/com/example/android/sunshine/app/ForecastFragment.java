package com.example.android.sunshine.app;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.sunshine.app.tools.WeatherDataParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * A fragment containing our list view with weather.
 */
public class ForecastFragment extends Fragment {

    private ArrayAdapter<String> foreCasts;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_fragment_menu, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        String[] lists = {

        };
        List<String> arrayList = new ArrayList<>(Arrays.asList(lists));
        foreCasts = new ArrayAdapter<>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                arrayList
        );

        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(foreCasts);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            private volatile Toast instance = null;
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String foreCast = foreCasts.getItem(i);
                Intent intent = new Intent();
                intent.setAction("com.example.android.sunshine.action.SHOW_DETAIL");
                intent.putExtra(Intent.EXTRA_TEXT, foreCast);
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
        return rootView;
    }

    private void updateWeather()
    {
        HashMap<String, String> arguments = new HashMap<>();
        arguments.put("method", "GET");
        arguments.put("units", getSettingsValue(getString(R.string.pref_metric_list_key), getString(R.string.pref_metric_default_key)));
        arguments.put("link", "http://api.openweathermap.org/data/2.5/forecast/daily");
        arguments.put("city", getSettingsValue(getString(R.string.pref_city_key), getString(R.string.pref_city_default)));
        new ForeCastAsyncTask(arguments).execute();
    }

    private String getSettingsValue(String key, String defaultValue)
    {
        return PreferenceManager.getDefaultSharedPreferences(
                getActivity()
        ).getString(key, defaultValue);
    }

    private class ForeCastAsyncTask extends AsyncTask<Void, Void, String[]> {

        private volatile HashMap<String, String> arguments;

        public ForeCastAsyncTask(HashMap<String, String> arguments) {
            super();
            this.arguments = arguments;
        }

        @Override
        protected String[] doInBackground(Void... test) {
            WeatherDataParser parser = new WeatherDataParser();
            if (isANotEnoughArguments()) {
                throw new IllegalArgumentException("Not Enough arguments");
            }
            int numDays = 7;
            return parser.fetchWeather(
                    arguments.get("method"),
                    arguments.get("link"),
                    arguments.get("city"),
                    arguments.get("units"),
                    numDays
            );
        }

        private boolean isANotEnoughArguments() {
            return !arguments.containsKey("method")
                    || !arguments.containsKey("link")
                    || !arguments.containsKey("units")
                    || !arguments.containsKey("city");
        }

        @Override
        protected void onPostExecute (String[] foreCastsArray) {
            if (foreCastsArray != null) {
                List<String> arrayList = new ArrayList<>(Arrays.asList(foreCastsArray));
                foreCasts.clear();
                foreCasts.addAll(arrayList);
            }
        }
    }
}