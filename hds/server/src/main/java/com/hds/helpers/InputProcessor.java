package com.hds.helpers;

public class InputProcessor {

    private InputProcessor() {
        // This is here so the class can't be instantiated. //
    }

    public static boolean simpleValidate(String str) {
        return str != null && !str.equals("");
    }
}
