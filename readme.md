This is a [Vert.x][vertx] busmod providing a [Fongo][fongo] database. The primary intention is for replacing a real
Mongo persistor busmod on the event bus in unit tests.

See the tests for examples of usage.

[vertx]:http://vertx.io
[fongo]:https://github.com/foursquare/fongo

## To-do

- make the busmod deployable (currently only works embedded)
- set up example app that uses fongo in place of mongo