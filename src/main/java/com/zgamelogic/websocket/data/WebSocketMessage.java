package com.zgamelogic.websocket.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebSocketMessage {
    @JsonView(Object.class)
    private String type;
    @JsonView(Object.class)
    private String subtype;
    @JsonView(Object.class)
    private String replyId;
    @JsonView(Object.class)
    private Object data;
    @JsonView(Object.class)
    private Integer statusCode;

    public WebSocketMessage(String type, Object data) {
        this.type = type;
        this.data = data;
        statusCode = 200;
    }

    public WebSocketMessage(String type, String replyId, Object data) {
        this.type = type;
        this.replyId = replyId;
        this.data = data;
        statusCode = 200;
    }

    public WebSocketMessage(String type, String subtype, String replyId, Object data) {
        this.type = type;
        this.subtype = subtype;
        this.replyId = replyId;
        this.data = data;
        statusCode = 200;
    }

    public WebSocketMessage(String type, String subtype, String replyId, Integer statusCode, Object data) {
        this.type = type;
        this.subtype = subtype;
        this.replyId = replyId;
        this.data = data;
        this.statusCode = statusCode;
    }

    public WebSocketMessage(String type, Integer statusCode, Object data) {
        this.type = type;
        this.data = data;
        this.statusCode = statusCode;
    }

    public WebSocketMessage(String type, String replyId, Integer statusCode, Object data) {
        this.type = type;
        this.replyId = replyId;
        this.data = data;
        this.statusCode = statusCode;
    }
}
