package org.dust.capApi;

import java.io.Serializable;

public class Resource implements Serializable {
    public String resourceDesc;
    public String mimeType;
    public int size;
    public String uri;
    public String derefUri;
    public String digest;

    public Resource(String resourceDesc)
    {
        this.resourceDesc = resourceDesc;
    }
}
