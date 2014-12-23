package clump

import com.twitter.util.Future

class ClumpSource[T, U](val fetch: Set[T] => Future[Map[T, U]], val maxBatchSize: Int) {

  def this(fetch: Set[T] => Future[Iterable[U]], keyFn: U => T, maxBatchSize: Int) = {
    this(fetch.andThen(_.map(_.map(v => (keyFn(v), v)).toMap)), maxBatchSize)
  }

  def list(inputs: T*): Clump[List[U]] =
    list(inputs.toList)

  def list(inputs: List[T]): Clump[List[U]] =
    Clump.collect(inputs.map(get))

  def get(input: T): Clump[U] = {
    val fetcher = ClumpContext().fetcherFor(this)
    fetcher.append(input)
    new ClumpFetch(input, fetcher)
  }
}
