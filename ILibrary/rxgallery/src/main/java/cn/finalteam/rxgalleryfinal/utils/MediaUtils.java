package cn.finalteam.rxgalleryfinal.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.finalteam.rxgalleryfinal.R;
import cn.finalteam.rxgalleryfinal.bean.BucketBean;
import cn.finalteam.rxgalleryfinal.bean.MediaBean;

/**
 * Desction:媒体获取工具
 * Author:pengjianbo
 * Date:16/5/4 下午4:11
 *
 * @since 1.0.0
 */
public class MediaUtils {

    public static List<MediaBean> getMediaWithImageList(Context context, int page, int limit) {
        return getMediaWithImageList(context, String.valueOf(Integer.MIN_VALUE), page, limit);
    }

    /**
     * 从数据库中读取图片
     */
    public static List<MediaBean> getMediaWithImageList(Context context, String bucketId, int page, int limit) {
        int offset = (page - 1) * limit;
        List<MediaBean> mediaBeanList = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        List<String> projection = new ArrayList<>();
        projection.add(MediaStore.Images.Media._ID);
        projection.add(MediaStore.Images.Media.TITLE);
        projection.add(MediaStore.Images.Media.DATA);
        projection.add(MediaStore.Images.Media.BUCKET_ID);
        projection.add(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        projection.add(MediaStore.Images.Media.MIME_TYPE);
        projection.add(MediaStore.Images.Media.DATE_ADDED);
        projection.add(MediaStore.Images.Media.DATE_MODIFIED);
        projection.add(MediaStore.Images.Media.LATITUDE);
        projection.add(MediaStore.Images.Media.LONGITUDE);
        projection.add(MediaStore.Images.Media.ORIENTATION);
        projection.add(MediaStore.Images.Media.SIZE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            projection.add(MediaStore.Images.Media.WIDTH);
            projection.add(MediaStore.Images.Media.HEIGHT);
        }
        String selection = null;
        String[] selectionArgs = null;
        /**
         * 如果bucketId为Integer.MIN_VALUE，那么表示显示所有的附件
         */
        if (!TextUtils.equals(bucketId, String.valueOf(Integer.MIN_VALUE))) {
            selection = MediaStore.Images.Media.BUCKET_ID + "=?";
            selectionArgs = new String[]{bucketId};
        }
        Cursor cursor = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection.toArray(new String[projection.size()]), selection,
                selectionArgs, MediaStore.Images.Media.DATE_ADDED + " DESC LIMIT " + limit + " OFFSET " + offset);
        if (cursor != null) {
            int count = cursor.getCount();
            if (count > 0) {
                cursor.moveToFirst();
                do {
                    MediaBean mediaBean = parseImageCursorAndCreateThumImage(context, cursor);
                    mediaBeanList.add(mediaBean);
                } while (cursor.moveToNext());
            }
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return mediaBeanList;
    }

    public static List<MediaBean> getMediaWithVideoList(Context context, int page, int limit) {
        return getMediaWithVideoList(context, String.valueOf(Integer.MIN_VALUE), page, limit);
    }

    /**
     * 从数据库中读取视频
     */
    public static List<MediaBean> getMediaWithVideoList(Context context, String bucketId, int page, int limit) {
        int offset = (page - 1) * limit;
        List<MediaBean> mediaBeanList = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        List<String> projection = new ArrayList<>();
        projection.add(MediaStore.Video.Media._ID);
        projection.add(MediaStore.Video.Media.TITLE);
        projection.add(MediaStore.Video.Media.DATA);
        projection.add(MediaStore.Video.Media.BUCKET_ID);
        projection.add(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
        projection.add(MediaStore.Video.Media.MIME_TYPE);
        projection.add(MediaStore.Files.FileColumns.MEDIA_TYPE);
        projection.add(MediaStore.Video.Media.DATE_ADDED);
        projection.add(MediaStore.Video.Media.DATE_MODIFIED);
        projection.add(MediaStore.Video.Media.LATITUDE);
        projection.add(MediaStore.Video.Media.LONGITUDE);
        projection.add(MediaStore.Video.Media.DURATION);
        projection.add(MediaStore.Video.Media.SIZE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            projection.add(MediaStore.Video.Media.WIDTH);
            projection.add(MediaStore.Video.Media.HEIGHT);
        }
        String selection = null;
        String[] selectionArgs = null;
        if (!TextUtils.equals(bucketId, String.valueOf(Integer.MIN_VALUE))) {
            selection = MediaStore.Video.Media.BUCKET_ID + "=?";
            selectionArgs = new String[]{bucketId};
        }

        Cursor cursor = contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection.toArray(new String[projection.size()]), selection,
                selectionArgs, MediaStore.Video.Media.DATE_ADDED + " DESC LIMIT " + limit + " OFFSET " + offset);
        if (cursor != null) {
            int count = cursor.getCount();
            if (count > 0) {
                cursor.moveToFirst();
                do {
                    MediaBean mediaBean = parseVideoCursorAndCreateThumImage(context, cursor);
                    mediaBeanList.add(mediaBean);
                } while (cursor.moveToNext());
            }
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return mediaBeanList;
    }

    /**
     * 根据原图获取图片相关信息
     */
    public static MediaBean getMediaBeanWithImage(Context context, String originalPath) {
        ContentResolver contentResolver = context.getContentResolver();
        List<String> projection = new ArrayList<>();
        projection.add(MediaStore.Images.Media._ID);
        projection.add(MediaStore.Images.Media.TITLE);
        projection.add(MediaStore.Images.Media.DATA);
        projection.add(MediaStore.Images.Media.BUCKET_ID);
        projection.add(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        projection.add(MediaStore.Images.Media.MIME_TYPE);
        projection.add(MediaStore.Files.FileColumns.MEDIA_TYPE);
        projection.add(MediaStore.Images.Media.DATE_ADDED);
        projection.add(MediaStore.Images.Media.DATE_MODIFIED);
        projection.add(MediaStore.Images.Media.LATITUDE);
        projection.add(MediaStore.Images.Media.LONGITUDE);
        projection.add(MediaStore.Images.Media.ORIENTATION);
        projection.add(MediaStore.Images.Media.SIZE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            projection.add(MediaStore.Images.Media.WIDTH);
            projection.add(MediaStore.Images.Media.HEIGHT);
        }
        Cursor cursor = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection.toArray(new String[projection.size()]), MediaStore.Images.Media.DATA + "=?",
                new String[]{originalPath}, null);
        MediaBean mediaBean = null;
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            mediaBean = parseImageCursorAndCreateThumImage(context, cursor);
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return mediaBean;
    }

    /**
     * 解析图片cursor并且创建缩略图
     */
    private static MediaBean parseImageCursorAndCreateThumImage(Context context, Cursor cursor) {
        MediaBean mediaBean = new MediaBean();
        long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID));
        mediaBean.setId(id);
        String title = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.TITLE));
        mediaBean.setTitle(title);
        String originalPath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        mediaBean.setOriginalPath(originalPath);
        String bucketId = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID));
        mediaBean.setBucketId(bucketId);
        String bucketDisplayName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
        mediaBean.setBucketDisplayName(bucketDisplayName);
        String mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE));
        mediaBean.setMimeType(mimeType);
        int mediaType = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE));
        mediaBean.setMediaType(mediaType);
        long createDate = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
        mediaBean.setCreateDate(createDate);
        long modifiedDate = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED));
        mediaBean.setModifiedDate(modifiedDate);
        mediaBean.setThumbnailBigPath(createThumbnailBigFileName(context, originalPath).getAbsolutePath());
        mediaBean.setThumbnailSmallPath(createThumbnailSmallFileName(context, originalPath).getAbsolutePath());
        int width = 0, height = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            width = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.WIDTH));
            height = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT));
        } else {
            try {
                ExifInterface exifInterface = new ExifInterface(originalPath);
                width = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0);
                height = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0);
            } catch (IOException e) {
                Logger.e(e);
            }
        }
        mediaBean.setWidth(width);
        mediaBean.setHeight(height);
        double latitude = cursor.getDouble(cursor.getColumnIndex(MediaStore.Images.Media.LATITUDE));
        mediaBean.setLatitude(latitude);
        double longitude = cursor.getDouble(cursor.getColumnIndex(MediaStore.Images.Media.LONGITUDE));
        mediaBean.setLongitude(longitude);
        int orientation = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION));
        mediaBean.setOrientation(orientation);
        long length = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.SIZE));
        mediaBean.setLength(length);

        return mediaBean;
    }

    /**
     * 解析视频cursor并且创建缩略图
     */
    private static MediaBean parseVideoCursorAndCreateThumImage(Context context, Cursor cursor) {
        MediaBean mediaBean = new MediaBean();
        long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media._ID));
        mediaBean.setId(id);
        String title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
        mediaBean.setTitle(title);
        String originalPath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
        mediaBean.setOriginalPath(originalPath);
        String bucketId = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID));
        mediaBean.setBucketId(bucketId);
        String bucketDisplayName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME));
        mediaBean.setBucketDisplayName(bucketDisplayName);
        String mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE));
        mediaBean.setMimeType(mimeType);
        int mediaType = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE));
        mediaBean.setMediaType(mediaType);
        long createDate = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED));
        mediaBean.setCreateDate(createDate);
        long modifiedDate = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED));
        mediaBean.setModifiedDate(modifiedDate);
        long length = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
        mediaBean.setLength(length);
        long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
        mediaBean.setDuration(duration);

        //创建缩略图文件
        mediaBean.setThumbnailBigPath(createThumbnailBigFileName(context, originalPath).getAbsolutePath());
        mediaBean.setThumbnailSmallPath(createThumbnailSmallFileName(context, originalPath).getAbsolutePath());

        int width = 0, height = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            width = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.WIDTH));
            height = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT));
        } else {
            try {
                ExifInterface exifInterface = new ExifInterface(originalPath);
                width = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0);
                height = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0);
            } catch (IOException e) {
                Logger.e(e);
            }
        }
        mediaBean.setWidth(width);
        mediaBean.setHeight(height);

        double latitude = cursor.getDouble(cursor.getColumnIndex(MediaStore.Video.Media.LATITUDE));
        mediaBean.setLatitude(latitude);
        double longitude = cursor.getDouble(cursor.getColumnIndex(MediaStore.Video.Media.LONGITUDE));
        mediaBean.setLongitude(longitude);
        return mediaBean;
    }

    public static File createThumbnailBigFileName(Context context, String originalPath) {
        File storeFile = StorageUtils.getCacheDirectory(context);
        return new File(storeFile, "big_" + FilenameUtils.getName(originalPath));
    }

    public static File createThumbnailSmallFileName(Context context, String originalPath) {
        File storeFile = StorageUtils.getCacheDirectory(context);
        return new File(storeFile, "small_" + FilenameUtils.getName(originalPath));
    }

    /**
     * 获取所有的图片文件夹
     */
    public static List<BucketBean> getAllBucketByImage(Context context) {
        return getAllBucketWithImageOrVideo(context, true);
    }

    /**
     * 获取所有视频文件夹
     */
    public static List<BucketBean> getAllBucketByVideo(Context context) {
        return getAllBucketWithImageOrVideo(context, false);
    }

    /**
     * 获取所有的问media文件夹
     */
    public static List<BucketBean> getAllBucketWithImageOrVideo(Context context, boolean isImage) {
        List<BucketBean> bucketBeenList = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        String[] projection;
        if (isImage) {
            projection = new String[]{
                    MediaStore.Images.Media.BUCKET_ID,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.ORIENTATION,
            };
        } else {
            projection = new String[]{
                    MediaStore.Video.Media.BUCKET_ID,
                    MediaStore.Video.Media.DATA,
                    MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            };
        }
        BucketBean allMediaBucket = new BucketBean();
        allMediaBucket.setBucketId(String.valueOf(Integer.MIN_VALUE));
        Uri uri;
        if (isImage) {
            allMediaBucket.setBucketName(context.getString(R.string.gallery_all_image));
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else {
            allMediaBucket.setBucketName(context.getString(R.string.gallery_all_video));
            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }
        bucketBeenList.add(allMediaBucket);
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(uri, projection, null, null, MediaStore.Video.Media.DATE_ADDED + " DESC");
        } catch (Exception e) {
            Logger.e(e);
        }

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                BucketBean bucketBean = new BucketBean();
                String bucketId;
                String bucketKey;
                String cover;
                if (isImage) {
                    bucketId = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID));
                    bucketBean.setBucketId(bucketId);
                    String bucketDisplayName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    bucketBean.setBucketName(bucketDisplayName);
                    bucketKey = MediaStore.Images.Media.BUCKET_ID;
                    cover = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    int orientation = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION));
                    bucketBean.setOrientation(orientation);
                } else {
                    bucketId = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID));
                    bucketBean.setBucketId(bucketId);
                    String bucketDisplayName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME));
                    bucketBean.setBucketName(bucketDisplayName);
                    bucketKey = MediaStore.Video.Media.BUCKET_ID;
                    cover = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                }
                if (TextUtils.isEmpty(allMediaBucket.getCover())) {
                    allMediaBucket.setCover(cover);
                }
                if (bucketBeenList.contains(bucketBean)) {
                    continue;
                }
                //获取数量
                Cursor c = contentResolver.query(uri, projection, bucketKey + "=?", new String[]{bucketId}, null);
                if (c != null && c.getCount() > 0) {
                    bucketBean.setImageCount(c.getCount());
                }
                bucketBean.setCover(cover);
                if (c != null && !c.isClosed()) {
                    c.close();
                }
                bucketBeenList.add(bucketBean);
            } while (cursor.moveToNext());
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return bucketBeenList;
    }

    /**
     * 返回所有外部存储的视频和图片文件的信息集合
     *
     * @return Map<String, List<MediaBean>>  KEY由媒体文件所在的文件夹的BUCKET_ID构成，VALUE用于存储该文件夹内所有媒体文件的文件信息
     * @since 1.1.0
     */
    public static Map<String, List<MediaBean>> getImageAndVideoList(Context context) {
        List<String> projection = new ArrayList<>();
        projection.add(MediaStore.Files.FileColumns._ID);
        projection.add(MediaStore.Files.FileColumns.TITLE);
        projection.add(MediaStore.Files.FileColumns.DATA);
        projection.add(MediaStore.Files.FileColumns.MIME_TYPE);
        projection.add(MediaStore.Files.FileColumns.MEDIA_TYPE);
        projection.add(MediaStore.Files.FileColumns.DATE_ADDED);
        projection.add(MediaStore.Files.FileColumns.DATE_MODIFIED);
        projection.add(MediaStore.Files.FileColumns.SIZE);
        projection.add(MediaStore.Images.Media.BUCKET_ID);
        projection.add(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        projection.add(MediaStore.Images.Media.LATITUDE);
        projection.add(MediaStore.Images.Media.LONGITUDE);
        projection.add(MediaStore.Images.Media.ORIENTATION);
        projection.add(MediaStore.Video.Media.BUCKET_ID);
        projection.add(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
        projection.add(MediaStore.Video.Media.LATITUDE);
        projection.add(MediaStore.Video.Media.LONGITUDE);
        projection.add(MediaStore.Video.Media.DURATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            projection.add(MediaStore.Files.FileColumns.WIDTH);
            projection.add(MediaStore.Files.FileColumns.HEIGHT);
        }
        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
        Cursor cursor = context.getContentResolver().query(MediaStore.Files.getContentUri("external")
                , projection.toArray(new String[projection.size()]), selection, null, null);
        if (cursor == null) return null;
        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        }

        /**
         * 当键值都为Integer是才用SparseIntArray
         */
        Map<String, List<MediaBean>> mediaMap = new ArrayMap<>();
        cursor.moveToFirst();
        do {
            int orientation = -1;
            long duration = 0L;
            double latitude = 0d;
            double longitude = 0d;
            String bucketId = null;
            String bucketDisplayName = null;
            int mediaType = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE));
            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID));
            long length = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.SIZE));
            long createDate = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
            long modifiedDate = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED));
            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.TITLE));
            String mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE));
            String originalPath = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
            switch (mediaType) {
                case MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE:
                    latitude = cursor.getDouble(cursor.getColumnIndex(MediaStore.Images.Media.LATITUDE));
                    longitude = cursor.getDouble(cursor.getColumnIndex(MediaStore.Images.Media.LONGITUDE));
                    orientation = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION));
                    bucketId = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID));
                    bucketDisplayName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    break;
                case MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO:
                    duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
                    latitude = cursor.getDouble(cursor.getColumnIndex(MediaStore.Video.Media.LATITUDE));
                    longitude = cursor.getDouble(cursor.getColumnIndex(MediaStore.Video.Media.LONGITUDE));
                    bucketId = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID));
                    bucketDisplayName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME));
                    break;
            }

            MediaBean mediaBean = new MediaBean();
            mediaBean.setId(id);
            mediaBean.setTitle(title);
            mediaBean.setMimeType(mimeType);
            mediaBean.setMediaType(mediaType);
            mediaBean.setBucketId(bucketId);
            mediaBean.setDuration(duration);
            mediaBean.setCreateDate(createDate);
            mediaBean.setModifiedDate(modifiedDate);
            mediaBean.setOriginalPath(originalPath);
            mediaBean.setBucketDisplayName(bucketDisplayName);
            mediaBean.setThumbnailBigPath(MediaUtils.createThumbnailBigFileName(context, originalPath).getAbsolutePath());
            mediaBean.setThumbnailSmallPath(MediaUtils.createThumbnailSmallFileName(context, originalPath).getAbsolutePath());
            int width = 0, height = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                width = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns.WIDTH));
                height = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns.HEIGHT));
            } else {
                try {
                    ExifInterface exifInterface = new ExifInterface(originalPath);
                    width = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0);
                    height = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0);
                } catch (IOException e) {
                    Logger.e(e);
                }
            }
            mediaBean.setWidth(width);
            mediaBean.setHeight(height);
            mediaBean.setLatitude(latitude);
            mediaBean.setLongitude(longitude);
            mediaBean.setOrientation(orientation);
            mediaBean.setLength(length);

            if (mediaMap.containsKey(bucketId)) {
                List<MediaBean> mediaList = mediaMap.get(bucketId);
                mediaList.add(mediaBean);
            } else {
                List<MediaBean> mediaList = new ArrayList<>();
                mediaList.add(mediaBean);
                mediaMap.put(bucketId, mediaList);
            }
        } while (cursor.moveToNext());
        cursor.close();
        return mediaMap;
    }

    /**
     * 根据BUCKET_ID（表示对应的媒体文件所在的文件目录的ID）来查询该目录下所有的图片和视频文件
     *
     * @param limit 限制查询多少个媒体文件（查询到的文件按照添加时间降序排列）
     * @since 1.1.0
     */
    public static List<MediaBean> getImageAndVideoList(Context context, String bucketId, int page, int limit) {
        int offset = (page - 1) * limit;
        List<MediaBean> mediaBeanList = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        List<String> projection = new ArrayList<>();
        projection.add(MediaStore.Files.FileColumns._ID);
        projection.add(MediaStore.Files.FileColumns.TITLE);
        projection.add(MediaStore.Files.FileColumns.DATA);
        projection.add(MediaStore.Files.FileColumns.MIME_TYPE);
        projection.add(MediaStore.Files.FileColumns.MEDIA_TYPE);
        projection.add(MediaStore.Files.FileColumns.DATE_ADDED);
        projection.add(MediaStore.Files.FileColumns.DATE_MODIFIED);
        projection.add(MediaStore.Files.FileColumns.SIZE);
        projection.add(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        projection.add(MediaStore.Images.Media.LATITUDE);
        projection.add(MediaStore.Images.Media.LONGITUDE);
        projection.add(MediaStore.Images.Media.ORIENTATION);
        projection.add(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
        projection.add(MediaStore.Video.Media.LATITUDE);
        projection.add(MediaStore.Video.Media.LONGITUDE);
        projection.add(MediaStore.Video.Media.DURATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            projection.add(MediaStore.Files.FileColumns.WIDTH);
            projection.add(MediaStore.Files.FileColumns.HEIGHT);
        }
        StringBuilder selection = new StringBuilder();
        String[] selectionArgs = null;
        selection.append(MediaStore.Files.FileColumns.MEDIA_TYPE + " IN("
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + ","
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO + ")");
        /**
         * 如果bucketId为Integer.MIN_VALUE，那么表示显示所有的附件
         */
        if (!TextUtils.equals(bucketId, String.valueOf(Integer.MIN_VALUE))) {
            selection.append(" AND " + MediaStore.Images.Media.BUCKET_ID + "=?");
            selectionArgs = new String[]{bucketId};
        }
        Cursor cursor = contentResolver.query(MediaStore.Files.getContentUri("external")
                , projection.toArray(new String[projection.size()]), selection.toString(), selectionArgs
                , MediaStore.Images.Media.DATE_ADDED + " DESC LIMIT " + limit + " OFFSET " + offset);
        if (cursor == null) return mediaBeanList;
        if (cursor.getCount() == 0) {
            cursor.close();
            return mediaBeanList;
        }

        cursor.moveToFirst();
        do {
            int orientation = -1;
            long duration = 0L;
            double latitude = 0d;
            double longitude = 0d;
            String bucketDisplayName = null;
            int mediaType = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE));
            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID));
            long length = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.SIZE));
            long createDate = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
            long modifiedDate = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED));
            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.TITLE));
            String mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE));
            String originalPath = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
            switch (mediaType) {
                case MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE:
                    latitude = cursor.getDouble(cursor.getColumnIndex(MediaStore.Images.Media.LATITUDE));
                    longitude = cursor.getDouble(cursor.getColumnIndex(MediaStore.Images.Media.LONGITUDE));
                    orientation = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION));
                    bucketDisplayName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    break;
                case MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO:
                    duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
                    latitude = cursor.getDouble(cursor.getColumnIndex(MediaStore.Video.Media.LATITUDE));
                    longitude = cursor.getDouble(cursor.getColumnIndex(MediaStore.Video.Media.LONGITUDE));
                    bucketDisplayName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME));
                    break;
            }

            MediaBean mediaBean = new MediaBean();
            mediaBean.setId(id);
            mediaBean.setTitle(title);
            mediaBean.setDuration(duration);
            mediaBean.setMimeType(mimeType);
            mediaBean.setMediaType(mediaType);
            mediaBean.setBucketId(bucketId);
            mediaBean.setCreateDate(createDate);
            mediaBean.setModifiedDate(modifiedDate);
            mediaBean.setOriginalPath(originalPath);
            mediaBean.setBucketDisplayName(bucketDisplayName);
            mediaBean.setThumbnailBigPath(MediaUtils.createThumbnailBigFileName(context, originalPath).getAbsolutePath());
            mediaBean.setThumbnailSmallPath(MediaUtils.createThumbnailSmallFileName(context, originalPath).getAbsolutePath());
            int width = 0, height = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                width = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns.WIDTH));
                height = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns.HEIGHT));
            } else {
                try {
                    ExifInterface exifInterface = new ExifInterface(originalPath);
                    width = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0);
                    height = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0);
                } catch (IOException e) {
                    Logger.e(e);
                }
            }
            mediaBean.setWidth(width);
            mediaBean.setHeight(height);
            mediaBean.setLatitude(latitude);
            mediaBean.setLongitude(longitude);
            mediaBean.setOrientation(orientation);
            mediaBean.setLength(length);
            mediaBeanList.add(mediaBean);
        } while (cursor.moveToNext());
        cursor.close();
        return mediaBeanList;
    }

    /**
     * @since 1.1.0
     */
    public static List<BucketBean> getAllBucketWithImageAndVideo(Context context) {
        List<BucketBean> bucketBeenList = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        String[] projection;
        projection = new String[]{
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.ORIENTATION,
                MediaStore.Video.Media.BUCKET_ID,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
        };
        BucketBean allMediaBucket = new BucketBean();
        Uri uri = MediaStore.Files.getContentUri("external");
        allMediaBucket.setBucketId(String.valueOf(Integer.MIN_VALUE));
        allMediaBucket.setBucketName(context.getString(R.string.gallery_all_image_and_video));
        bucketBeenList.add(allMediaBucket);
        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
        Cursor cursor = contentResolver.query(uri, projection, selection, null, MediaStore.Video.Media.DATE_ADDED + " DESC");
        if (cursor == null) return bucketBeenList;
        if (cursor.getCount() == 0) {
            cursor.close();
            return bucketBeenList;
        }

        cursor.moveToFirst();
        do {
            BucketBean bucketBean = new BucketBean();
            String bucketId = null;
            String bucketKey = null;
            String bucketDisplayName;
            String cover = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
            int mediaType = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE));
            switch (mediaType) {
                case MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE:
                    bucketId = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID));
                    bucketBean.setBucketId(bucketId);
                    bucketDisplayName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    bucketBean.setBucketName(bucketDisplayName);
                    bucketKey = MediaStore.Images.Media.BUCKET_ID;
                    int orientation = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION));
                    bucketBean.setOrientation(orientation);
                    break;
                case MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO:
                    bucketId = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID));
                    bucketBean.setBucketId(bucketId);
                    bucketDisplayName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME));
                    bucketBean.setBucketName(bucketDisplayName);
                    bucketKey = MediaStore.Video.Media.BUCKET_ID;
                    break;
            }
            if (TextUtils.isEmpty(allMediaBucket.getCover())) {
                allMediaBucket.setCover(cover);
            }
            if (bucketBeenList.contains(bucketBean)) {
                continue;
            }
            String mSelection = MediaStore.Files.FileColumns.MEDIA_TYPE + " IN("
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + ","
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO + ") AND "
                    + bucketKey + "=?";
            //获取数量
            Cursor c = contentResolver.query(uri, projection, mSelection, new String[]{bucketId}, null);
            if (c != null && c.getCount() > 0) {
                bucketBean.setImageCount(c.getCount());
            }
            bucketBean.setCover(cover);
            if (c != null && !c.isClosed()) {
                c.close();
            }
            bucketBeenList.add(bucketBean);
        } while (cursor.moveToNext());
        cursor.close();
        return bucketBeenList;
    }
}
