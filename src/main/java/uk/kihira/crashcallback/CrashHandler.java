package uk.kihira.crashcallback;

import net.minecraftforge.fml.common.ICrashCallable;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.HttpURLConnection;
import java.net.URL;

public class CrashHandler implements ICrashCallable {
    @Override
    public String getLabel() {
        return "zzz-crashcallback";
    }

    @Override
    public String call() throws Exception {
        //RuntimeMXBean factory = ManagementFactory.getRuntimeMXBean();
        //List<String> args = factory.getInputArguments();

        URL url = new URL(CrashCallback.callbackUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        //conn.setDoOutput(true);
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);

        conn.connect();

        if (conn.getResponseCode() != CrashCallback.expectedResponse) {
            return "Failed to call CrashCallback url\nGot bad response code: " + conn.getResponseCode();
        }

        return "";
    }
}
