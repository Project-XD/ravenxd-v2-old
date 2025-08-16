package keystrokesmod.utility.profile;

import keystrokesmod.Client;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.utility.Utils;

import java.awt.*;
import java.io.IOException;

public class ProfileManagerModule extends Module {
    public static final ButtonSetting autosaveNotification
            = new ButtonSetting("AutoSave notification", false);

    public ProfileManagerModule() {
        super("Manager", category.profiles);
        this.registerSetting(new ButtonSetting("Create profile", ProfileManagerModule::createProfile));
        this.registerSetting(new ButtonSetting("Load profiles", ProfileManagerModule::loadProfiles));
        this.registerSetting(new ButtonSetting("Open folder", ProfileManagerModule::openFolder));
        this.registerSetting(autosaveNotification);
        ignoreOnSave = true;
        canBeEnabled = false;
    }

    public static void loadProfiles() {
        if (Utils.nullCheck() && Client.profileManager != null) {
            Client.profileManager.loadProfiles();
        }
    }

    public static void openFolder() {
        try {
            Desktop.getDesktop().open(Client.profileManager.directory);
        } catch (IOException ex) {
            Client.profileManager.directory.mkdirs();
            Utils.sendMessage("&cError locating folder, recreated.");
        }
    }

    public static void createProfile() {
        if (Utils.nullCheck() && Client.profileManager != null) {
            String name = "profile-";
            for (int i = 1; i <= 1000; i++) {
                if (Client.profileManager.getProfile(name + i) != null) {
                    continue;
                }
                name += i;
                Client.profileManager.saveProfile(new Profile(name, 0));
                Utils.sendMessage("&7Created profile: &b" + name);
                Client.profileManager.loadProfiles();
                break;
            }

            loadProfiles();
        }
    }
}
