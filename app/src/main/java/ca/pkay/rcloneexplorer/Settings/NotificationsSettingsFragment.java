package ca.pkay.rcloneexplorer.Settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import ca.pkay.rcloneexplorer.BuildConfig;
import ca.pkay.rcloneexplorer.R;
import es.dmoral.toasty.Toasty;

public class NotificationsSettingsFragment extends Fragment {

    private Context context;
    private View notificationsElement;
    private View appUpdatesElement;
    private Switch appUpdatesSwitch;
    private Switch mNotificationReportSwitch;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NotificationsSettingsFragment() {
    }

    public static NotificationsSettingsFragment newInstance() {
        return new NotificationsSettingsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment_notification, container, false);
        getViews(view);
        setDefaultStates();
        setClickListeners();

        if (getActivity() != null) {
            getActivity().setTitle(getString(R.string.notifications_pref_title));
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private void getViews(View view) {
        notificationsElement = view.findViewById(R.id.notifications);
        appUpdatesElement = view.findViewById(R.id.app_updates);
        appUpdatesSwitch = view.findViewById(R.id.app_updates_switch);
        mNotificationReportSwitch = view.findViewById(R.id.app_notification_report_switch);
    }

    private void setDefaultStates() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean appUpdates = sharedPreferences.getBoolean(getString(R.string.pref_key_app_updates), true);
        boolean notificationReports = sharedPreferences.getBoolean(getString(R.string.pref_key_app_notification_reports), true);

        appUpdatesSwitch.setChecked(appUpdates);
        mNotificationReportSwitch.setChecked(notificationReports);
    }

    private void setClickListeners() {

        notificationsElement.setOnClickListener(v -> onNotificationsClicked());
        appUpdatesElement.setOnClickListener(v -> {
            if (appUpdatesSwitch.isChecked()) {
                appUpdatesSwitch.setChecked(false);
            } else {
                appUpdatesSwitch.setChecked(true);
            }
        });
        appUpdatesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> onAppUpdatesClicked(isChecked));


        mNotificationReportSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> onNotificationReportsClicked(isChecked));
    }

    private void onNotificationsClicked() {
        Intent intent = new Intent();
        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");

        //for Android 5-7
        intent.putExtra("app_package", context.getPackageName());
        intent.putExtra("app_uid", context.getApplicationInfo().uid);

        // for Android O
        intent.putExtra("android.provider.extra.APP_PACKAGE", context.getPackageName());

        if (intent.resolveActivity(context.getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toasty.error(context, "Couldn't find activity to start", Toast.LENGTH_SHORT, true).show();
        }
    }

    private void onAppUpdatesClicked(boolean isChecked) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.pref_key_app_updates), isChecked);
        editor.apply();
    }

    private void onNotificationReportsClicked(boolean isChecked) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.pref_key_app_notification_reports), isChecked);
        editor.apply();
    }
}
