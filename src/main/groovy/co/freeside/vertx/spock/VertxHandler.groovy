package co.freeside.vertx.spock

import org.spockframework.runtime.extension.ExtensionAnnotation
import java.lang.annotation.*

/**
 * Add this annotation to any specification fields that should be registered as Vert.x event bus handlers during test
 * execution. The field will be registered on the event bus before each feature method and unregistered afterwards. If
 * the field is `@Shared` it will be registered and unregistered at the start and end of the spec.
 *
 * Any spec using this annotation _must_ declare a `@Shared Vertx` property.
 *
 * For example:
 *
 *     @Shared Vertx vertx = Vertx.newVertx()
 *     @VertxHandler(address: 'my.handler', method: 'foo') def handler = new MyHandler()
 *     @VertxHandler(address: 'mock.handler') def mockHandler = Mock(Handler)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@ExtensionAnnotation(VertxHandlerExtension)
public @interface VertxHandler {

	/**
	 * The event bus address that the handler should listen on.
	 */
	String address()

	/**
	 * The name of the handler method. The method must take a single `Message` parameter.
	 */
	String method() default 'handle'
}