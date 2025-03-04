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

package com.navercorp.fixturemonkey.api.option;

import static com.navercorp.fixturemonkey.api.constraint.JavaConstraintGenerator.DEFAULT_JAVA_CONSTRAINT_GENERATOR;
import static com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator.DEFAULT_NOTNULL_ANNOTATION_TYPES;
import static com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator.DEFAULT_NULLABLE_ANNOTATION_TYPES;
import static com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator.DEFAULT_NULL_INJECT;
import static com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions.DEFAULT_MAX_UNIQUE_GENERATION_COUNT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.arbitrary.JavaTimeArbitraryGeneratorSet;
import com.navercorp.fixturemonkey.api.arbitrary.JavaTypeArbitraryGeneratorSet;
import com.navercorp.fixturemonkey.api.constraint.JavaConstraintGenerator;
import com.navercorp.fixturemonkey.api.container.DecomposableJavaContainer;
import com.navercorp.fixturemonkey.api.container.DecomposedContainerValueFactory;
import com.navercorp.fixturemonkey.api.container.DefaultDecomposedContainerValueFactory;
import com.navercorp.fixturemonkey.api.experimental.InstantiatorProcessor;
import com.navercorp.fixturemonkey.api.experimental.JavaInstantiatorProcessor;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfoGenerator;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGenerator;
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator;
import com.navercorp.fixturemonkey.api.generator.IntrospectedArbitraryGenerator;
import com.navercorp.fixturemonkey.api.generator.JavaDefaultArbitraryGeneratorBuilder;
import com.navercorp.fixturemonkey.api.generator.MatchArbitraryGenerator;
import com.navercorp.fixturemonkey.api.generator.NullInjectGenerator;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.JavaArbitraryResolver;
import com.navercorp.fixturemonkey.api.introspector.JavaTimeArbitraryResolver;
import com.navercorp.fixturemonkey.api.introspector.JavaTimeTypeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.introspector.JavaTypeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.introspector.MatchArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.TypedArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.jqwik.JqwikJavaArbitraryResolver;
import com.navercorp.fixturemonkey.api.jqwik.JqwikJavaTimeArbitraryGeneratorSet;
import com.navercorp.fixturemonkey.api.jqwik.JqwikJavaTimeArbitraryResolver;
import com.navercorp.fixturemonkey.api.jqwik.JqwikJavaTypeArbitraryGeneratorSet;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.plugin.Plugin;
import com.navercorp.fixturemonkey.api.property.DefaultPropertyGenerator;
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.api.validator.ArbitraryValidator;

