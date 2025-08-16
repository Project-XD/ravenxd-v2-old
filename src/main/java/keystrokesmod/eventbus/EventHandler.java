package keystrokesmod.eventbus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.function.Consumer;

@Getter
@AllArgsConstructor
public final class EventHandler<T extends Event> {
    private final @Nullable Method method;
    private final @Nullable Object object;
    private final Class<T> eventType;
    private final Consumer<T> eventConsumer;
    private final int priory;

    public void invoke(T event) {
        eventConsumer.accept(event);
    }
}
