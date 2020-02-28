package id.codify.sapio;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

import java.util.Collection;

import credoapp.CredoAppService;
import credoapp.CredoAppException;
import credoapp.ErrorType;


@NativePlugin(
        permissions={
                Manifest.permission.INTERNET,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.GET_ACCOUNTS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.PACKAGE_USAGE_STATS,
        },
        requestCodes={ 1234, 12345}
)

public class SapioPlugin extends Plugin {
    static final int REQUEST_CAPACITOR_CODE = 12345;
    static final int REQUEST_ACTION_USAGE = 1234;
    boolean grantedSettingPerm = false;

    @PluginMethod()
    public void init(PluginCall call) {

        Context context = getContext();
        String authKey = call.getString("authKey");
        String url = call.getString("url");
        String recordNumber = call.getString("recordNumber");
        Boolean ignoreDeniedPermission = call.getBoolean("ignoreDeniedPermission");
        Boolean requestPackagePermission = call.getBoolean("requestPackagePermission");
        grantedSettingPerm = call.getBoolean("ignoreDeniedPermission");
        try {

            CredoAppService credoAppService = new CredoAppService(context, url, authKey);
            credoAppService.setIgnorePermission(ignoreDeniedPermission);
            final Collection<String> ungrantedPermissions = credoAppService.getUngrantedPermissions();

            if (!ignoreDeniedPermission) {
                // Check data usage statistics permission. In order to access Application and Network usage statistics, the user should manually grant 'Usage Stats' permission
                if (ungrantedPermissions.contains("android.permission.PACKAGE_USAGE_STATS") && requestPackagePermission) {
                    saveCall(call);
                    startActivityForResult(getSavedCall(), new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), REQUEST_ACTION_USAGE);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !ungrantedPermissions.isEmpty()) {
                    saveCall(call);
                    pluginRequestPermissions(ungrantedPermissions.toArray(new String[ungrantedPermissions.size()]), REQUEST_CAPACITOR_CODE);
                }

                grantedSettingPerm = ungrantedPermissions.contains("android.permission.PACKAGE_USAGE_STATS");
            }

            final String rn = credoAppService.collectData(recordNumber);

            JSObject ret = new JSObject();
            ret.put("recordNumber", rn);
            ret.put("message", "Success record data");
            call.success(ret);

        }
        catch (CredoAppException e){
            ErrorType errorType = e.getType();
            String message = e.getMessage();

            JSObject err = new JSObject();
            err.put("errorType", errorType);
            err.put("errorMessage", message);
            err.put("hasSettingPerm", grantedSettingPerm);
            call.error(err.toString());
        }
    }

    @Override
    protected void handleOnActivityResult(int requestCode, int resultCode, Intent data) {
        super.handleOnActivityResult(requestCode, resultCode, data);
        Log.d(getLogTag(), "handling request activity perms result");

        PluginCall savedCall = getSavedCall();

        if (savedCall == null) {
            return;
        }

        if (requestCode == REQUEST_ACTION_USAGE) {
            Log.d(getLogTag(), "User granted activity permission");

            JSObject ret = new JSObject();
            ret.put("grantedSettingPerm", true);
            savedCall.success(ret);
        }
    }

    @Override
    protected void handleRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.handleRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(getLogTag(), "handling request perms result");

        PluginCall savedCall = getSavedCall();
        if (savedCall == null) {
            return;
        }

        for(int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                JSObject err = new JSObject();
                err.put("errorType", "UngrantedPermissions");
                err.put("errorMessage", "User denied permission");
                err.put("hasSettingPerm", grantedSettingPerm);
                savedCall.error(err.toString());
                return;
            }
        }

        if(requestCode == REQUEST_CAPACITOR_CODE){
            Log.d(getLogTag(), "User granted permission");

            JSObject ret = new JSObject();
            ret.put("grantedPerm", true);
            savedCall.success(ret);
        }
    }

}