@SuppressWarnings("UnusedReturnValue")
@API(since = "0.6.0", status = Status.EXPERIMENTAL)
public final class FixtureMonkeyOptionsBuilder {
	private List<MatcherOperator<PropertyGenerator>> propertyGenerators =
		new ArrayList<>(FixtureMonkeyOptions.DEFAULT_PROPERTY_GENERATORS);
	private PropertyGenerator defaultPropertyGenerator = new DefaultPropertyGenerator();
	private List<MatcherOperator<ObjectPropertyGenerator>> arbitraryObjectPropertyGenerators =
		new ArrayList<>(FixtureMonkeyOptions.DEFAULT_OBJECT_PROPERTY_GENERATORS);
	private List<MatcherOperator<ContainerPropertyGenerator>> containerPropertyGenerators =
		new ArrayList<>(FixtureMonkeyOptions.DEFAULT_CONTAINER_PROPERTY_GENERATORS);
	private ObjectPropertyGenerator defaultObjectPropertyGenerator;
	private List<MatcherOperator<PropertyNameResolver>> propertyNameResolvers = new ArrayList<>();
	private PropertyNameResolver defaultPropertyNameResolver;
	private List<MatcherOperator<NullInjectGenerator>> nullInjectGenerators = new ArrayList<>();
	private NullInjectGenerator defaultNullInjectGenerator;
	private List<MatcherOperator<ArbitraryContainerInfoGenerator>> arbitraryContainerInfoGenerators = new ArrayList<>();
	private ArbitraryContainerInfoGenerator defaultArbitraryContainerInfoGenerator;
	@Deprecated
	private List<MatcherOperator<ArbitraryGenerator>> arbitraryGenerators = new ArrayList<>();
	private ArbitraryGenerator defaultArbitraryGenerator;
	private UnaryOperator<ArbitraryGenerator> defaultArbitraryGeneratorOperator = it -> it;
	private final List<MatcherOperator<ArbitraryIntrospector>> arbitraryIntrospectors = new ArrayList<>();
	private final JavaDefaultArbitraryGeneratorBuilder javaDefaultArbitraryGeneratorBuilder =
		IntrospectedArbitraryGenerator.javaBuilder();
	private boolean defaultNotNull = false;
	private boolean nullableContainer = false;
	private boolean nullableElement = false;
	private UnaryOperator<NullInjectGenerator> defaultNullInjectGeneratorOperator = it -> it;
	private ArbitraryValidator defaultArbitraryValidator = (obj) -> {
	};
	private DecomposedContainerValueFactory decomposedContainerValueFactory =
		new DefaultDecomposedContainerValueFactory(
			(obj) -> {
				throw new IllegalArgumentException(
					"given type is not supported container : " + obj.getClass().getTypeName());
			}
		);
	private final Map<Class<?>, DecomposedContainerValueFactory> decomposableContainerFactoryMap = new HashMap<>();
	private int generateMaxTries = CombinableArbitrary.DEFAULT_MAX_TRIES;
	private int generateUniqueMaxTries = DEFAULT_MAX_UNIQUE_GENERATION_COUNT;
	private JavaConstraintGenerator javaConstraintGenerator = DEFAULT_JAVA_CONSTRAINT_GENERATOR;
	private JavaTypeArbitraryGenerator javaTypeArbitraryGenerator = new JavaTypeArbitraryGenerator() {
	};
	@Nullable
	private JavaArbitraryResolver javaArbitraryResolver = null;
	private JavaTimeTypeArbitraryGenerator javaTimeTypeArbitraryGenerator = new JavaTimeTypeArbitraryGenerator() {
	};
	@Nullable
	private JavaTimeArbitraryResolver javaTimeArbitraryResolver = null;
	@Nullable
	private Function<JavaConstraintGenerator, JavaTypeArbitraryGeneratorSet> generateJavaTypeArbitrarySet = null;
	@Nullable
	private Function<JavaConstraintGenerator, JavaTimeArbitraryGeneratorSet> generateJavaTimeArbitrarySet = null;
	private InstantiatorProcessor instantiatorProcessor = new JavaInstantiatorProcessor();

	FixtureMonkeyOptionsBuilder() {
	}

	public FixtureMonkeyOptionsBuilder propertyGenerators(List<MatcherOperator<PropertyGenerator>> propertyGenerators) {
		this.propertyGenerators = propertyGenerators;
		return this;
	}

	public FixtureMonkeyOptionsBuilder insertFirstPropertyGenerator(
		MatcherOperator<PropertyGenerator> propertyGenerator
	) {
		List<MatcherOperator<PropertyGenerator>> result =
			insertFirst(this.propertyGenerators, propertyGenerator);
		return this.propertyGenerators(result);
	}

	public FixtureMonkeyOptionsBuilder insertFirstPropertyGenerator(
		Matcher matcher,
		PropertyGenerator propertyGenerator
	) {
		return this.insertFirstPropertyGenerator(
			new MatcherOperator<>(matcher, propertyGenerator)
		);
	}

	public FixtureMonkeyOptionsBuilder insertFirstPropertyGenerator(
		Class<?> type,
		PropertyGenerator propertyGenerator
	) {
		return this.insertFirstPropertyGenerator(
			MatcherOperator.assignableTypeMatchOperator(type, propertyGenerator)
		);
	}

	public FixtureMonkeyOptionsBuilder defaultPropertyGenerator(PropertyGenerator propertyGenerator) {
		this.defaultPropertyGenerator = propertyGenerator;
		return this;
	}

