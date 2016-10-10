/*******************************************************************************
 * Copyright 2013-2014 Sergey Tarasevich
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.nostra13.universalimageloader.core.decode;

import android.annotation.TargetApi;
import android.graphics.BitmapFactory.Options;
import android.os.Build;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.download.ImageDownloader;

/**
 * Contains needed information for decoding image to Bitmap
 * 该类包含了将图片解码成Bitmap所有需要的信息（{@code 解码系数}）
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @since 1.8.3
 */
public class ImageDecodingInfo {
    /**
     * 内存缓存中的缓存图片所对应的KEY
     */
    private final String imageKey;
    /**
     * 磁盘缓存中的缓存图片对应的URI
     */
    private final String imageUri;
    /**
     * 传入到{@link com.nostra13.universalimageloader.core.ImageLoader}的原始地址
     */
    private final String originalImageUri;
    /**
     * {@link ImageSize}
     */
    private final ImageSize targetSize;

    /**
     * 图片的缩放类型
     */
    private final ImageScaleType imageScaleType;
    /**
     * 图片在View上的显示样式
     */
    private final ViewScaleType viewScaleType;

    /**
     * 图片在加载过程中的下载器{@link ImageDownloader}
     */
    private final ImageDownloader downloader;
    /**
     * {@link ImageDownloader#getStream(String, Object)}中的第二个Object参数
     */
    private final Object extraForDownloader;

    /**
     * 是否考虑EXIF参数
     */
    private final boolean considerExifParams;
    /**
     * {@link android.graphics.BitmapFactory.Options}
     */
    private final Options decodingOptions;

    public ImageDecodingInfo(String imageKey, String imageUri, String originalImageUri, ImageSize targetSize, ViewScaleType viewScaleType,
                             ImageDownloader downloader, DisplayImageOptions displayOptions) {
        this.imageKey = imageKey;
        this.imageUri = imageUri;
        this.originalImageUri = originalImageUri;
        this.targetSize = targetSize;

        this.imageScaleType = displayOptions.getImageScaleType();
        this.viewScaleType = viewScaleType;

        this.downloader = downloader;
        this.extraForDownloader = displayOptions.getExtraForDownloader();

        considerExifParams = displayOptions.isConsiderExifParams();
        decodingOptions = new Options();
        copyOptions(displayOptions.getDecodingOptions(), decodingOptions);
    }

    private void copyOptions(Options srcOptions, Options destOptions) {
        destOptions.inDensity = srcOptions.inDensity;
        destOptions.inDither = srcOptions.inDither;
        destOptions.inInputShareable = srcOptions.inInputShareable;
        destOptions.inJustDecodeBounds = srcOptions.inJustDecodeBounds;
        destOptions.inPreferredConfig = srcOptions.inPreferredConfig;
        destOptions.inPurgeable = srcOptions.inPurgeable;
        destOptions.inSampleSize = srcOptions.inSampleSize;
        destOptions.inScaled = srcOptions.inScaled;
        destOptions.inScreenDensity = srcOptions.inScreenDensity;
        destOptions.inTargetDensity = srcOptions.inTargetDensity;
        destOptions.inTempStorage = srcOptions.inTempStorage;
        if (Build.VERSION.SDK_INT >= 10) copyOptions10(srcOptions, destOptions);
        if (Build.VERSION.SDK_INT >= 11) copyOptions11(srcOptions, destOptions);
    }

    @TargetApi(10)
    private void copyOptions10(Options srcOptions, Options destOptions) {
        destOptions.inPreferQualityOverSpeed = srcOptions.inPreferQualityOverSpeed;
    }

    @TargetApi(11)
    private void copyOptions11(Options srcOptions, Options destOptions) {
        destOptions.inBitmap = srcOptions.inBitmap;
        destOptions.inMutable = srcOptions.inMutable;
    }

    /**
     * @return Original {@linkplain com.nostra13.universalimageloader.utils.MemoryCacheUtils#generateKey(String, ImageSize) image key} (used in memory cache).
     */
    public String getImageKey() {
        return imageKey;
    }

    /**
     * @return Image URI for decoding (usually image from disk cache)
     */
    public String getImageUri() {
        return imageUri;
    }

    /**
     * @return The original image URI which was passed to ImageLoader
     */
    public String getOriginalImageUri() {
        return originalImageUri;
    }

    /**
     * @return Target size for image. Decoded bitmap should close to this size according to {@linkplain ImageScaleType
     * image scale type} and {@linkplain ViewScaleType view scale type}.
     */
    public ImageSize getTargetSize() {
        return targetSize;
    }

    /**
     * @return {@linkplain ImageScaleType Scale type for image sampling and scaling}. This parameter affects result size
     * of decoded bitmap.
     */
    public ImageScaleType getImageScaleType() {
        return imageScaleType;
    }

    /**
     * @return {@linkplain ViewScaleType View scale type}. This parameter affects result size of decoded bitmap.
     */
    public ViewScaleType getViewScaleType() {
        return viewScaleType;
    }

    /**
     * @return Downloader for image loading
     */
    public ImageDownloader getDownloader() {
        return downloader;
    }

    /**
     * @return Auxiliary object for downloader
     */
    public Object getExtraForDownloader() {
        return extraForDownloader;
    }

    /**
     * @return <b>true</b> - if EXIF params of image should be considered; <b>false</b> - otherwise
     */
    public boolean shouldConsiderExifParams() {
        return considerExifParams;
    }

    /**
     * @return Decoding options
     */
    public Options getDecodingOptions() {
        return decodingOptions;
    }
}