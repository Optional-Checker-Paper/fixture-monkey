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

package com.navercorp.fixturemonkey.jackson.generator;

import static com.navercorp.fixturemonkey.jackson.property.JacksonAnnotations.getJacksonAnnotation;
import static com.navercorp.fixturemonkey.jackson.property.JacksonAnnotations.getRandomJsonSubType;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import com.navercorp.fixturemonkey.api.generator.ObjectProperty;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGeneratorContext;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.2", status = Status.MAINTAINED)
public final class PropertyJsonSubTypesObjectPropertyGenerator implements ObjectPropertyGenerator {
	public static final PropertyJsonSubTypesObjectPropertyGenerator INSTANCE =
		new PropertyJsonSubTypesObjectPropertyGenerator();

	@Override
	public ObjectProperty generate(ObjectPropertyGeneratorContext context) {
		Property property = context.getProperty();
		double nullInject = context.getNullInjectGenerator()
			.generate(context);
		PropertyGenerator propertyGenerator = context.getPropertyGenerator();

		JsonSubTypes jsonSubTypes = getJacksonAnnotation(property, JsonSubTypes.class);
		if (jsonSubTypes == null) {
			throw new IllegalArgumentException("@JsonSubTypes is not found " + property.getType().getTypeName());
		}

		Class<?> type = getRandomJsonSubType(jsonSubTypes);
		AnnotatedType annotatedType = Types.generateAnnotatedTypeWithoutAnnotation(type);
		List<Property> childProperties = propertyGenerator.generateChildProperties(annotatedType);

		JsonTypeInfo jsonTypeInfo = getJacksonAnnotation(property, JsonTypeInfo.class);
		List<Annotation> annotations = new ArrayList<>(property.getAnnotations());
		annotations.add(jsonTypeInfo);

		Property actualProperty = new Property() {
			@Override
			public Type getType() {
				return type;
			}

			@Override
			public AnnotatedType getAnnotatedType() {
				return annotatedType;
			}

			@Nullable
			@Override
			public String getName() {
				return property.getName();
			}

			@Override
			public List<Annotation> getAnnotations() {
				return Collections.unmodifiableList(annotations);
			}

			@Nullable
			@Override
			public Object getValue(Object instance) {
				return property.getValue(instance);
			}
		};
		return new ObjectProperty(
			actualProperty,
			context.getPropertyNameResolver(),
			nullInject,
			context.getElementIndex(),
			Collections.singletonMap(actualProperty, childProperties)
		);
	}
}
