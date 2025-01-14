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

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import java.util.stream.Gatherer;

public final class RandomOrderGatherer<T> implements Gatherer<T, List<T>, T> {

	private final RandomGenerator randomGenerator;

	public RandomOrderGatherer(RandomGenerator randomGenerator) {
		this.randomGenerator = randomGenerator;
	}

	@Override
	public Supplier<List<T>> initializer() {
		return ArrayList::new;
	}

	@Override
	public Integrator<List<T>, T, T> integrator() {
		return (elements, element, _) -> elements.add(element);
	}

	@Override
	public BiConsumer<List<T>, Downstream<? super T>> finisher() {
		return (elements, downstream) -> {
			boolean isAccepting = !downstream.isRejecting();
			while (!elements.isEmpty() && isAccepting) {
				int nextIndex = randomGenerator.nextInt(elements.size());
				isAccepting = downstream.push(elements.remove(nextIndex));
			}
		};
	}
}
