package ca.pkay.rcloneexplorer.Settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.os.Process;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.util.FLog;

public class LoggingSettingsFragment extends Fragment {

    private static final String TAG = "LoggingSettingsFragment";

    private Context context;
    private Switch useLogsSwitch;
    private View useLogsElement;
    private View sigquitElement;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LoggingSettingsFragment() {
    }

    public static LoggingSettingsFragment newInstance() {
        return new LoggingSettingsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment_logging, container, false);
        getViews(view);
        setDefaultStates();
        setClickListeners();

        if (getActivity() != null) {
            getActivity().setTitle(getString(R.string.logging_settings_header));
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private void getViews(View view) {
        useLogsSwitch = view.findViewById(R.id.use_logs_switch);
        useLogsElement = view.findViewById(R.id.use_logs);
        sigquitElement = view.findViewById(R.id.send_sigquit_to_rclone);
    }

    private void setDefaultStates() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean useLogs = sharedPreferences.getBoolean(getString(R.string.pref_key_logs), false);

        useLogsSwitch.setChecked(useLogs);
    }

    private void setClickListeners() {
        useLogsElement.setOnClickListener(v -> useLogsSwitch.setChecked(!useLogsSwitch.isChecked()));
        useLogsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> onUseLogsClicked(isChecked));
        sigquitElement.setOnClickListener(this::sigquitAll);
    }

    private void onUseLogsClicked(boolean isChecked) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.pref_key_logs), isChecked);
        editor.apply();
    }

    private void sigquitAll(View view) {
        Toast.makeText(context, "RCX: Stopping everything", Toast.LENGTH_LONG).show();
        try {
            Runtime runtime = Runtime.getRuntime();
            java.lang.Process process = runtime.exec("ps");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append('\n');
                output.append(line);
            }

            process.waitFor();

            final String regex = "\\s+(\\d+)\\s+\\d+\\s+\\d+\\s+.+librclone.+$";
            final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
            final Matcher matcher = pattern.matcher(output.toString());

            while (matcher.find()) {
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    String pidMatch = matcher.group(i);
                    if (null == pidMatch) {
                        continue;
                    }
                    int pid = Integer.parseInt(pidMatch);
                    FLog.i(TAG, "SIGQUIT to process pid=%s", pid);
                    Process.sendSignal(pid, Process.SIGNAL_QUIT);
                }
            }
            Process.killProcess(Process.myPid());
        } catch (IOException | InterruptedException e) {
            FLog.e(TAG, "Error executing shell commands", e);
        }
    }
}
