package cn.finalteam.rxgalleryfinal.ui.activity;

import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.finalteam.rxgalleryfinal.Configuration;
import cn.finalteam.rxgalleryfinal.R;
import cn.finalteam.rxgalleryfinal.bean.MediaBean;
import cn.finalteam.rxgalleryfinal.rxbus.RxBus;
import cn.finalteam.rxgalleryfinal.rxbus.RxBusSubscriber;
import cn.finalteam.rxgalleryfinal.rxbus.event.BaseResultEvent;
import cn.finalteam.rxgalleryfinal.rxbus.event.CloseRxMediaGridPageEvent;
import cn.finalteam.rxgalleryfinal.rxbus.event.ImageMultipleResultEvent;
import cn.finalteam.rxgalleryfinal.rxbus.event.MediaCheckChangeEvent;
import cn.finalteam.rxgalleryfinal.rxbus.event.MediaViewPagerChangedEvent;
import cn.finalteam.rxgalleryfinal.rxbus.event.OpenMediaPageFragmentEvent;
import cn.finalteam.rxgalleryfinal.rxbus.event.OpenMediaPreviewFragmentEvent;
import cn.finalteam.rxgalleryfinal.rxbus.event.RequestStorageReadAccessPermissionEvent;
import cn.finalteam.rxgalleryfinal.rxjob.RxJob;
import cn.finalteam.rxgalleryfinal.ui.fragment.MediaGridFragment;
import cn.finalteam.rxgalleryfinal.ui.fragment.MediaPageFragment;
import cn.finalteam.rxgalleryfinal.ui.fragment.MediaPreviewFragment;
import cn.finalteam.rxgalleryfinal.utils.Logger;
import cn.finalteam.rxgalleryfinal.utils.OsCompat;
import cn.finalteam.rxgalleryfinal.utils.ThemeUtils;
import cn.finalteam.rxgalleryfinal.view.ActivityFragmentView;
import rx.Subscription;

/**
 * Desction:
 * Author:pengjianbo
 * Date:16/5/7 上午10:01
 *
 * @since 1.0.0
 */
public class MediaActivity extends BaseActivity implements ActivityFragmentView {
    public static final int REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101;

    private static final String EXTRA_CHECKED_LIST = EXTRA_PREFIX + ".CheckedList";
    private static final String EXTRA_SELECTED_INDEX = EXTRA_PREFIX + ".SelectedIndex";
    private static final String EXTRA_PAGE_MEDIA_LIST = EXTRA_PREFIX + ".PageMediaList";
    private static final String EXTRA_PAGE_POSITION = EXTRA_PREFIX + ".PagePosition";
    private static final String EXTRA_PREVIEW_POSITION = EXTRA_PREFIX + ".PreviewPosition";

    /**
     * 以GridView的方式展示图像的页面以供选择
     */
    MediaGridFragment mMediaGridFragment;
    MediaPageFragment mMediaPageFragment;
    MediaPreviewFragment mMediaPreviewFragment;

    private Toolbar mToolbar;
    /**
     * 提交选择了哪些附件
     */
    private TextView mTvOverAction;
    private TextView mTvToolbarTitle;
//    private View mToolbarDivider;

    private int mPagePosition;
    private int mSelectedIndex = 0;
    private int mPreviewPosition;
    /**
     * 被选中的附件集合
     */
    private ArrayList<MediaBean> mCheckedList;
    private ArrayList<MediaBean> mPageMediaList;

    @Override
    public int getContentView() {
        return R.layout.gallery_activity_media;
    }

    @Override
    protected void onCreateOk(@Nullable Bundle savedInstanceState) {
        mMediaGridFragment = MediaGridFragment.newInstance(mConfiguration);
        /**
         * 判断是单选还是多选，单选的话，“完成”按钮隐藏掉
         */
        if (!mConfiguration.isRadio()) {
            mTvOverAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mCheckedList == null || mCheckedList.size() == 0) return;
                    BaseResultEvent event = new ImageMultipleResultEvent(mCheckedList);
                    RxBus.getDefault().post(event);
                    finish();
                }
            });
            mTvOverAction.setVisibility(View.VISIBLE);
        } else {
            mTvOverAction.setVisibility(View.GONE);
        }
        mCheckedList = new ArrayList<>();
        List<MediaBean> selectedList = mConfiguration.getSelectedList();
        if (selectedList != null && selectedList.size() > 0) {
            mCheckedList.addAll(selectedList);
        }

        showMediaGridFragment();
        subscribeEvent();
    }

    @Override
    public void findViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("");
        mTvToolbarTitle = (TextView) findViewById(R.id.tv_toolbar_title);
        mTvOverAction = (TextView) findViewById(R.id.tv_over_action);
