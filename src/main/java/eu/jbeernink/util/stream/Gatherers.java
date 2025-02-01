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
package eu.jbeernink.util.stream;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.random.RandomGenerator;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

import eu.jbeernink.util.stream.gatherer.InterleavingGatherer;
import eu.jbeernink.util.stream.gatherer.RandomOrderGatherer;
import eu.jbeernink.util.stream.gatherer.ZippingGatherer;

/// Collection of [Gatherer] implementations.
public final class Gatherers {

	/// Returns a [Gatherer] that reorders elements in a random order.
	///
	/// The returned [Gatherer] will consume the elements eagerly and will only start outputting elements once reaching
	/// the end of the [Stream]. This means that this operation will not complete on infinite streams.
	///
	/// @param <T> the type of elements in the [Stream].
	/// @return a [Gatherer] which reorders the elements of a stream in random order.
	public static <T> Gatherer<T, ?, T> randomOrder() {
		return randomOrder(RandomGenerator.getDefault());
	}

	/// Returns a [Gatherer] that reorders elements of a [Stream] in a random order using the given [RandomGenerator].
	///
	/// The returned [Gatherer] will consume the elements eagerly and will only start outputting elements once reaching
	/// the end of the [Stream]. This means that this operation will not complete on infinite streams.
	///
	/// @param randomGenerator the random generator to use when ordering the [Stream].
	/// @param <T>             the type of elements.
	/// @return a gatherer that randomly orders the elements of a stream.
	public static <T> Gatherer<T, ?, T> randomOrder(RandomGenerator randomGenerator) {
		return new RandomOrderGatherer<>(randomGenerator);
	}

	/// Returns a [Gatherer] that combines the elements of a [Stream] with the elements from another [Iterable].
	///
	/// @param otherElements the [Iterable] to retrieve the other elements from.
	/// @param zipFunction   a function which combines an element from the stream with an element from the iterable.
	/// @param <T>           the type of elements in the [Stream].
	/// @param <O>           the type of elements in `otherElements`.
	/// @param <R>           the type of combined elements produced by `zipFunction`.
	/// @return a [Gatherer] that combines the elements of stream together with the elements of another [Iterable].
	public static <T, O, R> Gatherer<T, ?, R> zip(Iterable<O> otherElements,
	                                              BiFunction<? super T, ? super O, ? extends R> zipFunction) {
		return new ZippingGatherer<>(otherElements::iterator, zipFunction);
	}

	/// Returns a [Gatherer] that combines the elements of a [Stream] with the elements from another [Stream].
	///
	/// @param otherStream the [Stream] to retrieve the other elements from.
	/// @param zipFunction a function which combines an element from the stream with an element from the iterable.
	/// @param <T>         the type of elements in the [Stream].
	/// @param <O>         the type of elements in `otherStream`.
	/// @param <R>         the type of combined elements produced by `zipFunction`.
	/// @return a [Gatherer] that combines elements of the [Stream] with another [Stream].
	public static <T, O, R> Gatherer<T, ?, R> zip(Stream<? extends O> otherStream,
	                                              BiFunction<? super T, ? super O, ? extends R> zipFunction) {
		return new ZippingGatherer<>(otherStream::iterator, zipFunction);
	}

	/// Returns a [Gatherer] that will interleave elements from another [Stream] into the current [Stream].
	///
	/// The resulting [Stream] after this [Gatherer] will consist of one element of the current [Stream]
	/// (while any elements remain), then an element from the interleaved [Stream] (while any elements remain).
	///
	/// If either [Stream] runs out of elements, then this [Gatherer] will keep taking elements only from the other
	/// [Stream].
	///
	/// It is the caller's responsibility to close the interleaved [Stream], if that is necessary.
	///
	/// @param <T>    The type of elements in the stream.
	/// @param stream the stream to interleave into the current stream.
	/// @return a gatherer that combines interleaves elements from another stream into the current stream.
	public static <T> Gatherer<T, ?, T> interleave(Stream<? extends T> stream) {
		return new InterleavingGatherer<>(stream::iterator);
	}

	/// Returns a [Gatherer] that will interleave elements from an [Iterable] into the current [Stream].
	///
	/// The resulting [Stream] after this [Gatherer] will consist of one element of the current [Stream]
	/// (while any elements remain), then an element from the interleaved [Iterable] (while any elements remain).
	///
	/// If either [Stream] runs out of elements, then this [Gatherer] will keep taking elements only from the other
	/// [Stream].
	///
	/// @param <T>      The type of elements in the stream.
	/// @param iterable the iterable to interleave into the current stream.
	/// @return a gatherer that combines interleaves elements from an iterable into the current stream.
	public static <T> Gatherer<T, ?, T> interleave(Iterable<T> iterable) {
		return new InterleavingGatherer<>(iterable::iterator);
	}

	/// Returns a [Gatherer] that will filter out all elements in a stream that are not instances of a given [Class].
	///
	/// @param clazz the type of elements to retain.
	/// @param <T>   the type of elements in the [Stream].
	/// @param <R>   the type of the elements that will be retained.
	/// @return a [Gatherer] that will filter out all elements that aren't instances of `clazz`.
	public static <T, R extends T> Gatherer<T, ?, R> isInstanceOf(Class<R> clazz) {
		return Gatherer.of((_, element, downstream) -> {
			if (clazz.isInstance(element)) {
				return downstream.push(clazz.cast(element));
			}

			return true;
		});
	}

	/// Returns a [Gatherer] that groups elements of a [Stream] into a list while a predicate returns `true`.
	///
	/// The `predicate` is a [BiFunction] that receives all the previous elements in the current group and the current
	/// element. If the `predicate` function returns `true`, the current element will be added to the current group. If
	/// the `predicate` function returns `false` instead, the current element will instead be the first element of a
	/// new group.
	///
	/// @param predicate the predicate used to determine the groups.
	/// @param <T>       the type of elements in the [Stream].
	/// @return a gatherer that groups elements based on a predicate.
	public static <T> Gatherer<T, ?, List<T>> groupWhile(BiPredicate<List<T>, T> predicate) {
		return Gatherer.ofSequential(() -> new ArrayList<T>(), (previousElementsInGroup, element, downstream) -> {
			if (previousElementsInGroup.isEmpty() || predicate.test(previousElementsInGroup, element)) {
				previousElementsInGroup.add(element);
				return true;
			}

			List<T> previousGroup = List.copyOf(previousElementsInGroup);

			previousElementsInGroup.clear();
			previousElementsInGroup.add(element);

			return downstream.push(previousGroup);
		}, (lastGroup, downstream) -> downstream.push(List.copyOf(lastGroup)));
	}

	private Gatherers() {
	}
}
