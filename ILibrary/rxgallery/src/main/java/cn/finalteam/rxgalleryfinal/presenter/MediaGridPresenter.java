package cn.finalteam.rxgalleryfinal.presenter;

import cn.finalteam.rxgalleryfinal.view.MediaGridView;

/**
 * Desction:
 * Author:pengjianbo
 * Date:16/5/14 上午10:53
 *
 * @since 1.0.0
 */
public interface MediaGridPresenter {
    void setMediaGridView(MediaGridView mediaGridView);

    /**
     * @deprecated Use {@link #getImageAndVideoList(String, int, int)} instead
     */
    @Deprecated
    void getImageList(String bucketId, int pageSize, int currentOffset);

    /**
     * @since 1.1.0
     */
    void getImageAndVideoList(String bucketId, int pageSize, int currentOffset);

    /**
     * @deprecated Use {@link #getBucketWithImageAndVideoList()} instead
     */
    @Deprecated
    void getBucketWithImageOrVideoList();

    /**
     * @since 1.1.0
     */
    void getBucketWithImageAndVideoList();
}
