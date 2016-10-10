/*******************************************************************************
 * Copyright 2011-2014 Sergey Tarasevich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.nostra13.universalimageloader.core.assist;

/**
 * Type of image scaling during decoding.
 * 图片在解码期间的缩放比例类型
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @since 1.5.0
 */
public enum ImageScaleType {
    /**
     * Image won't be scaled
     * 图片不被缩放
     */
    NONE,
    /**
     * Image will be scaled down only if image size is greater than
     * {@linkplain javax.microedition.khronos.opengles.GL10#GL_MAX_TEXTURE_SIZE maximum acceptable texture size}.
     * Usually it's 2048x2048.<br />
     * If Bitmap is expected to display than it must not exceed this size (otherwise you'll get the exception
     * "OpenGLRenderer: Bitmap too large to be uploaded into a texture".<br />
     * Image will be subsampled in an integer number of times (1, 2, 3, ...) to maximum texture size of device.
     * 图片因过大而被缩小
     */
    NONE_SAFE,
    /**
     * Image will be reduces 2-fold until next reduce step make image smaller target size.<br />
     * It's <b>fast</b> type and it's preferable for usage in lists/grids/galleries (and other
     * {@linkplain android.widget.AdapterView adapter-views}) .<br />
     * Relates to {@link android.graphics.BitmapFactory.Options#inSampleSize}<br />
     * Note: If original image size is smaller than target size then original image <b>won't</b> be scaled.
     * 图片被降低2倍（如果图片原始尺寸比目标尺寸小，那就不用缩小）
     */
    IN_SAMPLE_POWER_OF_2,
    /**
     * Image will be subsampled in an integer number of times (1, 2, 3, ...). Use it if memory economy is quite
     * important.<br />
     * Relates to {@link android.graphics.BitmapFactory.Options#inSampleSize}<br />
     * Note: If original image size is smaller than target size then original image <b>won't</b> be scaled.
     */
    IN_SAMPLE_INT,
    /**
     * Image will scaled-down exactly to target size (scaled width or height or both will be equal to target size;
     * depends on {@linkplain android.widget.ImageView.ScaleType ImageView's scale type}). Use it if memory economy is
     * critically important.<br />
     * <b>Note:</b> If original image size is smaller than target size then original image <b>won't</b> be scaled.<br />
     * <br />
     * <b>NOTE:</b> For creating result Bitmap (of exact size) additional Bitmap will be created with
     * {@link android.graphics.Bitmap#createBitmap(android.graphics.Bitmap, int, int, int, int, android.graphics.Matrix, boolean)
     * Bitmap.createBitmap(...)}.<br />
     * <b>Cons:</b> Saves memory by keeping smaller Bitmap in memory cache (comparing with IN_SAMPLE... scale types)<br />
     * <b>Pros:</b> Requires more memory in one time for creation of result Bitmap.
     * 图片被缩小到目标尺寸大小（如果图片原始尺寸比目标尺寸小，那就不用缩小）
     */
    EXACTLY,
    /**
     * Image will scaled exactly to target size (scaled width or height or both will be equal to target size; depends on
     * {@linkplain android.widget.ImageView.ScaleType ImageView's scale type}). Use it if memory economy is critically
     * important.<br />
     * <b>Note:</b> If original image size is smaller than target size then original image <b>will be stretched</b> to
     * target size.<br />
     * <br />
     * <b>NOTE:</b> For creating result Bitmap (of exact size) additional Bitmap will be created with
     * {@link android.graphics.Bitmap#createBitmap(android.graphics.Bitmap, int, int, int, int, android.graphics.Matrix, boolean)
     * Bitmap.createBitmap(...)}.<br />
     * <b>Cons:</b> Saves memory by keeping smaller Bitmap in memory cache (comparing with IN_SAMPLE... scale types)<br />
     * <b>Pros:</b> Requires more memory in one time for creation of result Bitmap.
     * 图片被缩放到目标尺寸大小（即图片相对目标尺寸过大则缩小到目标尺寸大小，反之进行拉伸操作）
     */
    EXACTLY_STRETCHED
}