	public FixtureMonkeyOptionsBuilder arbitraryObjectPropertyGenerators(
		List<MatcherOperator<ObjectPropertyGenerator>> arbitraryObjectPropertyGenerators
	) {
		this.arbitraryObjectPropertyGenerators = arbitraryObjectPropertyGenerators;
		return this;
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryObjectPropertyGenerator(
		MatcherOperator<ObjectPropertyGenerator> arbitraryObjectPropertyGenerator
	) {
		List<MatcherOperator<ObjectPropertyGenerator>> result =
			insertFirst(this.arbitraryObjectPropertyGenerators, arbitraryObjectPropertyGenerator);
		return this.arbitraryObjectPropertyGenerators(result);
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryObjectPropertyGenerator(
		Matcher matcher,
		ObjectPropertyGenerator objectPropertyGenerator
	) {
		return this.insertFirstArbitraryObjectPropertyGenerator(
			new MatcherOperator<>(matcher, objectPropertyGenerator)
		);
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryObjectPropertyGenerator(
		Class<?> type,
		ObjectPropertyGenerator objectPropertyGenerator
	) {
		return this.insertFirstArbitraryObjectPropertyGenerator(
			MatcherOperator.assignableTypeMatchOperator(type, objectPropertyGenerator)
		);
	}

	public FixtureMonkeyOptionsBuilder arbitraryContainerPropertyGenerators(
		List<MatcherOperator<ContainerPropertyGenerator>> arbitraryContainerPropertyGenerators
	) {
		this.containerPropertyGenerators = arbitraryContainerPropertyGenerators;
		return this;
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryContainerPropertyGenerator(
		MatcherOperator<ContainerPropertyGenerator> arbitraryContainerPropertyGenerator
	) {
		List<MatcherOperator<ContainerPropertyGenerator>> result =
			insertFirst(this.containerPropertyGenerators, arbitraryContainerPropertyGenerator);
		return this.arbitraryContainerPropertyGenerators(result);
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryContainerPropertyGenerator(
		Matcher matcher,
		ContainerPropertyGenerator containerPropertyGenerator
	) {
		return this.insertFirstArbitraryContainerPropertyGenerator(
			new MatcherOperator<>(matcher, containerPropertyGenerator)
		);
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryContainerPropertyGenerator(
		Class<?> type,
		ContainerPropertyGenerator containerPropertyGenerator
	) {
		return this.insertFirstArbitraryContainerPropertyGenerator(
			MatcherOperator.assignableTypeMatchOperator(type, containerPropertyGenerator)
		);
	}

	public FixtureMonkeyOptionsBuilder defaultObjectPropertyGenerator(
		ObjectPropertyGenerator defaultObjectPropertyGenerator
	) {
		this.defaultObjectPropertyGenerator = defaultObjectPropertyGenerator;
		return this;
	}

	public FixtureMonkeyOptionsBuilder propertyNameResolvers(
		List<MatcherOperator<PropertyNameResolver>> propertyNameResolvers
	) {
		this.propertyNameResolvers = propertyNameResolvers;
		return this;
	}

	public FixtureMonkeyOptionsBuilder insertFirstPropertyNameResolver(
		MatcherOperator<PropertyNameResolver> propertyNameResolver
	) {
		List<MatcherOperator<PropertyNameResolver>> result =
			insertFirst(this.propertyNameResolvers, propertyNameResolver);
		return this.propertyNameResolvers(result);
	}

	public FixtureMonkeyOptionsBuilder insertFirstPropertyNameResolver(
		Matcher matcher,
		PropertyNameResolver propertyNameResolver
	) {
		return this.insertFirstPropertyNameResolver(new MatcherOperator<>(matcher, propertyNameResolver));
	}

	public FixtureMonkeyOptionsBuilder insertFirstPropertyNameResolver(
		Class<?> type,
		PropertyNameResolver propertyNameResolver
	) {
		return this.insertFirstPropertyNameResolver(
			MatcherOperator.assignableTypeMatchOperator(type, propertyNameResolver)
		);
	}

	public FixtureMonkeyOptionsBuilder defaultPropertyNameResolver(PropertyNameResolver defaultPropertyNameResolver) {
		this.defaultPropertyNameResolver = defaultPropertyNameResolver;
		return this;
	}

	public FixtureMonkeyOptionsBuilder nullInjectGenerators(
		List<MatcherOperator<NullInjectGenerator>> nullInjectGenerators
	) {
		this.nullInjectGenerators = nullInjectGenerators;
		return this;
	}

	public FixtureMonkeyOptionsBuilder insertFirstNullInjectGenerators(
		MatcherOperator<NullInjectGenerator> nullInjectGenerator
	) {
		List<MatcherOperator<NullInjectGenerator>> result =
			insertFirst(this.nullInjectGenerators, nullInjectGenerator);
		return this.nullInjectGenerators(result);
	}

	public FixtureMonkeyOptionsBuilder insertFirstNullInjectGenerators(
		Matcher matcher,
		NullInjectGenerator nullInjectGenerator
	) {
		return this.insertFirstNullInjectGenerators(new MatcherOperator<>(matcher, nullInjectGenerator));
	}

	public FixtureMonkeyOptionsBuilder insertFirstNullInjectGenerators(
		Class<?> type,
		NullInjectGenerator nullInjectGenerator
	) {
		return this.insertFirstNullInjectGenerators(
			MatcherOperator.assignableTypeMatchOperator(type, nullInjectGenerator)
		);
	}

	public FixtureMonkeyOptionsBuilder defaultNullInjectGenerator(NullInjectGenerator defaultNullInjectGenerator) {
		this.defaultNullInjectGenerator = defaultNullInjectGenerator;
		return this;
	}

	public FixtureMonkeyOptionsBuilder defaultNullInjectGeneratorOperator(
		UnaryOperator<NullInjectGenerator> defaultNullInjectGeneratorOperator
	) {
		this.defaultNullInjectGeneratorOperator = defaultNullInjectGeneratorOperator;
		return this;
	}

	public FixtureMonkeyOptionsBuilder arbitraryContainerInfoGenerators(
		List<MatcherOperator<ArbitraryContainerInfoGenerator>> arbitraryContainerInfoGenerators
	) {
		this.arbitraryContainerInfoGenerators = arbitraryContainerInfoGenerators;
		return this;
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryContainerInfoGenerator(
		MatcherOperator<ArbitraryContainerInfoGenerator> arbitraryContainerInfoGenerator
	) {
		List<MatcherOperator<ArbitraryContainerInfoGenerator>> result =
			insertFirst(this.arbitraryContainerInfoGenerators, arbitraryContainerInfoGenerator);
		return this.arbitraryContainerInfoGenerators(result);
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryContainerInfoGenerator(
		Matcher matcher,
		ArbitraryContainerInfoGenerator arbitraryContainerInfoGenerator
	) {
		return this.insertFirstArbitraryContainerInfoGenerator(
			new MatcherOperator<>(matcher, arbitraryContainerInfoGenerator)
		);
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryContainerInfoGenerator(
		Class<?> type,
		ArbitraryContainerInfoGenerator arbitraryContainerInfoGenerator
	) {
		return this.insertFirstArbitraryContainerInfoGenerator(
			MatcherOperator.assignableTypeMatchOperator(type, arbitraryContainerInfoGenerator)
		);
	}

	public FixtureMonkeyOptionsBuilder defaultArbitraryContainerInfoGenerator(
		ArbitraryContainerInfoGenerator defaultArbitraryContainerInfoGenerator) {
		this.defaultArbitraryContainerInfoGenerator = defaultArbitraryContainerInfoGenerator;
		return this;
	}

	@Deprecated
	public FixtureMonkeyOptionsBuilder arbitraryGenerators(
		List<MatcherOperator<ArbitraryGenerator>> arbitraryGenerators
	) {
		this.arbitraryGenerators = arbitraryGenerators;
		return this;
	}

	@Deprecated
	public FixtureMonkeyOptionsBuilder insertFirstArbitraryGenerator(
		MatcherOperator<ArbitraryGenerator> arbitraryGenerator
	) {
		List<MatcherOperator<ArbitraryGenerator>> result =
			insertFirst(this.arbitraryGenerators, arbitraryGenerator);
		return this.arbitraryGenerators(result);
	}

	@Deprecated
	public FixtureMonkeyOptionsBuilder insertFirstArbitraryGenerator(
		Matcher matcher,
		ArbitraryGenerator arbitraryGenerator
	) {
		return this.insertFirstArbitraryGenerator(
			new MatcherOperator<>(matcher, arbitraryGenerator)
		);
	}

	@Deprecated
	public FixtureMonkeyOptionsBuilder insertFirstArbitraryGenerator(
		Class<?> type,
		ArbitraryGenerator arbitraryGenerator
	) {
		return this.insertFirstArbitraryGenerator(
			MatcherOperator.assignableTypeMatchOperator(type, arbitraryGenerator)
		);
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryIntrospector(
		MatcherOperator<ArbitraryIntrospector> arbitraryIntrospector
	) {
		this.arbitraryIntrospectors.add(arbitraryIntrospector);
		return this;
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryIntrospector(
		Matcher matcher,
		ArbitraryIntrospector arbitraryIntrospector
	) {
		return this.insertFirstArbitraryIntrospector(
			new MatcherOperator<>(
				matcher,
				arbitraryIntrospector
			)
		);
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryIntrospector(
		Class<?> type,
		ArbitraryIntrospector arbitraryIntrospector
	) {
		return this.insertFirstArbitraryIntrospector(
			MatcherOperator.assignableTypeMatchOperator(type, arbitraryIntrospector)
		);
	}

	@Deprecated
	public FixtureMonkeyOptionsBuilder defaultArbitraryGenerator(ArbitraryGenerator defaultArbitraryGenerator) {
		this.defaultArbitraryGenerator = defaultArbitraryGenerator;
		return this;
	}

	public FixtureMonkeyOptionsBuilder defaultArbitraryGenerator(
		UnaryOperator<ArbitraryGenerator> defaultArbitraryGeneratorOperator
	) {
		this.defaultArbitraryGeneratorOperator = defaultArbitraryGeneratorOperator;
		return this;
	}

	public FixtureMonkeyOptionsBuilder priorityIntrospector(
		UnaryOperator<ArbitraryIntrospector> priorityIntrospector
	) {
		this.javaDefaultArbitraryGeneratorBuilder.priorityIntrospector(priorityIntrospector);
		return this;
	}

	public FixtureMonkeyOptionsBuilder containerIntrospector(
		UnaryOperator<ArbitraryIntrospector> containerIntrospector
	) {
		this.javaDefaultArbitraryGeneratorBuilder.containerIntrospector(containerIntrospector);
		return this;
	}

	public FixtureMonkeyOptionsBuilder objectIntrospector(
		UnaryOperator<ArbitraryIntrospector> objectIntrospector
	) {
		this.javaDefaultArbitraryGeneratorBuilder.objectIntrospector(objectIntrospector);
		return this;
	}

	public FixtureMonkeyOptionsBuilder fallbackIntrospector(
		UnaryOperator<ArbitraryIntrospector> fallbackIntrospector
	) {
		this.javaDefaultArbitraryGeneratorBuilder.fallbackIntrospector(fallbackIntrospector);
		return this;
	}

	@Deprecated
	public FixtureMonkeyOptionsBuilder javaTypeArbitraryGenerator(
		JavaTypeArbitraryGenerator javaTypeArbitraryGenerator
	) {
		this.javaTypeArbitraryGenerator = javaTypeArbitraryGenerator;
		return this;
	}

	@Deprecated
	public FixtureMonkeyOptionsBuilder javaArbitraryResolver(
		JavaArbitraryResolver javaArbitraryResolver
	) {
		this.javaArbitraryResolver = javaArbitraryResolver;
		return this;
	}

	@Deprecated
	public FixtureMonkeyOptionsBuilder javaTimeTypeArbitraryGenerator(
		JavaTimeTypeArbitraryGenerator javaTimeTypeArbitraryGenerator
	) {
		this.javaTimeTypeArbitraryGenerator = javaTimeTypeArbitraryGenerator;
		return this;
	}

	@Deprecated
	public FixtureMonkeyOptionsBuilder javaTimeArbitraryResolver(
		JavaTimeArbitraryResolver javaTimeArbitraryResolver
	) {
		this.javaTimeArbitraryResolver = javaTimeArbitraryResolver;
		return this;
	}

	public FixtureMonkeyOptionsBuilder plugin(Plugin plugin) {
		plugin.accept(this);
		return this;
	}

	public FixtureMonkeyOptionsBuilder defaultNotNull(boolean defaultNotNull) {
		this.defaultNotNull = defaultNotNull;
		return this;
	}

	public FixtureMonkeyOptionsBuilder nullableContainer(boolean nullableContainer) {
		this.nullableContainer = nullableContainer;
		return this;
	}

	public FixtureMonkeyOptionsBuilder nullableElement(boolean nullableElement) {
		this.nullableElement = nullableElement;
		return this;
	}

	public FixtureMonkeyOptionsBuilder defaultArbitraryValidator(ArbitraryValidator arbitraryValidator) {
		this.defaultArbitraryValidator = arbitraryValidator;
		return this;
	}

	public FixtureMonkeyOptionsBuilder decomposedContainerValueFactory(
		DecomposedContainerValueFactory decomposedContainerValueFactory
	) {
		this.decomposedContainerValueFactory = decomposedContainerValueFactory;
		return this;
	}

	public FixtureMonkeyOptionsBuilder addDecomposedContainerValueFactory(
		Class<?> type,
		DecomposedContainerValueFactory additionalDecomposedContainerValueFactory
	) {
		decomposableContainerFactoryMap.put(type, additionalDecomposedContainerValueFactory);
		return this;
	}

	public FixtureMonkeyOptionsBuilder generateMaxTries(int generateMaxCount) {
		this.generateMaxTries = generateMaxCount;
		return this;
	}

	public FixtureMonkeyOptionsBuilder generateUniqueMaxTries(int generateUniqueMaxTries) {
		this.generateUniqueMaxTries = generateUniqueMaxTries;
		return this;
	}

	public FixtureMonkeyOptionsBuilder javaConstraintGenerator(JavaConstraintGenerator javaConstraintGenerator) {
		this.javaConstraintGenerator = javaConstraintGenerator;
		return this;
	}

	public FixtureMonkeyOptionsBuilder javaTypeArbitraryGeneratorSet(
		Function<JavaConstraintGenerator, JavaTypeArbitraryGeneratorSet> generateJavaTypeArbitrarySet
	) {
		this.generateJavaTypeArbitrarySet = generateJavaTypeArbitrarySet;
		return this;
	}

	public FixtureMonkeyOptionsBuilder javaTimeArbitraryGeneratorSet(
		Function<JavaConstraintGenerator, JavaTimeArbitraryGeneratorSet> generateJavaTimeArbitrarySet
	) {
		this.generateJavaTimeArbitrarySet = generateJavaTimeArbitrarySet;
		return this;
	}

	public FixtureMonkeyOptionsBuilder instantiatorProcessor(
		InstantiatorProcessor instantiatorProcessor
	) {
		this.instantiatorProcessor = instantiatorProcessor;
		return this;
	}

	public FixtureMonkeyOptions build() {
		ObjectPropertyGenerator defaultObjectPropertyGenerator = defaultIfNull(
			this.defaultObjectPropertyGenerator,
			() -> FixtureMonkeyOptions.DEFAULT_OBJECT_PROPERTY_GENERATOR
		);
		PropertyNameResolver defaultPropertyNameResolver = defaultIfNull(
			this.defaultPropertyNameResolver,
			() -> FixtureMonkeyOptions.DEFAULT_PROPERTY_NAME_RESOLVER
		);

		NullInjectGenerator defaultNullInjectGenerator = defaultIfNull(
			this.defaultNullInjectGenerator,
			() -> new DefaultNullInjectGenerator(
				DEFAULT_NULL_INJECT,
				nullableContainer,
				defaultNotNull,
				nullableElement,
				new HashSet<>(DEFAULT_NULLABLE_ANNOTATION_TYPES),
				new HashSet<>(DEFAULT_NOTNULL_ANNOTATION_TYPES)
			)
		);

		if (defaultNullInjectGeneratorOperator != null) {
			defaultNullInjectGenerator = defaultNullInjectGeneratorOperator.apply(defaultNullInjectGenerator);
		}

		ArbitraryContainerInfoGenerator defaultArbitraryContainerInfoGenerator = defaultIfNull(
			this.defaultArbitraryContainerInfoGenerator,
			() -> context -> new ArbitraryContainerInfo(0, FixtureMonkeyOptions.DEFAULT_ARBITRARY_CONTAINER_MAX_SIZE)
		);

		JavaArbitraryResolver javaArbitraryResolver = defaultIfNull(
			this.javaArbitraryResolver,
			() -> new JqwikJavaArbitraryResolver(this.javaConstraintGenerator)
		);

		this.generateJavaTypeArbitrarySet = defaultIfNull(
			this.generateJavaTypeArbitrarySet,
			() -> constraintGenerator ->
				new JqwikJavaTypeArbitraryGeneratorSet(
					this.javaTypeArbitraryGenerator,
					javaArbitraryResolver
				)
		);

		javaDefaultArbitraryGeneratorBuilder.javaTypeArbitraryGeneratorSet(
			generateJavaTypeArbitrarySet.apply(javaConstraintGenerator)
		);

		JavaTimeArbitraryResolver javaTimeArbitraryResolver = defaultIfNull(
			this.javaTimeArbitraryResolver,
			() -> new JqwikJavaTimeArbitraryResolver(javaConstraintGenerator)
		);

		this.generateJavaTimeArbitrarySet = defaultIfNull(
			this.generateJavaTimeArbitrarySet,
			() -> constraintGenerator ->
				new JqwikJavaTimeArbitraryGeneratorSet(
					this.javaTimeTypeArbitraryGenerator,
					javaTimeArbitraryResolver
				)
		);

		javaDefaultArbitraryGeneratorBuilder.javaTimeArbitraryGeneratorSet(
			generateJavaTimeArbitrarySet.apply(javaConstraintGenerator)
		);

		ArbitraryGenerator defaultArbitraryGenerator =
			defaultIfNull(this.defaultArbitraryGenerator, this.javaDefaultArbitraryGeneratorBuilder::build);

		List<ArbitraryIntrospector> typedArbitraryIntrospectors = arbitraryIntrospectors.stream()
			.map(TypedArbitraryIntrospector::new)
			.collect(Collectors.toList());

		ArbitraryGenerator introspectedGenerator =
			new IntrospectedArbitraryGenerator(new MatchArbitraryIntrospector(typedArbitraryIntrospectors));

		defaultArbitraryGenerator = new MatchArbitraryGenerator(
			Arrays.asList(introspectedGenerator, defaultArbitraryGenerator)
		);

		DecomposedContainerValueFactory decomposedContainerValueFactory = new DefaultDecomposedContainerValueFactory(
			obj -> {
				Class<?> actualType = obj.getClass();
				for (
					Map.Entry<Class<?>, DecomposedContainerValueFactory> entry :
					this.decomposableContainerFactoryMap.entrySet()
				) {
					Class<?> type = entry.getKey();
					DecomposableJavaContainer decomposedValue = entry.getValue().from(obj);

					if (type.isAssignableFrom(actualType)) {
						return decomposedValue;
					}
				}
				return this.decomposedContainerValueFactory.from(obj);
			}
		);

		defaultArbitraryGenerator = defaultArbitraryGeneratorOperator.apply(defaultArbitraryGenerator);

		return new FixtureMonkeyOptions(
			this.propertyGenerators,
			this.defaultPropertyGenerator,
			this.arbitraryObjectPropertyGenerators,
			defaultObjectPropertyGenerator,
			this.containerPropertyGenerators,
			this.propertyNameResolvers,
			defaultPropertyNameResolver,
			this.nullInjectGenerators,
			defaultNullInjectGenerator,
			this.arbitraryContainerInfoGenerators,
			defaultArbitraryContainerInfoGenerator,
			this.arbitraryGenerators,
			defaultArbitraryGenerator,
			this.defaultArbitraryValidator,
			decomposedContainerValueFactory,
			this.generateMaxTries,
			this.generateUniqueMaxTries,
			this.javaConstraintGenerator,
			this.instantiatorProcessor
		);
	}

	private static <T> T defaultIfNull(@Nullable T obj, Supplier<T> defaultValue) {
		return obj != null ? obj : defaultValue.get();
	}

	private static <T> List<T> insertFirst(List<T> list, T value) {
		List<T> result = new ArrayList<>();
		result.add(value);
		result.addAll(list);
		return result;
	}
}
