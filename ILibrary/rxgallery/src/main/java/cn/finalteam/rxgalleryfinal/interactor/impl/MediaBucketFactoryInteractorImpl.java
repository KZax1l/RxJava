package cn.finalteam.rxgalleryfinal.interactor.impl;

import android.content.Context;

import java.util.List;

import cn.finalteam.rxgalleryfinal.bean.BucketBean;
import cn.finalteam.rxgalleryfinal.interactor.MediaBucketFactoryInteractor;
import cn.finalteam.rxgalleryfinal.utils.MediaUtils;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Desction:
 * Author:pengjianbo
 * Date:16/7/4 下午8:29
 *
 * @since 1.0.0
 */
public class MediaBucketFactoryInteractorImpl implements MediaBucketFactoryInteractor {

    private Context context;
    private boolean isImage;
    private OnGenerateBucketListener onGenerateBucketListener;

    /**
     * @since 1.1.0
     */
    public MediaBucketFactoryInteractorImpl(Context context, OnGenerateBucketListener onGenerateBucketListener) {
        this.context = context;
        this.onGenerateBucketListener = onGenerateBucketListener;
    }

    /**
     * @deprecated Use {@link #MediaBucketFactoryInteractorImpl(Context, OnGenerateBucketListener)} instead
     */
    @Deprecated
    public MediaBucketFactoryInteractorImpl(Context context, boolean isImage, OnGenerateBucketListener onGenerateBucketListener) {
        this.context = context;
        this.isImage = isImage;
        this.onGenerateBucketListener = onGenerateBucketListener;
    }

    /**
     * @deprecated Use {@link #generateBucketsWithImageAndVideo()} instead
     */
    @Deprecated
    @Override
    public void generateBucketsWithImageOrVideo() {
        Observable.OnSubscribe<List<BucketBean>> onSubscribe = new Observable.OnSubscribe<List<BucketBean>>() {
            @Override
            public void call(Subscriber<? super List<BucketBean>> subscriber) {
                List<BucketBean> bucketBeanList;
                if (isImage) {
                    bucketBeanList = MediaUtils.getAllBucketByImage(context);
                } else {
                    bucketBeanList = MediaUtils.getAllBucketByVideo(context);
                }
                subscriber.onNext(bucketBeanList);
                subscriber.onCompleted();
            }
        };
        Observer<List<BucketBean>> observer = new Observer<List<BucketBean>>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                onGenerateBucketListener.onFinished(null);
            }

            @Override
            public void onNext(List<BucketBean> bucketBeanList) {
                onGenerateBucketListener.onFinished(bucketBeanList);
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
    public void generateBucketsWithImageAndVideo() {
        Observable.OnSubscribe<List<BucketBean>> onSubscribe = new Observable.OnSubscribe<List<BucketBean>>() {
            @Override
            public void call(Subscriber<? super List<BucketBean>> subscriber) {
                List<BucketBean> bucketBeanList;
                bucketBeanList = MediaUtils.getAllBucketWithImageAndVideo(context);
                subscriber.onNext(bucketBeanList);
                subscriber.onCompleted();
            }
        };
        Observer<List<BucketBean>> observer = new Observer<List<BucketBean>>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                onGenerateBucketListener.onFinished(null);
            }

            @Override
            public void onNext(List<BucketBean> bucketBeanList) {
                onGenerateBucketListener.onFinished(bucketBeanList);
            }
        };
        Observable.create(onSubscribe)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
}
