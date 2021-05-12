// https://github.com/rii-mango/GIFTI-IO
package com.udec_biomed.aBrainVis.Tools.ThirdParty.Gifti;

public class GiftiFormatException extends Exception {

    private static final long serialVersionUID = 1L;



    /**
     * @param string
     */
    public GiftiFormatException(String string) {
        super(string);
    }



    /**
     * @param ex
     */
    public GiftiFormatException(Throwable ex) {
        super(ex);
    }
}