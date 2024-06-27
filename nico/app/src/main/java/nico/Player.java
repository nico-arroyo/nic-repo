package nico;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.UUID;

public class Player {

    private final UUID id;
    private final String name;
    private final String lastName;
    private final Position position;
    private final List<String> teams;
    private final String nationality;
    private final PreferredLeg preferredLeg;
    private final Status status;
    private final UUID teamId;

    private Player(Builder builder) {
        id = builder.id;
        name = builder.name;
        lastName = builder.lastName;
        position = builder.position;
        teams = builder.teams;
        nationality = builder.nationality;
        preferredLeg = builder.preferredLeg;
        status = builder.status;
        teamId = builder.teamId;
    }

    public static Player create(JsonObject data) {
        return Builder.builder()
                .withId(UUID.randomUUID())
                .withName(data.getString("name"))
                .withLastName(data.getString("last_name"))
                .withPosition(Position.valueOf(data.getString("position")))
                .withTeams(data.getJsonArray("clubs").getList())
                .withNationality(data.getString("nationality"))
                .withPreferredLeg(PreferredLeg.valueOf(data.getString("preferred_leg")))
                .withStatus(Status.valueOf(data.getString("status")))
                .withTeamId(UUID.fromString(data.getString("teamId")))
                .build();
    }

    public JsonObject toJson() {
        return new JsonObject()
                .put("id", id.toString())
                .put("name", name)
                .put("last_name", lastName)
                .put("position", position)
                .put("clubs", new JsonArray(teams))
                .put("nationality", nationality)
                .put("preferred_leg", preferredLeg)
                .put("status", status)
                .put("teamId", teamId);
    }

    public static final class Builder {
        private UUID id;
        private String name;
        private String lastName;
        private Position position;
        private List<String> teams;
        private String nationality;
        private PreferredLeg preferredLeg;
        private Status status;
        private UUID teamId;

        private Builder() {}

        public static Builder builder() {
            return new Builder();
        }

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder withPosition(Position position) {
            this.position = position;
            return this;
        }

        public Builder withTeams(List<String> teams) {
            this.teams = teams;
            return this;
        }

        public Builder withNationality(String nationality) {
            this.nationality = nationality;
            return this;
        }

        public Builder withPreferredLeg(PreferredLeg preferredLeg) {
            this.preferredLeg = preferredLeg;
            return this;
        }

        public Builder withStatus(Status status) {
            this.status = status;
            return this;
        }

        public Builder withTeamId(UUID teamId) {
            this.teamId = teamId;
            return this;
        }

        public Player build() {
            return new Player(this);
        }
    }
}
