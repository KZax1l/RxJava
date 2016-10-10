package cn.finalteam.rxgalleryfinal.interactor.impl;

import android.content.Context;

import java.util.List;

import cn.finalteam.rxgalleryfinal.bean.MediaBean;
import cn.finalteam.rxgalleryfinal.interactor.MediaSrcFactoryInteractor;
import cn.finalteam.rxgalleryfinal.utils.MediaUtils;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Desction:
 * Author:pengjianbo
 * Date:16/5/14 上午11:08
 *
 * @since 1.0.0
 */
public class MediaSrcFactoryInteractorImpl implements MediaSrcFactoryInteractor {

    Context context;
    OnGenerateMediaListener onGenerateMediaListener;
    boolean isImage;

    /**
     * @since 1.1.0
     */
    public MediaSrcFactoryInteractorImpl(Context context, OnGenerateMediaListener onGenerateMediaListener) {
        this.context = context;
        this.onGenerateMediaListener = onGenerateMediaListener;
    }

    /**
     * @param isImage true表示是图片类型文件；false表示是视频类型文件
     * @deprecated Use {@link #MediaSrcFactoryInteractorImpl(Context, OnGenerateMediaListener)} instead
     */
    @Deprecated
    public MediaSrcFactoryInteractorImpl(Context context, boolean isImage, OnGenerateMediaListener onGenerateMediaListener) {
        this.context = context;
        this.isImage = isImage;
        this.onGenerateMediaListener = onGenerateMediaListener;
    }

    /**
     * @deprecated Use {@link #generateImagesAndVideos(String, int, int)} instead
     */
    @Deprecated
    @Override
    public void generateImages(final String bucketId, final int page, final int limit) {
        Observable.OnSubscribe<List<MediaBean>> onSubscribe = new Observable.OnSubscribe<List<MediaBean>>() {
            @Override
            public void call(Subscriber<? super List<MediaBean>> subscriber) {
                List<MediaBean> mediaBeanList;
                if (isImage) {
                    mediaBeanList = MediaUtils.getMediaWithImageList(context, bucketId, page, limit);
                } else {
                    mediaBeanList = MediaUtils.getMediaWithVideoList(context, bucketId, page, limit);
                }
                subscriber.onNext(mediaBeanList);
                subscriber.onCompleted();
            }
        };
        Observer<List<MediaBean>> observer = new Observer<List<MediaBean>>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                onGenerateMediaListener.onFinished(bucketId, page, limit, null);
            }

            @Override
            public void onNext(List<MediaBean> mediaBeenList) {
                onGenerateMediaListener.onFinished(bucketId, page, limit, mediaBeenList);
            }
        };
        Observable.create(onSubscribe)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    /**
     * @since 1.1.0
     */
    @Override
    public void generateImagesAndVideos(final String bucketId, final int page, final int limit) {
        Observable.OnSubscribe<List<MediaBean>> onSubscribe = new Observable.OnSubscribe<List<MediaBean>>() {
            @Override
            public void call(Subscriber<? super List<MediaBean>> subscriber) {
                List<MediaBean> mediaBeanList;
                mediaBeanList = MediaUtils.getImageAndVideoList(context, bucketId, page, limit);
                subscriber.onNext(mediaBeanList);
                subscriber.onCompleted();
            }
        };
        Observer<List<MediaBean>> observer = new Observer<List<MediaBean>>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                onGenerateMediaListener.onFinished(bucketId, page, limit, null);
            }

            @Override
            public void onNext(List<MediaBean> mediaBeenList) {
                onGenerateMediaListener.onFinished(bucketId, page, limit, mediaBeenList);
            }
        };
        Observable.create(onSubscribe)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
}
