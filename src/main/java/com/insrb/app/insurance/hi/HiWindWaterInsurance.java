package com.insrb.app.insurance.hi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import lombok.extern.slf4j.Slf4j;

//ref : http://kong.github.io/unirest-java/#requests

@Slf4j
@Component
public class HiWindWaterInsurance {

    @Value("${hi.waterwind.ww.token.url}")
    private String wwTokenUrl;

    @Value("${hi.waterwind.ww.premium.url}")
    private String premiumUrl;

    public String getWWToken2() {
        log.info("wwTokenUrl:" + wwTokenUrl);
        HttpResponse<JsonNode> req = Unirest.post(wwTokenUrl).basicAuth("29311b91", "75e365273b4f1d81ea944b1c9a9560ad")
                .header("Origin", "https://insrb.com").header("Content-Type", "application/x-www-form-urlencoded")
                .field("grant_type", "client_credentials").field("client_id", "6a08aba5")
                .field("client_secret", "be085145cd0ab2aa19f668ee777bff9a").field("scope", "web-origins").asJson()
                .ifSuccess((res) -> {
                    // if(res.getStatus() != HttpStatus.OK) throw new NoHandlerFoundException();
                })
                .ifFailure((res) -> {
                    log.error("Oh No! Status" + res.getStatus());
                    res.getParsingError().ifPresent(e -> {
                        log.error("Parsing Exception: ", e);
                        log.error("Original body: " + e.getOriginalBody());
                    });
                });
        JSONObject json = req.getBody().getObject();
        return json.getString("access_token");
    }

    public String getWWToken() {
        log.info("wwTokenUrl:" + wwTokenUrl);
        return Unirest.post(wwTokenUrl).basicAuth("29311b91", "75e365273b4f1d81ea944b1c9a9560ad")
                .header("Origin", "https://insrb.com").header("Content-Type", "application/x-www-form-urlencoded")
                .field("grant_type", "client_credentials").field("client_id", "6a08aba5")
                .field("client_secret", "be085145cd0ab2aa19f668ee777bff9a").field("scope", "web-origins").asJson()
                .getBody().getObject().getString("access_token");
    }

    public String getPrePremium(String data) {
        String wwToken = getWWToken();
        log.info("wwToken:" + wwToken);
        log.info("addressJson:" + data);
        String auth = "Bearer " + wwToken;
        return Unirest.post(premiumUrl).header("X-Channel-Id", "Main").header("X-Client-Id", "https://insrb.com")
                .header("X-Menu-Id", "home").header("X-User-Id", "4GE300").header("Authorization", auth)
                .header("Content-Type", "application/json").body(data).asJson().getBody().getObject()
                .getString("resultCode");
    }

}