//        mToolbarDivider = findViewById(R.id.toolbar_divider);
    }

    @Override
    protected void setTheme() {
        Drawable closeDrawable = ThemeUtils.resolveDrawable(this, R.attr.gallery_toolbar_close_image, R.drawable.gallery_default_toolbar_close_image);
        int closeColor = ThemeUtils.resolveColor(this, R.attr.gallery_toolbar_close_color, R.color.gallery_default_toolbar_widget_color);
        closeDrawable.setColorFilter(closeColor, PorterDuff.Mode.SRC_ATOP);
        mToolbar.setNavigationIcon(closeDrawable);

        int overButtonBg = ThemeUtils.resolveDrawableRes(this, R.attr.gallery_toolbar_over_button_bg);
        if (overButtonBg != 0) {
            mTvOverAction.setBackgroundResource(overButtonBg);
        } else {
            OsCompat.setBackgroundDrawableCompat(mTvOverAction, createDefaultOverButtonBgDrawable());
        }

//        float overTextSize = ThemeUtils.resolveDimen(this, R.attr.gallery_toolbar_over_button_text_size, R.dimen.gallery_default_toolbar_over_button_text_size);
//        mTvOverAction.setTextSize(TypedValue.COMPLEX_UNIT_PX, overTextSize);

//        int overTextColor = ThemeUtils.resolveColor(this, R.attr.gallery_toolbar_over_button_text_color, R.color.gallery_default_toolbar_over_button_text_color);
//        mTvOverAction.setTextColor(overTextColor);

//        float titleTextSize = ThemeUtils.resolveDimen(this, R.attr.gallery_toolbar_text_size, R.dimen.gallery_default_toolbar_text_size);
//        mTvToolbarTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize);

//        int titleTextColor = ThemeUtils.resolveColor(this, R.attr.gallery_toolbar_text_color, R.color.gallery_default_toolbar_text_color);
//        mTvToolbarTitle.setTextColor(titleTextColor);

//        int gravity = ThemeUtils.resolveInteger(this, R.attr.gallery_toolbar_text_gravity, R.integer.gallery_default_toolbar_text_gravity);
//        mTvToolbarTitle.setLayoutParams(new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT, gravity));

//        int toolbarBg = ThemeUtils.resolveColor(this, R.attr.gallery_toolbar_bg, R.color.gallery_default_color_toolbar_bg);
//        mToolbar.setBackgroundColor(toolbarBg);

//        int toolbarHeight = (int) ThemeUtils.resolveDimen(this, R.attr.gallery_toolbar_height, R.dimen.gallery_default_toolbar_height);
//        mToolbar.setMinimumHeight(toolbarHeight);

//        int statusBarColor = ThemeUtils.resolveColor(this, R.attr.gallery_color_statusbar, R.color.gallery_default_color_statusbar);
//        ThemeUtils.setStatusBarColor(statusBarColor, getWindow());

//        int dividerHeight = (int) ThemeUtils.resolveDimen(this, R.attr.gallery_toolbar_divider_height, R.dimen.gallery_default_toolbar_divider_height);
//        int dividerBottomMargin = (int) ThemeUtils.resolveDimen(this, R.attr.gallery_toolbar_bottom_margin, R.dimen.gallery_default_toolbar_bottom_margin);
//        LinearLayout.LayoutParams dividerLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dividerHeight);
//        dividerLP.bottomMargin = dividerBottomMargin;
//        mToolbarDivider.setLayoutParams(dividerLP);

//        Drawable dividerDrawable = ThemeUtils.resolveDrawable(this, R.attr.gallery_toolbar_divider_bg, R.color.gallery_default_toolbar_divider_bg);
//        OsCompat.setBackgroundDrawableCompat(mToolbarDivider, dividerDrawable);

        setSupportActionBar(mToolbar);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mCheckedList != null) {
            outState.putParcelableArrayList(EXTRA_CHECKED_LIST, mCheckedList);
        }
        outState.putInt(EXTRA_SELECTED_INDEX, mSelectedIndex);
        if (mPageMediaList != null) {
            outState.putParcelableArrayList(EXTRA_PAGE_MEDIA_LIST, mPageMediaList);
        }
        outState.putInt(EXTRA_PAGE_POSITION, mPagePosition);
        outState.putInt(EXTRA_PREVIEW_POSITION, mPreviewPosition);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        List<MediaBean> list = savedInstanceState.getParcelableArrayList(EXTRA_CHECKED_LIST);
        if (list != null && list.size() > 0) {
            mCheckedList.clear();
            mCheckedList.addAll(list);
        }
        mPageMediaList = savedInstanceState.getParcelableArrayList(EXTRA_PAGE_MEDIA_LIST);
        mPagePosition = savedInstanceState.getInt(EXTRA_PAGE_POSITION);
        mPreviewPosition = savedInstanceState.getInt(EXTRA_PREVIEW_POSITION);
        mSelectedIndex = savedInstanceState.getInt(EXTRA_SELECTED_INDEX);
        if (!mConfiguration.isRadio()) {
            switch (mSelectedIndex) {
                case 1:
                    showMediaPageFragment(mPageMediaList, mPagePosition);
                    break;
                case 2:
                    showMediaPreviewFragment();
                    break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            backAction();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showMediaGridFragment() {
        mSelectedIndex = 0;
        mMediaPageFragment = null;
        mMediaPreviewFragment = null;

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, mMediaGridFragment);
        if (mMediaPreviewFragment != null) {
            ft.hide(mMediaPreviewFragment);
        }
        if (mMediaPageFragment != null) {
            ft.hide(mMediaPageFragment);
        }
        ft.show(mMediaGridFragment).commit();

        switch (mConfiguration.getVersionCode()) {
            case Configuration.VERSION_CODE_100:
                if (mConfiguration.isImage()) {
                    mTvToolbarTitle.setText(R.string.gallery_media_grid_image_title);
                } else {
                    mTvToolbarTitle.setText(R.string.gallery_media_grid_video_title);
                }
                break;
            case Configuration.VERSION_CODE_LAST:
                switch (mConfiguration.getMediaType()) {
                    case Configuration.MEDIA_TYPE_IMAGE:
                        mTvToolbarTitle.setText(R.string.gallery_media_grid_image_title);
                        break;
                    case Configuration.MEDIA_TYPE_VIDEO:
                        mTvToolbarTitle.setText(R.string.gallery_media_grid_video_title);
                        break;
                    case Configuration.MEDIA_TYPE_IMAGE_AND_VIDEO:
                        mTvToolbarTitle.setText(R.string.gallery_media_grid_image_and_video_title);
                        break;
                }
                break;
        }
    }

    @Override
    public void showMediaPageFragment(ArrayList<MediaBean> list, int position) {
        mSelectedIndex = 1;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        mMediaPageFragment = MediaPageFragment.newInstance(mConfiguration, list, position);
        ft.add(R.id.fragment_container, mMediaPageFragment);
        mMediaPreviewFragment = null;
        ft.hide(mMediaGridFragment);
        ft.show(mMediaPageFragment);
        ft.commit();

        String title = getString(R.string.gallery_page_title, position + 1, list.size());
        mTvToolbarTitle.setText(title);
    }

    @Override
    public void showMediaPreviewFragment() {
        mSelectedIndex = 2;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        mMediaPreviewFragment = MediaPreviewFragment.newInstance(mConfiguration, mPreviewPosition);
        ft.add(R.id.fragment_container, mMediaPreviewFragment);
        mMediaPageFragment = null;
        ft.hide(mMediaGridFragment);
        ft.show(mMediaPreviewFragment);
        ft.commit();

        int firstPreviewPosition = mPreviewPosition + 1;//第一次进预览界面显示的是第一张图片
        String title = getString(R.string.gallery_page_title, firstPreviewPosition, mCheckedList.size());
        mTvToolbarTitle.setText(title);
    }

    private void subscribeEvent() {
        Subscription subscriptionOpenMediaPreviewEvent = RxBus.getDefault().toObservable(OpenMediaPreviewFragmentEvent.class)
//                .map(mediaPreviewEvent -> mediaPreviewEvent)
                .subscribe(new RxBusSubscriber<OpenMediaPreviewFragmentEvent>() {
                    @Override
                    protected void onEvent(OpenMediaPreviewFragmentEvent openMediaPreviewFragmentEvent) {
                        mPreviewPosition = 0;
                        showMediaPreviewFragment();
                    }
                });

        RxBus.getDefault().add(subscriptionOpenMediaPreviewEvent);

        Subscription subscriptionMediaCheckChangeEvent = RxBus.getDefault().toObservable(MediaCheckChangeEvent.class)
//                .map(mediaCheckChangeEvent -> mediaCheckChangeEvent)
                .subscribe(new RxBusSubscriber<MediaCheckChangeEvent>() {
                    @Override
                    protected void onEvent(MediaCheckChangeEvent mediaCheckChangeEvent) {
                        MediaBean mediaBean = mediaCheckChangeEvent.getMediaBean();
                        if (mCheckedList.contains(mediaBean)) {
                            mCheckedList.remove(mediaBean);
                        } else {
                            mCheckedList.add(mediaBean);
                        }

                        if (mCheckedList.size() > 0) {
                            String text = getResources().getString(R.string.gallery_over_button_text_checked, mCheckedList.size(), mConfiguration.getMaxSize());
                            mTvOverAction.setText(text);
                            mTvOverAction.setEnabled(true);
                        } else {
                            mTvOverAction.setText(R.string.gallery_over_button_text);
                            mTvOverAction.setEnabled(false);
                        }
                    }
                });
        RxBus.getDefault().add(subscriptionMediaCheckChangeEvent);

        Subscription subscriptionMediaViewPagerChangedEvent = RxBus.getDefault().toObservable(MediaViewPagerChangedEvent.class)
//                .map(mediaViewPagerChangedEvent -> mediaViewPagerChangedEvent)
                .subscribe(new RxBusSubscriber<MediaViewPagerChangedEvent>() {
                    @Override
                    protected void onEvent(MediaViewPagerChangedEvent mediaPreviewViewPagerChangedEvent) {
                        int curIndex = mediaPreviewViewPagerChangedEvent.getCurIndex();
                        int totalSize = mediaPreviewViewPagerChangedEvent.getTotalSize();
                        if (mediaPreviewViewPagerChangedEvent.isPreview()) {
                            mPreviewPosition = curIndex;
                        } else {
                            mPagePosition = curIndex;
                        }
                        String title = getString(R.string.gallery_page_title, curIndex + 1, totalSize);
                        mTvToolbarTitle.setText(title);
                    }
                });
        RxBus.getDefault().add(subscriptionMediaViewPagerChangedEvent);

        Subscription subscriptionCloseRxMediaGridPageEvent = RxBus.getDefault().toObservable(CloseRxMediaGridPageEvent.class)
                .subscribe(new RxBusSubscriber<CloseRxMediaGridPageEvent>() {
                    @Override
                    protected void onEvent(CloseRxMediaGridPageEvent closeRxMediaGridPageEvent) throws Exception {
                        finish();
                    }
                });
        RxBus.getDefault().add(subscriptionCloseRxMediaGridPageEvent);

        Subscription subscriptionOpenMediaPageFragmentEvent = RxBus.getDefault().toObservable(OpenMediaPageFragmentEvent.class)
                .subscribe(new RxBusSubscriber<OpenMediaPageFragmentEvent>() {
                    @Override
                    protected void onEvent(OpenMediaPageFragmentEvent openMediaPageFragmentEvent) {
                        mPageMediaList = openMediaPageFragmentEvent.getMediaBeanList();
                        mPagePosition = openMediaPageFragmentEvent.getPosition();

                        showMediaPageFragment(mPageMediaList, mPagePosition);
                    }
                });
        RxBus.getDefault().add(subscriptionOpenMediaPageFragmentEvent);
    }

    public List<MediaBean> getCheckedList() {
        return mCheckedList;
    }

    private void backAction() {
        if ((mMediaPreviewFragment != null && mMediaPreviewFragment.isVisible())
                || (mMediaPageFragment != null && mMediaPageFragment.isVisible())) {
            showMediaGridFragment();
            return;
        }
        /**
         * @since 1.1.0
         */
        if (mMediaGridFragment != null && mMediaGridFragment.isVisible() && mMediaGridFragment.bucketOverviewIsShow()) {
            mMediaGridFragment.bucketOverview();
            return;
        }
        onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backAction();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.getDefault().removeAllStickyEvents();
        RxBus.getDefault().clear();
        RxJob.getDefault().clearJob();
    }

    private StateListDrawable createDefaultOverButtonBgDrawable() {
        int dp12 = (int) ThemeUtils.applyDimensionDp(this, 12.f);
        int dp8 = (int) ThemeUtils.applyDimensionDp(this, 8.f);
        float dp4 = ThemeUtils.applyDimensionDp(this, 4.f);
        float[] round = new float[]{dp4, dp4, dp4, dp4, dp4, dp4, dp4, dp4};
        ShapeDrawable pressedDrawable = new ShapeDrawable(new RoundRectShape(round, null, null));
        pressedDrawable.setPadding(dp12, dp8, dp12, dp8);
        int pressedColor = ThemeUtils.resolveColor(this, R.attr.gallery_toolbar_over_button_pressed_color, R.color.gallery_default_toolbar_over_button_pressed_color);
        pressedDrawable.getPaint().setColor(pressedColor);

        int normalColor = ThemeUtils.resolveColor(this, R.attr.gallery_toolbar_over_button_normal_color, R.color.gallery_default_toolbar_over_button_normal_color);
        ShapeDrawable normalDrawable = new ShapeDrawable(new RoundRectShape(round, null, null));
        normalDrawable.setPadding(dp12, dp8, dp12, dp8);
        normalDrawable.getPaint().setColor(normalColor);

        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);
        stateListDrawable.addState(new int[]{}, normalDrawable);

        return stateListDrawable;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Logger.i("onRequestPermissionsResult:requestCode=" + requestCode + " permissions=" + permissions[0]);
        switch (requestCode) {
            case REQUEST_STORAGE_READ_ACCESS_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    RxBus.getDefault().post(new RequestStorageReadAccessPermissionEvent(true));
                } else {
                    finish();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
