package org.core.library;

import android.provider.MediaStore;

/**
 * Created by Zsago on 2016/9/29.
 */
public interface MediaColumns extends MediaStore.MediaColumns {
    /**
     * The latitude where the media was captured.
     * <P>Type: DOUBLE</P>
     */
    public static final String LATITUDE = "latitude";

    /**
     * The longitude where the media was captured.
     * <P>Type: DOUBLE</P>
     */
    public static final String LONGITUDE = "longitude";
    /**
     * The orientation for the media expressed as degrees.
     * Only degrees 0, 90, 180, 270 will work.
     * <P>Type: INTEGER</P>
     */
    public static final String ORIENTATION = "orientation";
    /**
     * The bucket id of the media. This is a read-only property that
     * is automatically computed from the DATA column.
     * <P>Type: TEXT</P>
     */
    public static final String BUCKET_ID = "bucket_id";

    /**
     * The bucket display name of the media. This is a read-only property that
     * is automatically computed from the DATA column.
     * <P>Type: TEXT</P>
     */
    public static final String BUCKET_DISPLAY_NAME = "bucket_display_name";
}
