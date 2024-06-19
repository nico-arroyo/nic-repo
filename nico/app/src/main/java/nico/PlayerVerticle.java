package nico;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerVerticle extends AbstractVerticle {
    private List<Player> players = new ArrayList<>();

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new PlayerVerticle());
    }

    @Override
    public void start() {
        players.add(new Player("Isco", "Alarcon", Position.CENTER_MIDFIELD, "Betis", new ArrayList<>(Arrays.asList("Malaga", "Real Madrid", "Sevilla")), PreferredLeg.AMBIDEXTROUS, Status.ACTIVE));

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.get("/player/:filter").handler(this::handleFindPlayer);
        router.get("/player/:filter/:value").handler(this::handleFindPlayer);

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080, result -> {
                    if (result.succeeded()) {
                        System.out.println("Server started on port 8080");
                    } else {
                        System.out.println("Failed to start server: " + result.cause());
                    }
                });
    }

    private void handleFindPlayer(RoutingContext context) {
        String filter = context.pathParam("filter");
        String value = context.pathParam("value");

        if (value == null || value.isEmpty()) {
            context.response()
                    .setStatusCode(400)
                    .end(String.format("{'error':'Value for filter %s is required'}", filter));
            return;
        }

        List<JsonObject> filteredPlayers = players.stream()
                .filter(player -> {
                    switch (filter.toLowerCase()) {
                        case "name":
                            return player.getName().equalsIgnoreCase(value);
                        case "lastname":
                            return player.getLastName().equalsIgnoreCase(value);
                        case "position":
                            return player.getPosition().toString().equalsIgnoreCase(value);
                        case "currentteam":
                            return player.getCurrentTeam().equalsIgnoreCase(value);
                        case "formerteam":
                            return player.getFormerTeams().stream().anyMatch(team -> team.equalsIgnoreCase(value));
                        case "preferredleg":
                            return player.getPreferredLeg().toString().equalsIgnoreCase(value);
                        case "status":
                            return player.getStatus().toString().equalsIgnoreCase(value);
                        default:
                            return false;
                    }
                })
                .map(Player::toJson)
                .toList();

        if (filteredPlayers.isEmpty()) {
            context.response()
                    .setStatusCode(404)
                    .end(String.format("{'result':'Player(s) with %s %s not found'}", filter, value));
        } else {
            JsonArray jsonResult = new JsonArray(filteredPlayers);
            context.response()
                    .putHeader("content-type", "application/json")
                    .end(jsonResult.encodePrettily());
        }
    }
}
