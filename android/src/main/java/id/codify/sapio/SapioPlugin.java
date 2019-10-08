package id.codify.sapio;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

@NativePlugin()
public class SapioPlugin extends Plugin {

    @PluginMethod()
    public void init(PluginCall call) {
        String authKey = call.getString("authKey");
        String url = call.getString("url");
        String recordNumber = call.getString("recordNumber");

        JSObject ret = new JSObject();
        ret.put("authKey", authKey);
        ret.put("url", url);
        ret.put("recordNumber", recordNumber);
        call.success(ret);
    }
}
