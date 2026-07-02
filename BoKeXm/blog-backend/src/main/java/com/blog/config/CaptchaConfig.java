package com.blog.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class CaptchaConfig {

    private int width = 120;
    private int height = 40;
    private int charLength = 4;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getCharLength() {
        return charLength;
    }

    public void setCharLength(int charLength) {
        this.charLength = charLength;
    }
}
