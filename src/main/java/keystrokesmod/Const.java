package keystrokesmod;

import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Unmodifiable
public final class Const {
    public static final String NAME = "Raven XD ";
    public static final String VERSION = "v2.2";
    public static final List<String> CHANGELOG = Collections.unmodifiableList(Arrays.asList(
            "-[/] **Fix** versions being wonky"
    ));
}
