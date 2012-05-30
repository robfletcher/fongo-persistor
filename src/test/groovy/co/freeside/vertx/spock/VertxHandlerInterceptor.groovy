package co.freeside.vertx.spock

import org.vertx.groovy.core.Vertx
import java.util.concurrent.CountDownLatch
import org.spockframework.runtime.extension.*
import org.spockframework.runtime.model.*

import static java.util.concurrent.TimeUnit.SECONDS

abstract class VertxHandlerInterceptor extends AbstractMethodInterceptor {

	static void install(SpecInfo spec, List<FieldInfo> fields) {
		installForInstanceHandlers spec, fields.findAll { !it.shared }
		installForSharedHandlers spec, fields.findAll { it.shared }
	}

	private final List<FieldInfo> fields
	private final Stack<String> handlerIds = new Stack<String>()
	private Vertx vertx

	protected VertxHandlerInterceptor(List<FieldInfo> fields) {
		this.fields = fields
	}

	@Override
	void interceptSpecExecution(IMethodInvocation invocation) {
		def vertxField = invocation.spec.fields.find { it.type == Vertx }
		if (!vertxField || !vertxField.shared) throw new ExtensionException("Your spec must declare a @Shared $Vertx.name property")
		vertx = vertxField.readValue(invocation.sharedInstance)

		invocation.proceed()
	}

	protected void setupHandlers(IMethodInvocation invocation) {
		def readyLatch = new CountDownLatch(fields.size())
		for (field in fields) {
			def annotation = field.getAnnotation(VertxHandler)
			def value = field.readValue(invocation.target)
			if (value != null) {
				def address = annotation.address()
				def methodName = annotation.method()
				handlerIds << vertx.eventBus.registerHandler(address, value.&"$methodName") {
					readyLatch.countDown()
				}
			}
		}
		if (!readyLatch.await(1, SECONDS)) {
			throw new ExtensionException("Timed out waiting for event bus handlers to be registered")
		}
	}

	protected void cleanupHandlers() {
		def latch = new CountDownLatch(handlerIds.size())
		while (!handlerIds.empty()) {
			vertx.eventBus.unregisterSimpleHandler(handlerIds.pop()) {
				latch.countDown()
			}
		}
		if (!latch.await(1, SECONDS)) {
			throw new ExtensionException("Timed out waiting for event bus handlers to be unregistered")
		}
	}

	private static void installForSharedHandlers(SpecInfo spec, List<FieldInfo> fields) {
		if (fields) {
			def sharedInterceptor = new VertxHandlerInterceptor(fields) {
				@Override
				void interceptSetupSpecMethod(IMethodInvocation invocation) {
					invocation.proceed()
					setupHandlers(invocation)
				}

				@Override
				void interceptCleanupSpecMethod(IMethodInvocation invocation) {
					cleanupHandlers()
					invocation.proceed()
				}
			}
			spec.addInterceptor(sharedInterceptor)
			spec.setupSpecMethod.addInterceptor(sharedInterceptor)
			spec.cleanupSpecMethod.addInterceptor(sharedInterceptor)
		}
	}

	private static void installForInstanceHandlers(SpecInfo spec, List<FieldInfo> fields) {
		if (fields) {
			def interceptor = new VertxHandlerInterceptor(fields) {
				@Override
				void interceptSetupMethod(IMethodInvocation invocation) {
					invocation.proceed()
					setupHandlers(invocation)
				}

				@Override
				void interceptCleanupMethod(IMethodInvocation invocation) {
					cleanupHandlers()
					invocation.proceed()
				}
			}
			spec.addInterceptor(interceptor)
			spec.setupMethod.addInterceptor(interceptor)
			spec.cleanupMethod.addInterceptor(interceptor)
		}
	}

}
