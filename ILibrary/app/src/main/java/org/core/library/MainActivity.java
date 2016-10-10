package org.core.library;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.finalteam.rxgalleryfinal.bean.BucketBean;
import cn.finalteam.rxgalleryfinal.bean.MediaBean;
import cn.finalteam.rxgalleryfinal.utils.Logger;
import cn.finalteam.rxgalleryfinal.utils.MediaUtils;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tvMsg = (TextView) findViewById(R.id.tv_message);
        List<BucketBean> imgList = MediaUtils.getAllBucketWithImageOrVideo(this, true);
        List<BucketBean> videoList = MediaUtils.getAllBucketWithImageOrVideo(this, false);
        StringBuilder sb = new StringBuilder();
        sb.append("----------images----------\n");
        for (BucketBean bean : imgList) {
            sb.append(bean.getBucketId() + "," + bean.getBucketName() + "," + bean.getCover() + "," + bean.getImageCount() + "," + bean.getOrientation() + "\n");
        }
        sb.append("----------videos----------\n");
        for (BucketBean bean : videoList) {
            sb.append(bean.getBucketId() + "," + bean.getBucketName() + "," + bean.getCover() + "," + bean.getImageCount() + "," + bean.getOrientation() + "\n");
        }
        tvMsg.setText(sb.toString());
        getImageAndVideoList(this);
        List<MediaBean> mediaBeanList = MediaUtils.getImageAndVideoList(this, String.valueOf(Integer.MIN_VALUE), 1, 5);
        for (MediaBean mediaBean : mediaBeanList) {
            System.out.println("----------->" + mediaBean.getOriginalPath());
        }
        List<BucketBean> bucketBeanList = MediaUtils.getAllBucketWithImageAndVideo(this);
        for (BucketBean bucketBean : bucketBeanList) {
            System.out.println("************" + bucketBean.getBucketName() + "," + bucketBean.getImageCount());
        }
    }

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            projection.add(MediaStore.Files.FileColumns.WIDTH);
            projection.add(MediaStore.Files.FileColumns.HEIGHT);
        }
        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + " IN("
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + ","
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO + ")";
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

            if (mediaMap.containsKey(bucketId)) {
                List<MediaBean> mediaList = mediaMap.get(bucketId);
                mediaList.add(mediaBean);
            } else {
                List<MediaBean> mediaList = new ArrayList<>();
                mediaList.add(mediaBean);
                mediaMap.put(bucketId, mediaList);
            }
            System.out.println("path=" + originalPath + ",bucketId=" + bucketId);
        } while (cursor.moveToNext());
        cursor.close();
        return mediaMap;
    }

    public static List<BucketBean> getAllBucket(Context context, boolean isImage) {
        List<BucketBean> bucketBeenList = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        String[] projection = new String[]{
                MediaStore.Files.FileColumns.MEDIA_TYPE
        };
        BucketBean allMediaBucket = new BucketBean();
        allMediaBucket.setBucketId(String.valueOf(Integer.MIN_VALUE));
        Uri uri;

        if (isImage) {
            allMediaBucket.setBucketName(context.getString(cn.finalteam.rxgalleryfinal.R.string.gallery_all_image));
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else {
            allMediaBucket.setBucketName(context.getString(cn.finalteam.rxgalleryfinal.R.string.gallery_all_video));
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
}
