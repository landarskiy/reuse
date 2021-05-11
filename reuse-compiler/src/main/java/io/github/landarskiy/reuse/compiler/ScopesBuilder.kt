package io.github.landarskiy.reuse.compiler

import io.github.landarskiy.reuse.annotation.ViewType
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind

object ScopesBuilder {

    private const val SCOPE_DEFAULT = "Default"

    fun buildScopes(roundEnv: RoundEnvironment): Result {
        val scopes: MutableMap<String, MutableList<Element>> = mutableMapOf()
        roundEnv.getElementsAnnotatedWith(ViewType::class.java)
            .forEach {
                if (it.kind != ElementKind.CLASS) {
                    return Result.Error("Only classes can be annotated via ${ViewType::class.simpleName} annotation")
                }
                val defaultScopeElements = scopes[SCOPE_DEFAULT] ?: mutableListOf()
                defaultScopeElements.add(it)
                scopes[SCOPE_DEFAULT] = defaultScopeElements
                it.getAnnotation(ViewType::class.java).scopes.forEach { scope ->
                    val scopeElements = scopes[scope] ?: mutableListOf()
                    scopeElements.add(it)
                    scopes[scope] = scopeElements
                }
            }
        return Result.Success(scopes)
    }

    sealed class Result {
        data class Success(val scopes: Map<String, List<Element>>) : Result()

        data class Error(val message: String) : Result()
    }
}