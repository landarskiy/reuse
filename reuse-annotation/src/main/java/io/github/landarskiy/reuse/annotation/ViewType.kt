package io.github.landarskiy.reuse.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ViewType(val scopes: Array<String> = [])