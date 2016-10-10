package cn.finalteam.rxgalleryfinal;

import static cn.finalteam.rxgalleryfinal.Configuration.VERSION_CODE_100;
import static cn.finalteam.rxgalleryfinal.Configuration.VERSION_CODE_LAST;

/**
 * Created by Zsago on 2016/9/29.
 *
 * @since 1.1.0
 */
public enum Version {
    VERSION_1_0_0(VERSION_CODE_100, "1.0.0"), VERSION_LAST(VERSION_CODE_LAST, "1.1.0");

    int versionCode;
    String versionName;

    Version(int versionCode, String versionName) {
        this.versionCode = versionCode;
        this.versionName = versionName;
    }
}
