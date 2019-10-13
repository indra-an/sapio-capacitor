package id.codify.sapio;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.PluginRequestCodes;

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
        Manifest.permission.PACKAGE_USAGE_STATS
    }
)
public class SapioPlugin extends Plugin {
    static final int REQUEST_IMAGE_CAPTURE = PluginRequestCodes.CAMERA_IMAGE_CAPTURE;

    @PluginMethod()
    public void init(PluginCall call) {
        if (!hasRequiredPermissions()) {
            saveCall(call);
            pluginRequestAllPermissions();
        } else {
            try {
                Context context = getContext();
                String authKey = call.getString("authKey");
                String url = call.getString("url");
                String recordNumber = call.getString("recordNumber");

                CredoAppService credoAppService = new CredoAppService(context, url, authKey);

                credoAppService.collectData(recordNumber);

                JSObject ret = new JSObject();
                ret.put("message", "Success record " + recordNumber);
                call.success(ret);
            }
            catch (CredoAppException e){
                ErrorType errorType = e.getType();
                String message = e.getMessage();

                JSObject err = new JSObject();
                err.put("errorType", errorType);
                err.put("errorMessage", message);
                call.error(err.toString());
            }
        }
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
        }
    }
}
