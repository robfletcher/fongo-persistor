package co.freeside.vertx.fongo

import com.mongodb.BasicDBObject
import org.vertx.groovy.core.Vertx
import org.vertx.groovy.core.eventbus.Message
import spock.util.concurrent.BlockingVariable
import spock.lang.*

/**
 * This spec demonstrates using _fongo-persistor_ as a collaborator when testing your own event bus components.
 */
class FongoPersistorSpec extends Specification {

	@Shared Vertx vertx = Vertx.newVertx()
	def persistor = new FongoPersistor()

	void setup() {
		persistor.vertx = vertx.toJavaVertx()
		persistor.start()
	}

	void 'can save to a fongo database'() {
		given:
		def reply = new BlockingVariable<Message>()

		when:
		def document = [name: 'Edward Teach', nomDeGuerre: 'Blackbeard', vessel: 'Queen Anne\'s Revenge']
		vertx.eventBus.send persistor.address, [action: 'save', collection: 'pirates', document: document], reply.&set

		then:
		reply.get().body.status == 'ok'

		and:
		def collection = persistor.db.getCollection('pirates')
		collection.count() == 1
		def row = collection.findOne()
		row.get('name') == document.name
		row.get('nomDeGuerre') == document.nomDeGuerre
		row.get('vessel') == document.vessel
	}

	void 'can find in a fongo database'() {
		given:
		def document = new BasicDBObject(name: 'Edward Teach', nomDeGuerre: 'Blackbeard', vessel: 'Queen Anne\'s Revenge')
		persistor.db.getCollection('pirates').insert(document)

		and:
		def reply = new BlockingVariable<Message>()

		when:
		vertx.eventBus.send persistor.address, [action: 'findone', collection: 'pirates', match: [nomDeGuerre: 'Blackbeard']], reply.&set

		then:
		def msg = reply.get()
		msg.body.status == 'ok'
		msg.body.result.name == document.name
	}

}
