package eu.jbeernink.util.stream.gatherer;

import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

///  [Gatherer] that interleaves elements from an [Iterator] into a [java.util.stream.Stream].
///
/// @param <T> The type of the elements in the stream.
public final class InterleavingGatherer<T> implements Gatherer<T, Iterator<? extends T>, T> {

	private final Supplier<Iterator<? extends T>> iteratorSupplier;

	public InterleavingGatherer(Supplier<Iterator<? extends T>> iteratorSupplier) {
		this.iteratorSupplier = iteratorSupplier;
	}

	@Override
	public Supplier<Iterator<? extends T>> initializer() {
		return iteratorSupplier;
	}

	@Override
	public Integrator<Iterator<? extends T>, T, T> integrator() {
		return (iterator, element, downstream) -> {
			boolean acceptingMoreElements = downstream.push(element);

			if (acceptingMoreElements && iterator.hasNext()) {
				acceptingMoreElements = downstream.push(iterator.next());
			}

			return acceptingMoreElements;
		};
	}

	@Override
	public BiConsumer<Iterator<? extends T>, Downstream<? super T>> finisher() {
		return (iterator, downstream) -> {
			while (!downstream.isRejecting() && iterator.hasNext()) {
				downstream.push(iterator.next());
			}
		};
	}


}
