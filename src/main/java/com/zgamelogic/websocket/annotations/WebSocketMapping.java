package com.zgamelogic.websocket.annotations;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebSocketMapping {
    String type();
    String subtype() default "";
}
