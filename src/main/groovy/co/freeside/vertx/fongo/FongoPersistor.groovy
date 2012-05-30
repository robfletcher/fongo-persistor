package co.freeside.vertx.fongo

import com.foursquare.fongo.Fongo
import com.mongodb.DB
import org.vertx.java.busmods.BusModBase
import org.vertx.java.core.Handler
import org.vertx.java.core.eventbus.Message
import org.vertx.java.core.json.JsonObject
import org.vertx.mods.MongoPersistor
import org.vertx.java.core.logging.Logger
import org.vertx.java.core.logging.impl.JULLogDelegate

class FongoPersistor extends BusModBase implements Handler<Message<JsonObject>> {

	private String address
	private String dbName

	private Fongo fongo
	private DB db
	private MongoPersistor delegate = new MongoPersistor()

	FongoPersistor() {
		fongo = new Fongo('fongo server', true)
		db = fongo.getDB(dbName)

//		delegate.@mongo = fongo.mongo
		delegate.@db = db

		logger = new Logger(new JULLogDelegate(FongoPersistor.name))
	}

	@Override
	void start() {
		super.start()

		address = getOptionalStringConfig("address", "vertx.mongopersistor");
		dbName = getOptionalStringConfig("db_name", "default_db");
	}

	@Override
	void handle(Message<JsonObject> message) {
		delegate.handle(message)
	}

	void handle(org.vertx.groovy.core.eventbus.Message message) {
		delegate.handle(message.@jMessage)
	}

	String getAddress() {
		address
	}

	DB getDB() {
		db
	}
}
