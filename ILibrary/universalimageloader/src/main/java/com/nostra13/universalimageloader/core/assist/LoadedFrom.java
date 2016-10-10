package com.nostra13.universalimageloader.core.assist;

/**
 * Source image loaded from.
 * 图片资源从哪加载的枚举，有从网络、磁盘缓存或内存缓存三种加载方式
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public enum LoadedFrom {
    NETWORK, DISC_CACHE, MEMORY_CACHE
}