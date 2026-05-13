package apiGateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
//    HMAC is an algo that converts your secret string into mathematical secret key
//    object that is used by library to verify signature

    public void validateToken(String token) {
        Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractUserId(String token) {
        return extractAllClaims(token).get("userId", String.class);
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                // verifies the signature one last time and decrypts the middle part of jwt
                .getPayload();
        // this returns the claims object that is map containing all the user data stored in token
    }
}


// While the validateToken method only checks if the token is "legit" (not fake),
// extractAllClaims actually opens the envelope to read the data inside.

//Detailed Breakdown of extractAllClaims
//private Claims extractAllClaims(String token) {
//    return Jwts.parser()
//            .verifyWith(getSigningKey()) // Step 1: Check the wax seal (Signature)
//            .build()
//            .parseSignedClaims(token)    // Step 2: Open the envelope
//            .getPayload();               // Step 3: Read the letter (Claims)
//}


//1. What are "Claims"?
//In JWT terms, a Claim is just a piece of information about the user. When you log in, your auth-service "claims" that:
//Your name is X.
//Your ID is Y.
//Your Role is Z.
//These are all packed into the middle part of the JWT (the payload).


//2. What does this method do?
//Decryption/Extraction: It takes that long, encrypted string and turns it back into a Map-like object (the Claims object).
//Verification: It uses your secret key one last time to make sure that the data inside hasn't been modified. If someone tried to change their role from "USER" to "ADMIN" inside the token, this method would detect the signature mismatch and crash.
//The "Payload": The .getPayload() call specifically returns the body of the JWT where all your data (userId, email, role) is stored.


//3. Why is it private?
//This is a helper method. You don't want other classes to worry about the complex logic of parsing. Instead, you provide "Public" methods like extractEmail or extractRole which call this private method internally to get the data they need.
//The "Unpacking" Flow
//When your Gateway needs to know who is making a request, the flow goes like this:
//Request hits Gateway with a token.
//extractEmail(token) is called.
//Inside that, extractAllClaims(token) runs first. It decodes the whole token into a big list of info.
//Then .getSubject() is called on that list to pick out just the email.