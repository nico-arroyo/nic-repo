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

public class MainVerticle extends AbstractVerticle {

    private MongoClient client;

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle());
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
        router.put("/players/:playerId").handler(this::handlePutPlayer);
        //router.delete("players/:id").handler(this::handleDeletePlayer);
        
        router.get("/teams/:id").handler(this::handleFindTeam);
        router.get("/teams").handler(this::handleFindTeams);
        router.post("/teams").handler(this::handleAddTeam);
        router.put("/teams/:teamId").handler(this::handlePutTeam);
        //router.delete("teams/:id").handler(this::handleDeleteTeam);


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
                                    .setStatusCode(201)
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

    private void handlePutPlayer(RoutingContext routingContext) {
        var playerId = routingContext.pathParam("playerId");
        var updatedPlayerJson = routingContext.getBodyAsJson();

        client.findOne("players", new JsonObject().put("id", playerId), null)
                .subscribe(player -> {
                    if (player != null) {
                        player.mergeIn(updatedPlayerJson);

                        client.replaceDocuments("players", new JsonObject().put("id", playerId), player)
                                .subscribe(
                                        res -> {
                                            routingContext.response()
                                                    .setStatusCode(200)
                                                    .putHeader("Content-type", "application/json")
                                                    .end(new JsonObject().put("message", "Player updated successfully").encodePrettily());
                                        },
                                        error -> {
                                            error.printStackTrace();
                                            routingContext.response()
                                                    .setStatusCode(500)
                                                    .end(new JsonObject().put("error", error.getMessage()).encodePrettily());
                                        }
                                );
                    } else {
                        routingContext.response()
                                .setStatusCode(404)
                                .end(new JsonObject().put("error", "Player not found").encodePrettily());
                    }
                }, error -> {
                    error.printStackTrace();
                    routingContext.response()
                            .setStatusCode(500)
                            .end(new JsonObject().put("error", error.getMessage()).encodePrettily());
                });
    }


    private void handleDeletePlayer(RoutingContext routingContext) {
        var id = routingContext.pathParam("id");

        client.removeDocument("players", new JsonObject().put("id", id))
                .subscribe(
                        res -> {
                            System.out.println(res);
                            routingContext.response().setStatusCode(201).end();
                        },
                        error -> {
                            error.getCause().printStackTrace();
                            routingContext.response().setStatusCode(500).end();
                        }
                );
    }
    
    private void handleFindTeam(RoutingContext routingContext) {
        var id = routingContext.pathParam("id");
        
        client.findOne("teams", new JsonObject().put("id", id), null)
                .subscribe(
                        team -> {
                            if (team != null) {
                                routingContext.response()
                                        .setStatusCode(200)
                                        .putHeader("Content-type", "application/json")
                                        .end(team.encodePrettily());
                            } else {
                                routingContext.response()
                                        .setStatusCode(404)
                                        .end(new JsonObject().put("error", "Team not found").encodePrettily());
                            }
                        },
                        error -> {
                            routingContext.response()
                                    .setStatusCode(500)
                                    .end(new JsonObject().put("error", error.getMessage()).encodePrettily());
                        });
    }

    private void handleFindTeams(RoutingContext routingContext) {
        JsonObject query = new JsonObject();

        List<String> params = Arrays.asList("name", "city", "country", "stadium", "founded");
        for (String param : params) {
            String value = routingContext.queryParam(param).stream().findFirst().orElse(null);
            if (value != null) {
                query.put(param, value);
            }
        }

        client.find("teams", query)
                .subscribe(
                        teams -> {
                            JsonArray jsonArray = new JsonArray(teams);
                            routingContext.response()
                                    .setStatusCode(201)
                                    .putHeader("Content-Type", "application/json")
                                    .end(jsonArray.encodePrettily());
                        },
                        error -> {
                            routingContext.response()
                                    .setStatusCode(500)
                                    .end(new JsonObject().put("error", error.getMessage()).encodePrettily());
                        });
    }
    
    private void handleAddTeam(RoutingContext routingContext) {
        var body = routingContext.body().asJsonObject();
        var team = Team.create(body);
        client
                .rxSave("teams", team.toJson())
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

    private void handlePutTeam(RoutingContext routingContext) {
        var teamId = routingContext.pathParam("teamId");
        var updatedTeamJson = routingContext.getBodyAsJson();

        client.findOne("teams", new JsonObject().put("id", teamId), null)
                .subscribe(team -> {
                    if (team != null) {
                        team.mergeIn(updatedTeamJson);

                        client.replaceDocuments("teams", new JsonObject().put("id", teamId), team)
                                .subscribe(
                                        res -> {
                                            routingContext.response()
                                                    .setStatusCode(200)
                                                    .putHeader("Content-type", "application/json")
                                                    .end(new JsonObject().put("message", "Team updated successfully").encodePrettily());
                                        },
                                        error -> {
                                            error.printStackTrace();
                                            routingContext.response()
                                                    .setStatusCode(500)
                                                    .end(new JsonObject().put("error", error.getMessage()).encodePrettily());
                                        }
                                );
                    } else {
                        routingContext.response()
                                .setStatusCode(404)
                                .end(new JsonObject().put("error", "Team not found").encodePrettily());
                    }
                }, error -> {
                    error.printStackTrace();
                    routingContext.response()
                            .setStatusCode(500)
                            .end(new JsonObject().put("error", error.getMessage()).encodePrettily());
                });
    }


    private void handleDeleteTeam(RoutingContext routingContext) {
        var id = routingContext.pathParam("id");

        client.removeDocument("teams", new JsonObject().put("id", id))
                .subscribe(
                        res -> {
                            System.out.println(res);
                            routingContext.response().setStatusCode(201).end();
                        },
                        error -> {
                            error.getCause().printStackTrace();
                            routingContext.response().setStatusCode(500).end();
                        }
                );
    }

}
