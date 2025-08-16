package keystrokesmod.eventbus.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 尽量使用public方法，以获得更高的性能
 * 较高的优先级会被更早调用，较低的优先级更晚调用
 * 相反，较高的优先级设置的event数据更可能被较低优先级的listener覆盖
 * 不应依赖相同priority值时的顺序
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventListener {
    int priority() default 0;
}
