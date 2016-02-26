package org.devocative.demeter.iservice.persistor;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface FilterOption {
	boolean useLike() default true;

	String property() default "";
}
