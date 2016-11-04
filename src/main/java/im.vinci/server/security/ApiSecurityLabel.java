package im.vinci.server.security;

import java.lang.annotation.*;

/**
 * Created by tim@vinci on 16/7/16.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiSecurityLabel {
    boolean isCheckLogin() default false;
}
