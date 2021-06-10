package io.github.landarskiy.reuse.compiler

import io.github.landarskiy.reuse.annotation.Factory
import java.util.*
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind

object ScopesBuilder {

    private const val SCOPE_DEFAULT = "Default"

    fun buildScopes(roundEnv: RoundEnvironment): Result {
        val scopes: MutableMap<String, MutableList<TypeInfo>> = mutableMapOf()
        roundEnv.getElementsAnnotatedWith(Factory::class.java)
            .forEach { element ->
                if (element.kind != ElementKind.CLASS) {
                    return Result.Error("Only classes can be annotated via ${Factory::class.simpleName} annotation")
                }
                val annotation = element.getAnnotation(Factory::class.java)
                val defaultScopeElements = scopes[SCOPE_DEFAULT] ?: mutableListOf()
                val name = if (annotation.name.isBlank()) {
                    element.simpleName.toString()
                } else {
                    annotation.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                }
                val typeInfo = TypeInfo(name, element)
                defaultScopeElements.add(typeInfo)
                scopes[SCOPE_DEFAULT] = defaultScopeElements
                annotation.scopes.map { scopeName ->
                    scopeName.replaceFirstChar { first ->
                        if (first.isLowerCase()) {
                            first.titlecase(Locale.getDefault())
                        } else {
                            first.toString()
                        }
                    }
                }.forEach { scope ->
                    val scopeElements = scopes[scope] ?: mutableListOf()
                    scopeElements.add(typeInfo)
                    scopes[scope] = scopeElements
                }
            }
        return Result.Success(scopes)
    }

    sealed class Result {
        data class Success(val scopes: Map<String, List<TypeInfo>>) : Result()

        data class Error(val message: String) : Result()
    }

    data class TypeInfo(val name: String, val element: Element)
}