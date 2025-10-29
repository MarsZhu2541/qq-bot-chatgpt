package com.mars.deltaforce.exception;

public class PlayerNotLogin extends RuntimeException {
    public PlayerNotLogin() {
        super("请先扫码登陆");
    }

    public PlayerNotLogin(String message) {
        super(message);
    }
}
