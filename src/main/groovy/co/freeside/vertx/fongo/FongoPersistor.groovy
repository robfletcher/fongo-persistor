package co.freeside.vertx.fongo

import com.foursquare.fongo.Fongo
import com.mongodb.DB
import org.vertx.java.busmods.BusModBase
import org.vertx.java.core.eventbus.Message
import org.vertx.java.core.json.JsonObject
import org.vertx.java.core.logging.Logger
import org.vertx.java.core.logging.impl.JULLogDelegate
import org.vertx.mods.MongoPersistor
import org.vertx.java.core.*

class FongoPersistor extends BusModBase implements Handler<Message<JsonObject>> {

	boolean debug = true
	String address = 'vertx.mongopersistor'
	String dbName = 'default_db'

	private Fongo fongo
	private DB db
	private MongoPersistor delegate = new MongoPersistor()
	private String eventBusId

	@Override
	void start() {
		if (container) {
			super.start()
			address = getOptionalStringConfig('address', 'vertx.mongopersistor');
			dbName = getOptionalStringConfig('db_name', 'default_db');
		} else {
			// we are outside the container, e.g. embedded
			eb = vertx.eventBus()
			logger = new Logger(new JULLogDelegate(FongoPersistor.name))
		}

		fongo = new Fongo('fongo server', debug)
		db = fongo.getDB(dbName)

		// hack the private field on the delegate to inject the fongo db instead of a real mongo one
		delegate.@db = db

		eventBusId = eb.registerHandler(address, this)
	}

	@Override
	void stop() {
		eb.unregisterHandler(eventBusId)
	}

	@Override
	void handle(Message<JsonObject> message) {
		delegate.handle(message)
	}

	/**
	 * Overloads the handle method so this busmod can directly handle Groovy messages.
	 */
	void handle(org.vertx.groovy.core.eventbus.Message message) {
		delegate.handle(message.@jMessage)
	}

	/**
	 * To allow tests access to the underlying database.
	 */
	DB getDb() {
		db
	}
}
