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

package com.navercorp.fixturemonkey.api.property;

import java.lang.reflect.AnnotatedType;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * It generates child properties of the given {@link Property}.
 * Property could be field or JavaBeans property or method or constructor parameter.
 * It should generate properties whose name is unique.
 */
@API(since = "0.5.3", status = Status.MAINTAINED)
@FunctionalInterface
public interface PropertyGenerator {
	List<Property> generateChildProperties(Property property);

	@Deprecated
	default List<Property> generateChildProperties(AnnotatedType annotatedType) {
		return generateChildProperties(new RootProperty(annotatedType));
	}
}
