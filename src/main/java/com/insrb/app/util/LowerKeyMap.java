package com.insrb.app.util;

import java.util.HashMap;

public class LowerKeyMap extends HashMap<String,Object> {

    private static final long serialVersionUID = 1L;

    @Override
    public Object put(String key, Object value) {
        return super.put(key.toLowerCase(), value);
    }

}