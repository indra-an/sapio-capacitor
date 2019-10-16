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
import com.getcapacitor.PluginRequestCodes;

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
    }
)

public class SapioPlugin extends Plugin {
    static final int REQUEST_IMAGE_CAPTURE = PluginRequestCodes.CAMERA_IMAGE_CAPTURE;

    @PluginMethod()

    public void init(PluginCall call) {

//        if (!hasRequiredPermissions()) {
//            saveCall(call);
//            pluginRequestAllPermissions();
//
//            pluginRequestPermissions(new String[] {
//                    Manifest.permission.INTERNET,
//                    Manifest.permission.READ_EXTERNAL_STORAGE,
//                    Manifest.permission.READ_CONTACTS,
//                    Manifest.permission.READ_CALENDAR,
//                    Manifest.permission.GET_ACCOUNTS,
//                    Manifest.permission.READ_PHONE_STATE,
//                    Manifest.permission.BLUETOOTH,
//                    Manifest.permission.ACCESS_WIFI_STATE,
//                    Manifest.permission.PACKAGE_USAGE_STATS
//            }, REQUEST_IMAGE_CAPTURE);
//        } else {
        Context context = getContext();
        String authKey = call.getString("authKey");
        String url = call.getString("url");
        String recordNumber = call.getString("recordNumber");
        Boolean ignorePermission = call.getBoolean("ignorePermission");

        try {

            CredoAppService credoAppService = new CredoAppService(context, url, authKey);
            credoAppService.setIgnorePermission(ignorePermission);
            final Collection<String> ungrantedPermissions = credoAppService.getUngrantedPermissions();

            // Check data usage statistics permission. In order to access Application and Network usage statistics, the user should manually grant 'Usage Stats' permission
            if (ungrantedPermissions.contains("android.permission.PACKAGE_USAGE_STATS")) {
                startActivityForResult(getSavedCall(), new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), 0);
                init(call);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !ungrantedPermissions.isEmpty()) {
                pluginRequestPermissions(ungrantedPermissions.toArray(new String[ungrantedPermissions.size()]), REQUEST_IMAGE_CAPTURE);
                init(call);
            }else{

                final String res = credoAppService.collectData(recordNumber);

                JSObject ret = new JSObject();
                ret.put("result", res);
                ret.put("message", "Success record " + recordNumber);
                call.success(ret);
            }

        }
        catch (CredoAppException e){
            ErrorType errorType = e.getType();
            String message = e.getMessage();

            JSObject err = new JSObject();
            err.put("errorType", errorType);
            err.put("errorMessage", message);
            call.error(err.toString());
        }
//        }
    }

    @Override
    protected void handleRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.handleRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d("request_permission", "handling request perms result");
        PluginCall savedCall = getSavedCall();
        if (savedCall == null) {
            Log.d("request_permission", "No stored plugin call for permissions request result");
            return;
        }

        for(int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                savedCall.error("User denied permission");
                return;
            }
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            // We got the permission
            init(savedCall);
        }
    }
}
