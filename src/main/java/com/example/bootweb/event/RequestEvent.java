package com.example.bootweb.event;

import jdk.jfr.*;

@Name("com.example.bootweb.Request")
@Label("HTTP Request")
@Category({"HTTP", "Request"})
public class RequestEvent extends Event {

    @Label("HTTP method")
    public String method;

    @Label("HTTP path")
    public String path;

    @Label("HTTP body size")
    @DataAmount
    public int size;
}
