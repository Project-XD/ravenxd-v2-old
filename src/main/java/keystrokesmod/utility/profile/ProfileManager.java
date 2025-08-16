package keystrokesmod.utility.profile;

import com.google.gson.*;
import keystrokesmod.Client;
import keystrokesmod.clickgui.ClickGui;
import keystrokesmod.clickgui.components.impl.CategoryComponent;
import keystrokesmod.event.world.WorldChangeEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.client.Gui;
import keystrokesmod.module.impl.exploit.ClientSpoofer;
import keystrokesmod.module.impl.other.KillMessage;
import keystrokesmod.module.impl.render.HUD;
import keystrokesmod.module.impl.render.TargetHUD;
import keystrokesmod.module.impl.render.Watermark;
import keystrokesmod.module.setting.Setting;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.module.setting.interfaces.InputSetting;
import keystrokesmod.script.Manager;
import keystrokesmod.utility.Utils;
import keystrokesmod.eventbus.annotations.EventListener;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static keystrokesmod.Client.mc;

public class ProfileManager {
    public static final String PROFILE_VERSION = "1";

    public File directory;
    public final List<Profile> profiles = new ArrayList<>();

    public ProfileManager() {
        directory = new File(mc.mcDataDir + File.separator + "keystrokes", "profiles");
        if (!directory.exists()) {
            boolean success = directory.mkdirs();
            if (!success) {
                System.out.println("There was an issue creating profiles directory.");
                return;
            }
        }
        if (Objects.requireNonNull(directory.listFiles()).length == 0) { // if there's no profile in the folder upon launch,
            // create a new default profile
            saveProfile(new Profile("default", 0));
        }

        Client.getExecutor().scheduleWithFixedDelay(this::updateLatest, 2, 2, TimeUnit.MINUTES);
    }

    @EventListener
    public void onWorldChange(WorldChangeEvent event) {
        updateLatest();
    }

    public void updateLatest() {
        saveToLatest(fromCurrentState(-1));
    }

