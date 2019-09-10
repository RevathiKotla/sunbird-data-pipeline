package com.library.checksum.system;

import java.util.Map;

public interface Mappable {

    Map<String,Object> getMap();

    void setMetadata(Map<String, Object> metadata);
}
