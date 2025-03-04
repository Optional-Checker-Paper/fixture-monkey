/*
 * Fixture Monkey
 *
 * Copyright (c) 2021-present NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.fixturemonkey.kotlin.experimental

import com.navercorp.fixturemonkey.api.experimental.ConstructorInstantiator
import com.navercorp.fixturemonkey.api.experimental.FactoryMethodInstantiator
import com.navercorp.fixturemonkey.api.experimental.Instantiator
import com.navercorp.fixturemonkey.api.experimental.InstantiatorProcessResult
import com.navercorp.fixturemonkey.api.experimental.InstantiatorProcessor
import com.navercorp.fixturemonkey.api.experimental.InstantiatorUtils.resolveParameterTypes
import com.navercorp.fixturemonkey.api.experimental.InstantiatorUtils.resolvedParameterNames
import com.navercorp.fixturemonkey.api.experimental.JavaBeansPropertyInstantiator
import com.navercorp.fixturemonkey.api.experimental.JavaFieldPropertyInstantiator
import com.navercorp.fixturemonkey.api.experimental.PropertyInstantiator
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector
import com.navercorp.fixturemonkey.api.introspector.ConstructorArbitraryIntrospector
import com.navercorp.fixturemonkey.api.introspector.ConstructorArbitraryIntrospector.ConstructorWithParameterNames
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector
import com.navercorp.fixturemonkey.api.property.ConstructorProperty
import com.navercorp.fixturemonkey.api.property.FieldPropertyGenerator
import com.navercorp.fixturemonkey.api.property.JavaBeansPropertyGenerator
import com.navercorp.fixturemonkey.api.property.MethodParameterProperty
import com.navercorp.fixturemonkey.api.property.Property
import com.navercorp.fixturemonkey.api.property.PropertyUtils
import com.navercorp.fixturemonkey.api.type.TypeReference
import com.navercorp.fixturemonkey.api.type.Types
import com.navercorp.fixturemonkey.api.type.Types.getDeclaredConstructor
import com.navercorp.fixturemonkey.kotlin.introspector.CompanionObjectFactoryMethodIntrospector
import com.navercorp.fixturemonkey.kotlin.introspector.KotlinPropertyArbitraryIntrospector
import com.navercorp.fixturemonkey.kotlin.property.KotlinPropertyGenerator
import com.navercorp.fixturemonkey.kotlin.type.actualType
import com.navercorp.fixturemonkey.kotlin.type.toTypeReference
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.kotlinFunction

class KotlinInstantiatorProcessor : InstantiatorProcessor {
    override fun process(typeReference: TypeReference<*>, instantiator: Instantiator): InstantiatorProcessResult {
        return when (instantiator) {
            is ConstructorInstantiator<*> -> processConstructor(typeReference, instantiator)
            is FactoryMethodInstantiator<*> -> processFactoryMethod(typeReference, instantiator)
            is KotlinPropertyInstantiator<*> -> processProperty(typeReference, instantiator)
            is JavaFieldPropertyInstantiator<*> -> processJavaField(typeReference, instantiator)
            is JavaBeansPropertyInstantiator<*> -> processJavaBeansProperty(typeReference, instantiator)
            else -> throw IllegalArgumentException("Given instantiator is not valid. instantiator: ${instantiator.javaClass}")
        }
    }

    private fun processConstructor(
        typeReference: TypeReference<*>,
        instantiator: ConstructorInstantiator<*>,
    ): InstantiatorProcessResult {
        val parameterTypes =
            instantiator.inputParameterTypes.map { it.type.actualType() }.toTypedArray()
        val declaredConstructor = getDeclaredConstructor(typeReference.type.actualType(), *parameterTypes)
        val kotlinConstructor = declaredConstructor.kotlinFunction!!
        val parameters: List<KParameter> = kotlinConstructor.parameters

        val inputParameterTypes = instantiator.inputParameterTypes
        val inputParameterNames = instantiator.inputParameterNames
        val parameterTypeReferences = parameters.map { it.type.javaType.toTypeReference() }
        val parameterNames = parameters.map { it.name }

        val resolveParameterTypes = resolveParameterTypes(parameterTypeReferences, inputParameterTypes)
        val resolveParameterName = resolvedParameterNames(parameterNames, inputParameterNames)

        val constructorParameterProperties = parameters.mapIndexed { index, kParameter ->
            val resolvedParameterTypeReference = resolveParameterTypes[index]
            val resolvedParameterName = resolveParameterName[index]

            ConstructorProperty(
                resolvedParameterTypeReference.annotatedType,
                declaredConstructor,
                resolvedParameterName,
                null,
                kParameter.type.isMarkedNullable,
            )
        }

        return InstantiatorProcessResult(
            ConstructorArbitraryIntrospector(
                ConstructorWithParameterNames(
                    declaredConstructor,
                    resolveParameterName,
                ),
            ),
            constructorParameterProperties,
        )
    }

    private fun processProperty(
        typeReference: TypeReference<*>,
        instantiator: KotlinPropertyInstantiator<*>,
    ): InstantiatorProcessResult {
        val property = PropertyUtils.toProperty(typeReference)
        val propertyFilter = instantiator.propertyFilter
        val properties = KotlinPropertyGenerator(
            javaDelegatePropertyGenerator = { listOf() },
            propertyFilter = propertyFilter,
        ).generateChildProperties(property)

        return InstantiatorProcessResult(
            KotlinPropertyArbitraryIntrospector.INSTANCE,
            properties,
        )
    }

    private fun processFactoryMethod(
        typeReference: TypeReference<*>,
        instantiator: FactoryMethodInstantiator<*>,
    ): InstantiatorProcessResult {
        val type = typeReference.type.actualType()
        val inputParameterTypes = instantiator.inputParameterTypes.map { it.type.actualType() }
            .toTypedArray()
        val kotlinType = type.kotlin
        val companionMethod = kotlinType.companionObject?.memberFunctions
            ?.findDeclaredMemberFunction(inputParameterTypes)
            ?: throw IllegalArgumentException("Given type $kotlinType has no static factory method.")

        val companionMethodParameters = companionMethod.parameters.filter { it.kind != KParameter.Kind.INSTANCE }
        val methodParameterTypeReferences = companionMethodParameters.map { it.type.toTypeReference() }
        val methodParameterNames = companionMethodParameters.map { it.name }
        val inputParameterTypesReferences = instantiator.inputParameterTypes
        val inputParameterNames = instantiator.inputParameterNames

        val resolvedParameterTypes =
            resolveParameterTypes(methodParameterTypeReferences, inputParameterTypesReferences)
        val resolvedParameterNames = resolvedParameterNames(methodParameterNames, inputParameterNames)

        val properties = companionMethod.toParameterProperty(
            resolvedParameterTypes = resolvedParameterTypes,
            resolvedParameterNames = resolvedParameterNames,
        )
        return InstantiatorProcessResult(
            CompanionObjectFactoryMethodIntrospector(companionMethod),
            properties,
        )
    }

    private fun processJavaField(
        typeReference: TypeReference<*>,
        instantiator: JavaFieldPropertyInstantiator<*>,
    ): InstantiatorProcessResult {
        val property = PropertyUtils.toProperty(typeReference)
        val filterPredicate = instantiator.fieldPredicate
        val properties = FieldPropertyGenerator(filterPredicate) { true }
            .generateChildProperties(property)

        return InstantiatorProcessResult(
            FieldReflectionArbitraryIntrospector.INSTANCE,
            properties,
        )
    }

    private fun processJavaBeansProperty(
        typeReference: TypeReference<*>,
        instantiator: JavaBeansPropertyInstantiator<*>,
    ): InstantiatorProcessResult {
        val property = PropertyUtils.toProperty(typeReference)
        val propertyDescriptorPredicate = instantiator.propertyDescriptorPredicate
        val properties = JavaBeansPropertyGenerator(propertyDescriptorPredicate) { true }
            .generateChildProperties(property)

        return InstantiatorProcessResult(
            BeanArbitraryIntrospector.INSTANCE,
            properties,
        )
    }

    private fun KFunction<*>.toParameterProperty(
        resolvedParameterTypes: List<TypeReference<*>>,
        resolvedParameterNames: List<String>,
    ): List<Property> = this.parameters
        .filter { parameter -> parameter.kind != KParameter.Kind.INSTANCE }
        .mapIndexed { index, kParameter ->
            MethodParameterProperty(
                resolvedParameterTypes[index].annotatedType,
                resolvedParameterNames[index],
                kParameter.type.isMarkedNullable,
            )
        }

    private fun Collection<KFunction<*>>.findDeclaredMemberFunction(inputParameterTypes: Array<Class<*>>): KFunction<*>? =
        this.find { function ->
            function.parameters
                .filter { parameter -> parameter.kind != KParameter.Kind.INSTANCE }
                .map { parameter -> parameter.type.javaType.actualType() }
                .let {
                    Types.isAssignableTypes(it.toTypedArray(), inputParameterTypes)
                }
        }
}

/**
 * The [KotlinConstructorInstantiator] class is an implementation of the [ConstructorInstantiator] interface
 * specifically designed for use in Kotlin. It allows the dynamic construction of objects of type T using a constructor
 * by specifying the parameter types and names.
 *
 * @param T The type of objects that can be instantiated using this [Instantiator].
 */
