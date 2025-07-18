package com.zgamelogic.websocket.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebSocketMessage {
    private String type;
    private String subtype;
    private String replyId;
    private Object data;

    public WebSocketMessage(String type, Object data) {
        this.type = type;
        this.data = data;
    }

    public WebSocketMessage(String type, String replyId, Object data) {
        this.type = type;
        this.replyId = replyId;
        this.data = data;
    }

    public WebSocketMessage(String type, String subtype, String replyId, Object data) {
        this.type = type;
        this.subtype = subtype;
        this.replyId = replyId;
        this.data = data;
    }
}
