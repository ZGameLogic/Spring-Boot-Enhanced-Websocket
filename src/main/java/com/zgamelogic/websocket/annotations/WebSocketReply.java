package com.zgamelogic.websocket.annotations;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebSocketReply {
    ReplyType value() default ReplyType.SESSION;
    String attribute() default "";
}
