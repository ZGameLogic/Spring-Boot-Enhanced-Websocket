package com.zgamelogic.websocket.annotations;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebSocketMapping {
    String Id();
    String SubId() default "";
}
