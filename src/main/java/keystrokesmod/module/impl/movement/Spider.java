package keystrokesmod.module.impl.movement;

import keystrokesmod.module.Module;
import keystrokesmod.module.impl.movement.spider.IntaveSpider;
import keystrokesmod.module.impl.movement.spider.TestSpider;
import keystrokesmod.module.impl.movement.spider.VulcanSpider;
import keystrokesmod.module.setting.impl.ModeValue;

public class Spider extends Module {
    private final ModeValue mode;

    public Spider() {
        super("Spider", category.movement);
        this.registerSetting(mode = new ModeValue("Mode", this)
                .add(new IntaveSpider("Intave", this))
                .add(new VulcanSpider("Vulcan", this))
                .add(new TestSpider("Test", this))
        );
    }

    @Override
    public void onEnable() {
        mode.enable();
    }

    @Override
    public void onDisable() {
        mode.disable();
    }

    @Override
    public String getInfo() {
        return mode.getSubModeValues().get((int) mode.getInput()).getPrettyName();
    }
}
