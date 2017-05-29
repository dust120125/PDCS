package org.dust.capApi;

import java.io.Serializable;

public class Value implements Serializable {
    public String valueName;
    public String value;

    public Value(String valueName, String value)
    {
        this.valueName = valueName;
        this.value = value;
    }
}
