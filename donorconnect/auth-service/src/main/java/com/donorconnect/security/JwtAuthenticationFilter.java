package com.donorconnect.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component // automatically creates bean means object
@RequiredArgsConstructor
// OncePerRequestFilter -> in one request any filter should not run twice
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    // raw data coming from the user(request) and the data sent back to user(response)
    // filter chain -> it allows you to pass the request to next filter chain

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // this method is called down what it is doing is it is checking for the  authorization header and removes the word bearer and space from jwt using that substring(7)
        // and get the raw string
        String token = getTokenFromRequest(request);

        // it verifies whether the token is valid or not
        if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
            // if token is valid, username (email) will be extracted from tokens payload
            String username = tokenProvider.getUsernameFromToken(token);

            // it is calling this loadByUsername thing to get latest user details (roles,status)
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // this is the security context holder which is populated with user info that is name, id email, role
            // password is null as already proved user's identity
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            // extra info like users ip address so that as to hacker your logs will show mismatch
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        // this tells that i am done with checking this user let it pass to next filter
        // in the chain and if there is no next filter then it will go to the controller
        filterChain.doFilter(request, response);
    }


    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}