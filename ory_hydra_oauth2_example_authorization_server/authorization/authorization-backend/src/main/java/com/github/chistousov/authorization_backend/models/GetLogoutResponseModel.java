package com.github.chistousov.authorization_backend.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetLogoutResponseModel {
    private String challenge;

    private Client client;

    @JsonProperty("oidc_context")
    private Object oidcContext;

    @JsonProperty("request_url")
    private String requestUrl;

    @JsonProperty("rp_initiated")
    private Boolean rpInitiated;

    @JsonProperty("sid")
    private String sid;
    
    private String subject;
}