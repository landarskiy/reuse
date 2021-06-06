package io.github.landarskiy.reuse.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
//TODO add name
annotation class ViewHolderType(val scopes: Array<String> = [])