class KotlinConstructorInstantiator<T> : ConstructorInstantiator<T> {
    val _types: MutableList<TypeReference<*>> = ArrayList()
    val _parameterNames: MutableList<String?> = ArrayList()

    /**
     * Specifies a constructor parameter with its type inferred using reified type parameters and, optionally,
     * a parameter name. Parameters should be specified in the order they appear in the constructor's parameter list.
     *
     * @param U The type of the constructor parameter, inferred using reified type parameters.
     * @param parameterName An optional parameter name for the constructor parameter.
     * @return This [KotlinConstructorInstantiator] instance with the specified parameter added.
     */
    inline fun <reified U> parameter(parameterName: String? = null): KotlinConstructorInstantiator<T> =
        this.apply {
            _types.add(object : TypeReference<U>() {})
            _parameterNames.add(parameterName)
        }

    /**
     * Get the list of types representing the input parameter types of the constructor.
     *
     * @return A list of types representing the input parameter types of the constructor.
     */
    override fun getInputParameterTypes(): List<TypeReference<*>> = _types

    /**
     * Get the list of string representing the input parameter names of the constructor.
     *
     * @return A list of string representing the input parameter names of the constructor.
     */
    override fun getInputParameterNames(): List<String?> = _parameterNames
}

class FactoryMethodInstantiatorKt<T> : FactoryMethodInstantiator<T> {
    val _types: MutableList<TypeReference<*>> = ArrayList()
    val _parameterNames: MutableList<String?> = ArrayList()

    inline fun <reified U> parameter(parameterName: String? = null): FactoryMethodInstantiatorKt<T> =
        this.apply {
            _types.add(object : TypeReference<U>() {})
            _parameterNames.add(parameterName)
        }

    override fun getInputParameterTypes(): List<TypeReference<*>> = _types

    override fun getInputParameterNames(): List<String?> = _parameterNames
}

class KotlinPropertyInstantiator<T>(
    internal var propertyFilter: (property: KProperty<*>) -> Boolean = { true },
) : PropertyInstantiator<T> {
    fun filter(propertyFilter: (KProperty<*>) -> Boolean): KotlinPropertyInstantiator<T> {
        this.propertyFilter = propertyFilter
        return this
    }
}
