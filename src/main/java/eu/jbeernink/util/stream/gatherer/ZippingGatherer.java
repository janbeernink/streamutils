/*
 * Copyright © 2025 Jan Beernink
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.jbeernink.util.stream.gatherer;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

public class ZippingGatherer<T, O, R> implements Gatherer<T, Iterator<O>, R> {

	private final Supplier<Iterator<O>> otherElementsSupplier;
	private final BiFunction<? super T, ? super O, ? extends R> zipFunction;

	public ZippingGatherer(Supplier<Iterator<O>> otherElementsSupplier, BiFunction<? super T, ? super O, ? extends R> zipFunction) {
		this.otherElementsSupplier = otherElementsSupplier;
		this.zipFunction = zipFunction;
	}

	@Override
	public Supplier<Iterator<O>> initializer() {
		return otherElementsSupplier;
	}

	@Override
	public Integrator<Iterator<O>, T, R> integrator() {
		return (iterator, element, downstream) -> {
			if (downstream.isRejecting()) {
				return false;
			}

			if (!iterator.hasNext()) {
				return false;
			}

			return downstream.push(zipFunction.apply(element, iterator.next()));
		};
	}
}
