package caret.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;

@Retention(RUNTIME)
public @interface GeneratedContainer {
 Generated[] value();
}