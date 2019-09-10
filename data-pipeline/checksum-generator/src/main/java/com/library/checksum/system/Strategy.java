package com.library.checksum.system;

import java.util.Map;

public interface Strategy {
    Mappable generateChecksum(Mappable event);
}
