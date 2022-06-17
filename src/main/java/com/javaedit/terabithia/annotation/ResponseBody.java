package com.javaedit.terabithia.annotation;

import java.lang.annotation.*;

/**
 * Annotation that indicates a method return value should be bound to the web
 * response body. Supported for annotated handler methods.
 *
 * <p>As of version 4.0 this annotation can also be added on the type level in
 * which case it is inherited and does not need to be added on the method level.
 *
 * @author Arjen Poutsma
 * @see RequestBody
 * @see RestController
 * @since 3.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResponseBody {

}
