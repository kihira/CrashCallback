package uk.kihira.crashcallback;

import net.minecraft.util.StringUtils;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = "crashcallback", name = "CrashCallback", acceptedMinecraftVersions = "[1,)")
public class CrashCallback {
    public static final Logger logger = LogManager.getLogger("CrashCallback");
    public static String callbackUrl;
    public static int expectedResponse;
    public static boolean sendCrash;

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent e) {
        // Load configuration data first
        Configuration config = new Configuration(e.getSuggestedConfigurationFile());
        callbackUrl = config.getString("Callback", Configuration.CATEGORY_GENERAL, "", "The URL that is called on crash");
        expectedResponse = config.getInt("Response Code", Configuration.CATEGORY_GENERAL, 200, 0, 1000, "Expected HTTP response code from callback");
        sendCrash = config.getBoolean("Send Crash Report", Configuration.CATEGORY_GENERAL, false, "Whether to send the crash report to the server as a POST");
        config.save();

        // Load from command line (overrides config options)
        callbackUrl = System.getProperty("cc.callback", callbackUrl);
        expectedResponse = Integer.parseInt(System.getProperty("cc.response", String.valueOf(expectedResponse)));
        sendCrash = Boolean.parseBoolean(System.getProperty("cc.sendCrash", String.valueOf(sendCrash)));

        if (StringUtils.isNullOrEmpty(callbackUrl)) {
            logger.error("No callback URL is set!");
            return;
        }

        FMLCommonHandler.instance().registerCrashCallable(new CrashHandler());
    }

    @Mod.EventHandler
    public void onServerStart(FMLServerStartedEvent e) {
        throw new RuntimeException("Oh noes!");
    }
}
