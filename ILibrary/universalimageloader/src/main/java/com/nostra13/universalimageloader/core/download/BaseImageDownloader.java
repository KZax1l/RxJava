/*******************************************************************************
 * Copyright 2011-2014 Sergey Tarasevich
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
package com.nostra13.universalimageloader.core.download;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ContentLengthInputStream;
import com.nostra13.universalimageloader.utils.IoUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Provides retrieving of {@link InputStream} of image by URI from network or file system or app resources.<br />
 * {@link URLConnection} is used to retrieve image stream from network.
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @since 1.8.0
 */
public class BaseImageDownloader implements ImageDownloader {
    /**
     * {@value}
     * 默认的网络连接超时时间
     */
    public static final int DEFAULT_HTTP_CONNECT_TIMEOUT = 5 * 1000; // milliseconds
    /**
     * {@value}
     * 默认的读取网络数据的超时时间
     */
    public static final int DEFAULT_HTTP_READ_TIMEOUT = 20 * 1000; // milliseconds

    /**
     * {@value}
     */
    protected static final int BUFFER_SIZE = 32 * 1024; // 32 Kb
    /**
     * {@value}
     */
    protected static final String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";

    protected static final int MAX_REDIRECT_COUNT = 5;

    /**
     * {@link android.provider.ContactsContract#AUTHORITY_URI}
     * 系统联系人的数据库Uri的开头字符串
     */
    protected static final String CONTENT_CONTACTS_URI_PREFIX = "content://com.android.contacts/";

    private static final String ERROR_UNSUPPORTED_SCHEME = "UIL doesn't support scheme(protocol) by default [%s]. " + "You should implement this support yourself (BaseImageDownloader.getStreamFromOtherSource(...))";

    protected final Context context;
    protected final int connectTimeout;
    protected final int readTimeout;

    public BaseImageDownloader(Context context) {
        this(context, DEFAULT_HTTP_CONNECT_TIMEOUT, DEFAULT_HTTP_READ_TIMEOUT);
    }

