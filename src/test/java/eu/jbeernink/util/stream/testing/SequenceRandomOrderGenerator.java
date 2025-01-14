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
package eu.jbeernink.util.stream.testing;

import java.util.List;
import java.util.random.RandomGenerator;

public class SequenceRandomOrderGenerator implements RandomGenerator {

	private final List<Long> numbers;

	private int currentIndex = 0;

	public SequenceRandomOrderGenerator(List<Long> numbers) {
		this.numbers = List.copyOf(numbers);
	}

	@Override
	public int nextInt() {
		return (int) nextLong();
	}

	@Override
	public int nextInt(int bound) {
		return nextInt() % bound;
	}

	@Override
	public long nextLong() {
		long next = numbers.get(currentIndex);
		currentIndex = (currentIndex + 1) % numbers.size();
		return next;
	}
}
