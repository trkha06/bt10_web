package vn.hcmute.webpr.jwt.model;

public record LoginResponse(String token, long expiresIn) { }
