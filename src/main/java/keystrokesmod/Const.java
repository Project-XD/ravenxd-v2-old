package keystrokesmod;

import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Unmodifiable
public final class Const {
    public static final String NAME = "Raven XD";
    public static final String VERSION = "3.1";
    public static final List<String> CHANGELOG = Collections.unmodifiableList(Arrays.asList(
            "-[+] **Add** 'SelfBow' mode to Fly",
            "-[+] **Add** modes 'LiquidBounce' and 'Ring' to TargetESP",
            "-[+] **Add** BowAimbot (still a bit buggy, can't silent grim. also auto shoot doesn't work)",
            "-[+] **Add** attack timing modes 'Pre', 'Post', and 'Switch' to KillAura (Post is commonly detected)"
    ));
}
