package io.github.landarskiy.reuse.annotation

/**
 * Entry point for data builders. Should use only on interfaces
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ReuseModule