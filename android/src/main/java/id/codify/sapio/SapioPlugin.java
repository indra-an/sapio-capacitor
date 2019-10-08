package id.codify.sapio;

import android.content.Context;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import credoapp.CredoAppService;
import credoapp.CredoAppException;
import credoapp.ErrorType;

@NativePlugin()
public class SapioPlugin extends Plugin {

    @PluginMethod()
    public void init(PluginCall call) {
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
