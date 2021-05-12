// https://github.com/rii-mango/GIFTI-IO
package com.udec_biomed.aBrainVis.Tools.ThirdParty.Gifti;

import java.util.Map;


public interface MetadataHolder {

    /**
     * @param metadata
     */
    public void addMetadata(Map<String, String> metadata);
}