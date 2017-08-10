package uk.kihira.crashcallback;

import joptsimple.internal.Strings;
import net.minecraftforge.fml.common.ICrashCallable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CrashHandler implements ICrashCallable {
    @Override
    public String getLabel() {
        return "crashcallback";
    }

    @Override
    public String call() throws Exception {
        URL url = new URL(CrashCallback.callbackUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(2000);
        conn.setReadTimeout(2000);

        if (CrashCallback.sendCrash) {
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    List<String> data = new ArrayList<>();
                    Path path = Paths.get("./crash-reports/");
                    Optional<Path> lastFile = Files.list(path)
                            .filter(f -> !Files.isDirectory(f))
                            .max((f1, f2) -> (int)(f1.toFile().lastModified() - f2.toFile().lastModified()));

                    if (lastFile.isPresent()) {
                        data =  Files.readAllLines(lastFile.get());
                    }
                    else {
                        data.add("Failed to load crash report");
                    }

                    // todo: Spawn a different process instead? This might get killed too soon

                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    writer.write(Strings.join(data, "\n"));
                    writer.flush();
                    writer.close();
                    os.close();

                    if (conn.getResponseCode() != CrashCallback.expectedResponse) {
                        System.err.println("Failed to call CrashCallback url\nGot bad response code: " + conn.getResponseCode());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
        }
        else {
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() != CrashCallback.expectedResponse) {
                return "Failed to call CrashCallback url\nGot bad response code: " + conn.getResponseCode();
            }
        }

        return "";
    }
}
