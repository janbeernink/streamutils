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
package eu.jbeernink.util.stream.test.gatherer;

import static eu.jbeernink.util.stream.Gatherers.filterInstancesOf;
import static eu.jbeernink.util.stream.Gatherers.groupWhile;
import static eu.jbeernink.util.stream.Gatherers.randomOrder;
import static eu.jbeernink.util.stream.Gatherers.zip;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import eu.jbeernink.util.stream.testing.SequenceRandomOrderGenerator;

@DisplayName("Gatherers")
public class GathererTest {

	@Test
	@DisplayName("randomOrder(RandomGenerator) returns a gatherer which randomly orders stream elements.")
	void randomOrder_returnsRandomOrderGatherer() {
		Gatherer<String, ?, String> randomOrderGatherer =
				randomOrder(new SequenceRandomOrderGenerator(List.of(5L, 2L, 3L, 1L, 1L, 0L)));

		List<String> randomizedElements = Stream.of("a", "b", "c", "d", "e", "f").gather(randomOrderGatherer).toList();

		assertEquals(List.of("f", "c", "e", "b", "d", "a"), randomizedElements);
	}

	@Test
	@DisplayName("zip(Iterable, BiFunction) returns a gatherer which zips elements of the stream with the iterable.")
	void zipping_iterable_biFunction_returnsZippingGatherer() {
		List<String> otherElements = List.of("a", "b", "c");

		List<String> zippedElements = Stream.of("1", "2", "3")
		                                    .gather(zip(otherElements::iterator, (a, b) -> a + b))
		                                    .toList();

		assertEquals(List.of("1a", "2b", "3c"), zippedElements);
	}

	@Test
	@DisplayName("zip(Stream, BiFunction) returns a gatherer which zips elements of the stream with the other stream.")
	void zipping_stream_biFunction_returnsZippingGatherer() {
		List<String> otherElements = List.of("a", "b", "c");

		List<String> zippedElements = Stream.of("3", "2", "1")
		                                    .gather(zip(otherElements.stream(), (a, b) -> a + b))
		                                    .toList();

		assertEquals(List.of("3a", "2b", "1c"), zippedElements);
	}

	@Test
	@DisplayName("filterInstancesOf(Class) returns a gatherer which filters only the elements of a given type.")
	void filterInstancesOf_returnsTypeFilteringGatherer() {
		List<String> elements = Stream.of(new Object(), 1, "test")
		                              .gather(filterInstancesOf(String.class))
		                              .toList();

		assertEquals(List.of("test"), elements);
	}

	@Test
	@DisplayName("groupWhile(BiFunction<List<T>, T>) groups elements while the grouping function returns true.")
	void groupWhile_returnsTrueGroupingGatherer() {
		List<List<String>> groups = Stream.of("a", "b", "c", "AA", "d", "BB", "CC")
		                                  .gather(groupWhile(
				                                  (previousElements, element) -> previousElements.getFirst().length() ==
				                                                                 element.length()))
		                                  .toList();

		assertEquals(List.of(List.of("a", "b", "c"), List.of("AA"), List.of("d"), List.of("BB", "CC")), groups);
	}
}
