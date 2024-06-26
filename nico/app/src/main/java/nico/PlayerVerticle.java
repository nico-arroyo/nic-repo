package nico;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.ext.mongo.MongoClient;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.rxjava3.ext.web.RoutingContext;
import io.vertx.rxjava3.ext.web.handler.BodyHandler;

import java.util.Arrays;
import java.util.List;

public class PlayerVerticle extends AbstractVerticle {

    private MongoClient client;

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new PlayerVerticle());
    }

    @Override
    public void start() {
        JsonObject config =
                new JsonObject().put("url", "mongodb://127.0.0.1:27017").put("db_name", "football");
        this.client = MongoClient.create(vertx, config);

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.get("/players/:id").handler(this::handleFindPlayer);
        router.get("/players").handler(this::handleFindPlayers);
        router.post("/players").handler(this::handleAddPlayer);

        vertx
                .createHttpServer()
                .requestHandler(router)
                .rxListen(8080)
                .subscribe(
                        result -> System.out.println("Server started on port 8080"),
                        error -> System.out.println("Failed to start server: " + error.getCause()));
    }

    private void handleFindPlayer(RoutingContext context) {
        var id = context.pathParam("id");

        client.findOne("players", new JsonObject().put("id", id), null)
                .subscribe(
                        player -> {
                            if (player != null) {
                                context.response()
                                        .setStatusCode(200)
                                        .putHeader("Content-type", "application/json")
                                        .end(player.encodePrettily());
                            } else {
                                context.response()
                                        .setStatusCode(404)
                                        .end(new JsonObject().put("error", "Player not found").encodePrettily());
                            }
                        },
                        error -> {
                            context.response()
                                    .setStatusCode(500)
                                    .end(new JsonObject().put("error", error.getMessage()).encodePrettily());
                        });
    }

    private void handleFindPlayers(RoutingContext routingContext) {
        JsonObject query = new JsonObject();

        List<String> params = Arrays.asList("name", "lastName", "position", "nationality", "preferredLeg", "status");
        for (String param : params) {
            String value = routingContext.queryParam(param).stream().findFirst().orElse(null);
            if (value != null) {
                query.put(param, value);
            }
        }

        client.find("players", query)
                .subscribe(
                        players -> {
                            JsonArray jsonArray = new JsonArray(players);
                            routingContext.response()
                                    .setStatusCode(200)
                                    .putHeader("Content-Type", "application/json")
                                    .end(jsonArray.encodePrettily());
                        },
                        error -> {
                            routingContext.response()
                                    .setStatusCode(500)
                                    .end(new JsonObject().put("error", error.getMessage()).encodePrettily());
                        });
}

    private void handleAddPlayer(RoutingContext routingContext) {
        var body = routingContext.body().asJsonObject();
        var player = Player.create(body);
        client
                .rxSave("players", player.toJson())
                .subscribe(
                        res -> {
                            System.out.println(res);
                            routingContext.response().setStatusCode(201).end();
                        },
                        error -> {
                            error.getCause().printStackTrace();
                            routingContext.response().setStatusCode(500).end();
                        },
                        () -> System.out.println("Empty"));
    }
}
