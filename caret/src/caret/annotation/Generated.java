package caret.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Repeatable;

@Retention(RUNTIME)
@Repeatable(GeneratedContainer.class)
public @interface Generated {
    String agent();
    String task();
    String id();
    String timestamp();
}
