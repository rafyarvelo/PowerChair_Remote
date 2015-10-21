package com.smart.powerchair_remote;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.view.Display;
import com.smart.powerchair_remote.TelemetryBridge;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

//This is the Main Activity
public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        TelemetryFragment.OnFragmentInteractionListener,
        RemoteFragment.OnRemoteInteractionListener,
        HelpFragment.OnHelpSelectedListener,
        GoogleMapsFragment.OnFragmentInteractionListener
{

    private final int SMART_REMOTE = 0;
    private final int TM_STREAM    = 1;
    private final int MAP_SCREEN   = 2;
    private final int HELP_SCREEN  = 3;



    private TelemetryBridge tmBridge;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */

    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        tmBridge = TelemetryBridge.Instance();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));



    }


    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Fragment objFragment = null;

        //Switch between screens here
        switch (position)
        {
            case SMART_REMOTE:
                objFragment = RemoteFragment.Instance(tmBridge);
                break;
            case TM_STREAM:
                objFragment = TelemetryFragment.newInstance(tmBridge);
                break;
            case HELP_SCREEN:
                objFragment = new HelpFragment();
                break;
            case MAP_SCREEN:
                objFragment = GoogleMapsFragment.newInstance(tmBridge);
                ((GoogleMapsFragment) objFragment).setUpMapIfNeeded();
                break;
            default:
                objFragment = PlaceholderFragment.newInstance(position + 1);
                break;
        }
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, objFragment)
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number + 1) {
            case SMART_REMOTE:
                mTitle = getString(R.string.title_remote);
                break;
            case TM_STREAM:
                mTitle = getString(R.string.title_tm_stream);
                break;
            case HELP_SCREEN:
                mTitle = getString(R.string.title_help);
                break;
            case MAP_SCREEN:
                mTitle = getString(R.string.title_google_maps);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    public void onHelpButtonClicked(View v) {
        onNavigationDrawerItemSelected(HELP_SCREEN);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTelemetryFragmentInteraction()
    {
        TelemetryFragment fragment = (TelemetryFragment)
                getSupportFragmentManager().findFragmentById(R.id.telemetryFragment);

        if (fragment != null)
            fragment.updateTelemetryFields();
    }

    @Override
    public void onRemoteInteraction(RemoteFragment rf)
    {
        System.out.println("Starting Remote Fragment...");
    }

    @Override
    public void onMapInteraction(Uri uri)
    {
        System.out.println("Starting Map Fragment...");
    }

    @Override
    public void onHelpInteraction(Uri uri)
    {
        System.out.println("Starting Help Fragment...");
    }
    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}