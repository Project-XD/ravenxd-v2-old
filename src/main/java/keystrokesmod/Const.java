package keystrokesmod;

import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Unmodifiable
public final class Const {
    public static final String NAME = "Raven XD v2";
    public static final String VERSION = ".1";
    public static final List<String> CHANGELOG = Collections.unmodifiableList(Arrays.asList(
            "-[+] **Recode** the entire thing, change to v2"
    ));
}