    private @NotNull JsonObject fromCurrentState(int keyBind) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("clientVersion", Client.VERSION);
        jsonObject.addProperty("profileVersion", PROFILE_VERSION);
        jsonObject.addProperty("clientName", Watermark.customName);
        jsonObject.addProperty("killmessage", KillMessage.killMessage);
        jsonObject.addProperty("clientbrand", ClientSpoofer.customBrand);
        jsonObject.addProperty("keybind", keyBind);
        JsonArray jsonArray = new JsonArray();
        for (Module module : Client.moduleManager.getModules()) {
            if (module.ignoreOnSave) {
                continue;
            }
            JsonObject moduleInformation = getJsonObject(module);
            jsonArray.add(moduleInformation);
        }
        if (Client.scriptManager != null && Client.scriptManager.scripts != null) {
            for (Module module : Client.scriptManager.scripts.values()) {
                if (module.ignoreOnSave) {
                    continue;
                }
                JsonObject moduleInformation = getJsonObject(module);
                jsonArray.add(moduleInformation);
            }
        }
        jsonObject.add("modules", jsonArray);
        return jsonObject;
    }

    public void saveProfile(@NotNull Profile profile) {
        JsonObject jsonObject = fromCurrentState(profile.getModule().getKeycode());
        try (FileWriter fileWriter = new FileWriter(new File(directory, profile.getName() + ".json"))) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(jsonObject, fileWriter);
        } catch (Exception e) {
            failedMessage("save", profile.getName());
            Utils.log.error(e);
        }

        saveToLatest(jsonObject);
    }

    public synchronized void saveToLatest(JsonObject jsonObject) {
        deleteProfile("latest");
        try (FileWriter fileWriter = new FileWriter(new File(directory, "latest.json"))) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(jsonObject, fileWriter);
        } catch (Exception ignored) {
        }
    }

    public static @NotNull JsonObject getJsonObject(@NotNull Module module) {
        JsonObject moduleInformation = new JsonObject();
        moduleInformation.addProperty("name", (module.moduleCategory() == Module.category.scripts && !(module instanceof Manager)) ?  "sc-" + module.getName() :  module.getName());
        moduleInformation.addProperty("prettyName", module.getRawPrettyName());
        moduleInformation.addProperty("prettyInfo", module.getRawPrettyInfo());
        if (module.canBeEnabled) {
            moduleInformation.addProperty("enabled", module.isEnabled());
            moduleInformation.addProperty("hidden", module.isHidden());
            moduleInformation.addProperty("keybind", module.getKeycode());
        }
        if (module instanceof HUD) {
            moduleInformation.addProperty("posX", HUD.posX);
            moduleInformation.addProperty("posY", HUD.posY);
        }
        if (module instanceof TargetHUD) {
            moduleInformation.addProperty("posX", TargetHUD.posX);
            moduleInformation.addProperty("posY", TargetHUD.posY);
        }
        if (module instanceof Gui) {
            JsonArray jsonArray = new JsonArray();
            for (CategoryComponent component : ClickGui.categories.values()) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("name", component.categoryName.name());
                jsonObject.addProperty("x", component.getX());
                jsonObject.addProperty("y", component.getY());
                jsonObject.addProperty("opened", component.fv());

                jsonArray.add(jsonObject);
            }

            moduleInformation.add("catPos", jsonArray);
        }
        for (Setting setting : module.getSettings()) {
            if (setting.viewOnly && !(module instanceof SubMode)) continue;

            if (setting instanceof ButtonSetting && !((ButtonSetting) setting).isMethodButton) {
                moduleInformation.addProperty(setting.getName(), ((ButtonSetting) setting).isToggled());
            } else if (setting instanceof InputSetting) {
                moduleInformation.addProperty(setting.getName(), ((InputSetting) setting).getInput());
            }
        }
        return moduleInformation;
    }

    public void loadProfile() {
        if (getProfileFiles().stream().anyMatch(file -> file.getName().equals("latest.json"))) {
            loadProfile("latest");
        } else {
            loadProfile("default");
        }
    }

    public void loadProfile(String name) {
        for (File file : getProfileFiles()) {
            if (!file.exists()) {
                failedMessage("load", name);
                System.out.println("Failed to load " + name);
                return;
            }
            if (!file.getName().equals(name + ".json")) {
                continue;
            }
            if (Client.scriptManager != null) {
                for (Module module : Client.scriptManager.scripts.values()) {
                    if (module.canBeEnabled()) {
                        module.disable();
                        module.setBind(0);
                    }
                }
            }
            for (Module module : Client.getModuleManager().getModules()) {
                if (module.canBeEnabled()) {
                    module.disable();
                    module.setBind(0);
                }
            }
            try (FileReader fileReader = new FileReader(file)) {
                JsonParser jsonParser = new JsonParser();
                JsonObject profileJson = jsonParser.parse(fileReader).getAsJsonObject();
                if (profileJson == null) {
                    failedMessage("load", name);
                    return;
                }
                JsonArray modules = profileJson.getAsJsonArray("modules");
                if (modules == null) {
                    failedMessage("load", name);
                    return;
                }
//                if (profileJson.has("profileVersion")) {
//                    String profileVersion = profileJson.get("profileVersion").getAsString();
//                    if (!profileVersion.equals(PROFILE_VERSION)) {
//                        Notifications.sendNotification(Notifications.NotificationTypes.WARN, String.format(
//                                "Outdated profile! The profile version is '%s', but the client supports '%s'.",
//                                profileVersion, PROFILE_VERSION
//                        ), 10000);
//                    }
//                }
                if (profileJson.has("clientName")) {
                    Watermark.customName = profileJson.get("clientName").getAsString();
                }
                if (profileJson.has("killmessage")) {
                    KillMessage.killMessage = profileJson.get("killmessage").getAsString();
                }
                if (profileJson.has("clientbrand")) {
                    ClientSpoofer.customBrand = profileJson.get("clientbrand").getAsString();
                }

                for (JsonElement moduleJson : modules) {
                    JsonObject moduleInformation = moduleJson.getAsJsonObject();
                    String moduleName = moduleInformation.get("name").getAsString();
                    switch (moduleName) {
                        case "AntiKnockback":
                            moduleName = "Velocity";
                            break;
                        case "Bhop":
                            moduleName = "Speed";
                            break;
                        case "SuperKB":
                            moduleName = "MoreKB";
                            break;
                    }

                    if (moduleName.isEmpty()) {
                        continue;
                    }

                    Module module = Client.moduleManager.getModule(moduleName);
                    if (module == null && moduleName.startsWith("sc-") && Client.scriptManager != null) {
                        for (Module module1 : Client.scriptManager.scripts.values()) {
                            if (module1.getName().equals(moduleName.substring(3))) {
                                module = module1;
                            }
                        }
                    }

                    if (module == null) {
                        continue;
                    }

                    loadFromJsonObject(moduleInformation, module);

                    Client.currentProfile = getProfile(name);
                }

                if (!Objects.equals(name, "latest")) {
                    saveToLatest(profileJson);
                }
            } catch (Exception e) {
                failedMessage("load", name);
                Utils.log.error(e);
                if (Objects.equals(name, "latest")) {
                    loadProfile("default");
                }
            }
        }
    }

    public static void loadFromJsonObject(@NotNull JsonObject moduleInformation, Module module) {
        if (moduleInformation.has("prettyName")) {
            module.setPrettyName(moduleInformation.get("prettyName").getAsString());
        }

        if (moduleInformation.has("prettyInfo")) {
            module.setPrettyInfo(moduleInformation.get("prettyInfo").getAsString());
        }

        if (module.canBeEnabled() && !(module instanceof SubMode)) {
            if (moduleInformation.has("enabled")) {
                boolean enabled = moduleInformation.get("enabled").getAsBoolean();
                if (enabled) {
                    module.enable();
                } else {
                    module.disable();
                }
            }
            if (moduleInformation.has("hidden")) {
                boolean hidden = moduleInformation.get("hidden").getAsBoolean();
                module.setHidden(hidden);
            }
            if (moduleInformation.has("keybind")) {
                int keybind = moduleInformation.get("keybind").getAsInt();
                module.setBind(keybind);
            }
        }

        if (module.getName().equals("HUD")) {
            if (moduleInformation.has("posX")) {
                HUD.posX = moduleInformation.get("posX").getAsInt();
            }
            if (moduleInformation.has("posY")) {
                HUD.posY = moduleInformation.get("posY").getAsInt();
            }
        }

        if (module.getName().equals("TargetHUD")) {
            if (moduleInformation.has("posX")) {
                TargetHUD.posX = moduleInformation.get("posX").getAsInt();
            }
            if (moduleInformation.has("posY")) {
                TargetHUD.posY = moduleInformation.get("posY").getAsInt();
            }
        }

        if (module.getName().equals("Gui")) {
            if (moduleInformation.has("catPos")) {
                ArrayList<CategoryComponent> movedCategories = new ArrayList<>(ClickGui.categories.size());
                for (JsonElement jsonElement : moduleInformation.get("catPos").getAsJsonArray()) {
                    try {
                        JsonObject jsonCat = jsonElement.getAsJsonObject();
                        CategoryComponent component = ClickGui.categories.values().stream()
                                .filter(c -> c.categoryName.name().equals(jsonCat.get("name").getAsString()))
                                .findAny()
                                .orElseThrow(NoSuchElementException::new);

                        if (jsonCat.has("x")) {
                            component.x(jsonCat.get("x").getAsInt());
                        }
                        if (jsonCat.has("y")) {
                            component.y(jsonCat.get("y").getAsInt());
                        }
                        if (jsonCat.has("opened")) {
                            component.fv(jsonCat.get("opened").getAsBoolean());
                        }

                        movedCategories.add(component);
                    } catch (NoSuchElementException ignored) {
                    }
                }
                for (CategoryComponent component : movedCategories) {
                    ClickGui.categories.replace(component.categoryName, component);
                }
            }
        }

        for (Setting setting : module.getSettings()) {
            setting.loadProfile(moduleInformation);
        }
    }

    public void deleteProfile(String name) {
        profiles.removeIf(profile -> profile.getName().equals(name));
        if (directory.exists()) {
            File[] files = directory.listFiles();
            assert files != null;
            for (File file : files) {
                if (file.getName().equals(name + ".json")) {
                    file.delete();
                }
            }
        }
    }

    public void loadProfiles() {
        profiles.clear();
        if (directory.exists()) {
            File[] files = directory.listFiles();
            assert files != null;
            for (File file : files) {
                try (FileReader fileReader = new FileReader(file)) {
                    JsonParser jsonParser = new JsonParser();
                    JsonObject profileJson = jsonParser.parse(fileReader).getAsJsonObject();
                    String profileName = file.getName().replace(".json", "");

                    if (profileJson == null) {
                        failedMessage("load", profileName);
                        return;
                    }

                    int keybind = 0;

                    if (profileJson.has("keybind")) {
                        keybind = profileJson.get("keybind").getAsInt();
                    }

                    Profile profile = new Profile(profileName, keybind);
                    profiles.add(profile);
                } catch (Exception e) {
                    Utils.sendMessage("&cFailed to load profiles.");
                    e.printStackTrace();
                }
            }

            for (CategoryComponent categoryComponent : ClickGui.categories.values()) {
                if (categoryComponent.categoryName == Module.category.profiles) {
                    categoryComponent.reloadModules(true);
                }
            }
            Utils.sendMessage("&b" + Client.profileManager.getProfileFiles().size() + " &7profiles loaded.");
        }
    }

    public List<File> getProfileFiles() {
        List<File> profileFiles = new ArrayList<>();
        if (directory.exists()) {
            File[] files = directory.listFiles();
            assert files != null;
            for (File file : files) {
                if (!file.getName().endsWith(".json")) {
                    continue;
                }
                profileFiles.add(file);
            }
        }
        return profileFiles;
    }

    public Profile getProfile(String name) {
        for (Profile profile : profiles) {
            if (profile.getName().equals(name)) {
                return profile;
            }
        }
        return null;
    }

    public void failedMessage(String reason, String name) {
        Utils.sendMessage("&cFailed to " + reason + ": &b" + name);
    }
}
