package keystrokesmod.utility.profile;

import keystrokesmod.Client;
import keystrokesmod.clickgui.ClickGui;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.client.Settings;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.utility.Utils;

public class ProfileModule extends Module {
    private static long lastToggleTime = -1;

    private final Profile profile;
    public boolean saved = true;

    public ProfileModule(Profile profile, String name, int bind) {
        super(name, category.profiles, bind);
        this.profile = profile;
        this.registerSetting(new ButtonSetting("Save profile", () -> {
            Utils.sendMessage("&7Saved profile: &b" + getName());
            Client.profileManager.saveProfile(this.profile);
            saved = true;
        }));
        this.registerSetting(new ButtonSetting("Remove profile", () -> {
            Utils.sendMessage("&7Removed profile: &b" + getName());
            Client.profileManager.deleteProfile(getName());
        }));
    }

    @Override
    public void toggle() {
        long curTime = System.currentTimeMillis();
        if (curTime - lastToggleTime < 50)
            return;
        lastToggleTime = curTime;

        if (mc.currentScreen instanceof ClickGui || mc.currentScreen == null) {
            if (this.profile == Client.currentProfile) {
                return;
            }
            Client.profileManager.loadProfile(this.getName());

            Client.currentProfile = profile;

            if (Settings.sendMessage.isToggled()) {
                Utils.sendMessage("&7Enabled profile: &b" + this.getName());
            }
            saved = true;
        }
    }

    @Override
    public boolean isEnabled() {
        if (Client.currentProfile == null) {
            return false;
        }
        return Client.currentProfile.getModule() == this;
    }
}