    public BaseImageDownloader(Context context, int connectTimeout, int readTimeout) {
        this.context = context.getApplicationContext();
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    @Override
    public InputStream getStream(String imageUri, Object extra) throws IOException {
        switch (Scheme.ofUri(imageUri)) {
            case HTTP:
            case HTTPS:
                return getStreamFromNetwork(imageUri, extra);
            case FILE:
                return getStreamFromFile(imageUri, extra);
            case CONTENT:
                return getStreamFromContent(imageUri, extra);
            case ASSETS:
                return getStreamFromAssets(imageUri, extra);
            case DRAWABLE:
                return getStreamFromDrawable(imageUri, extra);
            case UNKNOWN:
            default:
                return getStreamFromOtherSource(imageUri, extra);
        }
    }

    /**
     * Retrieves {@link InputStream} of image by URI (image is located in the network).
     * <p>
     * 通过网络请求，获取网络图片数据流
     *
     * @param imageUri Image URI
     * @param extra    Auxiliary object which was passed to {@link DisplayImageOptions.Builder#extraForDownloader(Object)
     *                 DisplayImageOptions.extraForDownloader(Object)}; can be null
     * @return {@link InputStream} of image
     * @throws IOException if some I/O error occurs during network request or if no InputStream could be created for
     *                     URL.
     */
    protected InputStream getStreamFromNetwork(String imageUri, Object extra) throws IOException {
        HttpURLConnection conn = createConnection(imageUri, extra);

        int redirectCount = 0;
        /**
         * HTTP响应状态码通常由一个三位数组成，而以3开头的三位数状态码表示重定向类，为了完成指定的动作，必须接受进一步处理
         */
        while (conn.getResponseCode() / 100 == 3 && redirectCount < MAX_REDIRECT_COUNT) {
            /**
             * 参见HTTP协议的响应头Location，表示用于重定向一个新的位置；Location响应报头域常用在更换域名的时候
             */
            conn = createConnection(conn.getHeaderField("Location"), extra);
            redirectCount++;
        }

        InputStream imageStream;
        try {
            /**
             * 获取从此打开的链接中读取的输入流
             */
            imageStream = conn.getInputStream();
        } catch (IOException e) {
            // Read all data to allow reuse connection (http://bit.ly/1ad35PY)
            IoUtils.readAndCloseStream(conn.getErrorStream());
            throw e;
        }
        if (!shouldBeProcessed(conn)) {
            IoUtils.closeSilently(imageStream);
            throw new IOException("Image request failed with response code " + conn.getResponseCode());
        }

        /**
         * conn.getContentLength表示获取响应头Content-Length的数值，该数值指明了实体正文的长度
         */
        return new ContentLengthInputStream(new BufferedInputStream(imageStream, BUFFER_SIZE), conn.getContentLength());
    }

    /**
     * @param conn Opened request connection (response code is available)
     * @return <b>true</b> - if data from connection is correct and should be read and processed;
     * <b>false</b> - if response contains irrelevant data and shouldn't be processed
     * 判断HTTP响应码是不是200（即请求被成功地完成，所请求的资源发送回客户端）
     * @throws IOException
     */
    protected boolean shouldBeProcessed(HttpURLConnection conn) throws IOException {
        return conn.getResponseCode() == HttpURLConnection.HTTP_OK;
    }

    /**
     * Create {@linkplain HttpURLConnection HTTP connection} for incoming URL
     *
     * @param url   URL to connect to
     * @param extra Auxiliary object which was passed to {@link DisplayImageOptions.Builder#extraForDownloader(Object)
     *              DisplayImageOptions.extraForDownloader(Object)}; can be null
     * @return {@linkplain HttpURLConnection Connection} for incoming URL. Connection isn't established so it still configurable.
     * @throws IOException if some I/O error occurs during network request or if no InputStream could be created for
     *                     URL.
     */
    protected HttpURLConnection createConnection(String url, Object extra) throws IOException {
        String encodedUrl = Uri.encode(url, ALLOWED_URI_CHARS);
        HttpURLConnection conn = (HttpURLConnection) new URL(encodedUrl).openConnection();
        conn.setConnectTimeout(connectTimeout);
        conn.setReadTimeout(readTimeout);
        return conn;
    }

    /**
     * Retrieves {@link InputStream} of image by URI (image is located on the local file system or SD card).
     * 获取本地文件的数据流；如果本地文件是视频文件，那么返回其缩略图的数据流
     *
     * @param imageUri Image URI 文件路径
     * @param extra    Auxiliary object which was passed to {@link DisplayImageOptions.Builder#extraForDownloader(Object)
     *                 DisplayImageOptions.extraForDownloader(Object)}; can be null
     * @return {@link InputStream} of image
     * @throws IOException if some I/O error occurs reading from file system
     */
    protected InputStream getStreamFromFile(String imageUri, Object extra) throws IOException {
        String filePath = Scheme.FILE.crop(imageUri);//去掉file://前缀
        if (isVideoFileUri(imageUri)) {
            return getVideoThumbnailStream(filePath);
        } else {
            BufferedInputStream imageStream = new BufferedInputStream(new FileInputStream(filePath), BUFFER_SIZE);
            return new ContentLengthInputStream(imageStream, (int) new File(filePath).length());
        }
    }

    /**
     * Android系统2.2以上（包括2.2）
     * 返回视频的缩略图的数据流{@link android.media.ThumbnailUtils#createVideoThumbnail}
     * {@link android.media.MediaMetadataRetriever}获取媒体文件信息
     * {@link android.provider.MediaStore.Images.Thumbnails#MICRO_KIND}表示微型缩略模式
     * {@link android.provider.MediaStore.Images.Thumbnails#MINI_KIND}表示迷你型缩略模式
     *
     * @param filePath
     * @return
     */
    @TargetApi(Build.VERSION_CODES.FROYO)
    private InputStream getVideoThumbnailStream(String filePath) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            Bitmap bitmap = ThumbnailUtils
                    .createVideoThumbnail(filePath, MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
            if (bitmap != null) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(CompressFormat.PNG, 0, bos);
                return new ByteArrayInputStream(bos.toByteArray());
            }
        }
        return null;
    }

    /**
     * Retrieves {@link InputStream} of image by URI (image is accessed using {@link ContentResolver}).
     * 通过文件路径的Uri，返回其数据流
     *
     * @param imageUri Image URI
     * @param extra    Auxiliary object which was passed to {@link DisplayImageOptions.Builder#extraForDownloader(Object)
     *                 DisplayImageOptions.extraForDownloader(Object)}; can be null
     * @return {@link InputStream} of image
     * @throws FileNotFoundException if the provided URI could not be opened
     */
    protected InputStream getStreamFromContent(String imageUri, Object extra) throws FileNotFoundException {
        ContentResolver res = context.getContentResolver();

        Uri uri = Uri.parse(imageUri);
        if (isVideoContentUri(uri)) { // video thumbnail
            /**
             * uri.getLastPathSegment即获取该Uri字符串中最后一个'/'后面的字符串
             */
            Long origId = Long.valueOf(uri.getLastPathSegment());
            Bitmap bitmap = MediaStore.Video.Thumbnails
                    .getThumbnail(res, origId, MediaStore.Images.Thumbnails.MINI_KIND, null);
            if (bitmap != null) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(CompressFormat.PNG, 0, bos);
                return new ByteArrayInputStream(bos.toByteArray());
            }
        } else if (imageUri.startsWith(CONTENT_CONTACTS_URI_PREFIX)) { // contacts photo
            return getContactPhotoStream(uri);
        }

        return res.openInputStream(uri);
    }

    /**
     * 获取本地通讯录用户的头像原图的数据流
     *
     * @param uri
     * @return
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    protected InputStream getContactPhotoStream(Uri uri) {
        ContentResolver res = context.getContentResolver();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return ContactsContract.Contacts.openContactPhotoInputStream(res, uri, true);
        } else {
            return ContactsContract.Contacts.openContactPhotoInputStream(res, uri);
        }
    }

    /**
     * Retrieves {@link InputStream} of image by URI (image is located in assets of application).
     * 获取assets目录下的相应文件的数据流
     *
     * @param imageUri Image URI
     * @param extra    Auxiliary object which was passed to {@link DisplayImageOptions.Builder#extraForDownloader(Object)
     *                 DisplayImageOptions.extraForDownloader(Object)}; can be null
     * @return {@link InputStream} of image
     * @throws IOException if some I/O error occurs file reading
     */
    protected InputStream getStreamFromAssets(String imageUri, Object extra) throws IOException {
        String filePath = Scheme.ASSETS.crop(imageUri);
        return context.getAssets().open(filePath);
    }

    /**
     * Retrieves {@link InputStream} of image by URI (image is located in drawable resources of application).
     * 获取drawable下的文件数据流
     *
     * @param imageUri Image URI
     * @param extra    Auxiliary object which was passed to {@link DisplayImageOptions.Builder#extraForDownloader(Object)
     *                 DisplayImageOptions.extraForDownloader(Object)}; can be null
     * @return {@link InputStream} of image
     */
    protected InputStream getStreamFromDrawable(String imageUri, Object extra) {
        String drawableIdString = Scheme.DRAWABLE.crop(imageUri);
        int drawableId = Integer.parseInt(drawableIdString);
        return context.getResources().openRawResource(drawableId);
    }

    /**
     * Retrieves {@link InputStream} of image by URI from other source with unsupported scheme. Should be overriden by
     * successors to implement image downloading from special sources.<br />
     * This method is called only if image URI has unsupported scheme. Throws {@link UnsupportedOperationException} by
     * default.
     * 获取{@link com.nostra13.universalimageloader.core.download.ImageDownloader.Scheme#UNKNOWN}类型的文件数据流
     *
     * @param imageUri Image URI
     * @param extra    Auxiliary object which was passed to {@link DisplayImageOptions.Builder#extraForDownloader(Object)
     *                 DisplayImageOptions.extraForDownloader(Object)}; can be null
     * @return {@link InputStream} of image
     * @throws IOException                   if some I/O error occurs
     * @throws UnsupportedOperationException if image URI has unsupported scheme(protocol)
     */
    protected InputStream getStreamFromOtherSource(String imageUri, Object extra) throws IOException {
        throw new UnsupportedOperationException(String.format(ERROR_UNSUPPORTED_SCHEME, imageUri));
    }

    /**
     * 通过文件路径的Uri来获取其MIME类型是否是视频类型
     *
     * @param uri
     * @return
     */
    private boolean isVideoContentUri(Uri uri) {
        String mimeType = context.getContentResolver().getType(uri);
        return mimeType != null && mimeType.startsWith("video/");
    }

    /**
     * 判断给定的路径文件MIME类型是不是以video开头的{@link android.webkit.MimeTypeMap#getMimeTypeFromExtension}
     *
     * @param uri
     * @return
     */
    private boolean isVideoFileUri(String uri) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(uri);
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        return mimeType != null && mimeType.startsWith("video/");
    }
}
