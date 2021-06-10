package io.github.landarskiy.reuse.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Factory(val name: String = "", val scopes: Array<String> = [])