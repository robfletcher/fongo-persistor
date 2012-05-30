package co.freeside.vertx.fongo

import co.freeside.vertx.spock.VertxHandler
import org.vertx.groovy.core.Vertx
import org.vertx.groovy.core.eventbus.Message
import spock.util.concurrent.BlockingVariable
import com.mongodb.*
import spock.lang.*

class FongoPersistorSpec extends Specification {

	@Shared Vertx vertx = Vertx.newVertx()
	@VertxHandler(address = 'mongo.persistor') def persistor = new FongoPersistor()
	DB db = persistor.getDB()

	void 'can save to a fongo database'() {
		given:
		def reply = new BlockingVariable<Message>()

		when:
		def document = [name: 'Edward Teach', nomDeGuerre: 'Blackbeard', vessel: 'Queen Anne\'s Revenge']
		vertx.eventBus.send 'mongo.persistor', [action: 'save', collection: 'pirates', document: document], reply.&set

		then:
		reply.get().body.status == 'ok'

		and:
		def collection = db.getCollection('pirates')
		collection.count() == 1
		def row = collection.findOne()
		row.get('name') == document.name
		row.get('nomDeGuerre') == document.nomDeGuerre
		row.get('vessel') == document.vessel
	}

	void 'can find in a fongo database'() {
		given:
		def document = new BasicDBObject(name: 'Edward Teach', nomDeGuerre: 'Blackbeard', vessel: 'Queen Anne\'s Revenge')
		db.getCollection('pirates').insert(document)

		and:
		def reply = new BlockingVariable<Message>()

		when:
		vertx.eventBus.send 'mongo.persistor', [action: 'findone', collection: 'pirates', match: [nomDeGuerre: 'Blackbeard']], reply.&set

		then:
		reply.get().body.status == 'ok'
		reply.get().body.result.name == document.name
	}

}
