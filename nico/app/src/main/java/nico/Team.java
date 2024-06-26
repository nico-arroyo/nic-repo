package nico;

import io.vertx.core.json.JsonObject;
import java.util.UUID;

public class Team {

    private final UUID id;
    private final String name;
    private final String city;
    private final String country;
    private final String stadium;
    private final int founded;

    private Team(Builder builder) {
        id = builder.id;
        name = builder.name;
        city = builder.city;
        country = builder.country;
        stadium = builder.stadium;
        founded = builder.founded;
    }

    public static Team create(JsonObject data) {
        return Builder.builder()
                .withId(UUID.randomUUID())
                .withName(data.getString("name"))
                .withCity(data.getString("city"))
                .withCountry(data.getString("country"))
                .withStadium(data.getString("stadium"))
                .withFounded(data.getInteger("founded"))
                .build();
    }

    public JsonObject toJson() {
        return new JsonObject()
                .put("id", id.toString())
                .put("name", name)
                .put("city", city)
                .put("country", country)
                .put("stadium", stadium)
                .put("founded", founded);
    }

    public static final class Builder {
        private UUID id;
        private String name;
        private String city;
        private String country;
        private String stadium;
        private int founded;

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

        public Builder withCity(String city) {
            this.city = city;
            return this;
        }

        public Builder withCountry(String country) {
            this.country = country;
            return this;
        }

        public Builder withStadium(String stadium) {
            this.stadium = stadium;
            return this;
        }

        public Builder withFounded(int founded) {
            this.founded = founded;
            return this;
        }

        public Team build() {
            return new Team(this);
        }
    }
}
