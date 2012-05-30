package co.freeside.vertx.spock

import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension
import org.spockframework.runtime.model.*

class VertxHandlerExtension extends AbstractAnnotationDrivenExtension<VertxHandler> {

	private final List<FieldInfo> fields = []

	@Override
	void visitFieldAnnotation(VertxHandler annotation, FieldInfo field) {
		fields << field
	}

	@Override
	void visitSpec(SpecInfo spec) {
		VertxHandlerInterceptor.install(spec, fields)
	}
}
