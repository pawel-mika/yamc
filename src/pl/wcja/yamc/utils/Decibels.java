package pl.wcja.yamc.utils;

/**
 * Based on NAudio Decibels:
 * 
 * http://code.google.com/p/mimi-douban/source/browse/trunk/NAudio/Utils/Decibels.cs
 * 
 * @author:		<a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 * @date:		09-02-2012
 *
 */
public class Decibels {

	// 20 / ln( 10 )
    private final static double LOG_2_DB = 8.6858896380650365530225783783321;

    // ln( 10 ) / 20
    private final static double DB_2_LOG = 0.11512925464970228420089957273422;

    /**
     * Linear to dB conversion
     * @param lin linear value
     * @return decibel value
     */
    public static double linearToDecibels(double lin)
    {
        return Math.log(lin) * LOG_2_DB;
    }

    /**
     * dB to linear conversion
     * @param dB decibel value
     * @return linear value
     */
    public static double DecibelsToLinear(double dB)
    {
        return Math.exp(dB * DB_2_LOG);
    }
}
