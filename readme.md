This is a [Vert.x][vertx] busmod providing a [Fongo][fongo] database. The primary intention is for replacing a real
Mongo persistor busmod on the event bus in Java or Groovy unit tests. However, it is also possible to deploy as in a
Vert.x container just like any other busmod.

## Installing

- Clone this repository.
- Ensure that you have the environment variable `VERTX_MODS` set to the directory where you want mods to be installed.
- Use `./gradlew install` to install the busmod.

## Using embedded

You can use the Fongo Persistor as an event bus handler when using embedded vert.x in your tests (see [Testing With
Embedded Vert.x][blog]).

### JUnit

	private Vertx vertx = Vertx.newVertx();
	FongoPersistor persistor = new FongoPersistor();

	@Before void startFongo() {
		persistor.setVertx(vertx);
		persistor.start();
	}

	@After void stopFongo() {
		persistor.stop();
	}

### Spock

	@Shared Vertx vertx = Vertx.newVertx()
	def persistor = new FongoPersistor()

	void setup() {
		persistor.vertx = vertx.toJavaVertx()
		persistor.start()
	}

	void cleanup() {
		persistor.stop()
	}

## Using as a busmod

The Fongo Persistor needs to be installed in `VERTX_MODS` then you can simply register it as you would any other busmod
using `deployVerticle('fongo-persistor')`

## Example app

There is an example app in `examples/webapp`. This is simply the standard Vert.x webapp example but using a _fongo-persistor_
instead of a _mongo-persistor_.

To run the example ensure that you have the `VERTX_MODS` environment variable set, then:

	./gradlew install
	cd examples/web-app
	vertx run App.groovy

then point your browser at `https://localhost:8080/index.html`

[vertx]:http://vertx.io
[fongo]:https://github.com/foursquare/fongo
[blog]:http://blog.freeside.co/blog/2012/05/09/testing-with-embedded-vertx/
