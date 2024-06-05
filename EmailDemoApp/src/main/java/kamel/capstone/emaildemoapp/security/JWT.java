package kamel.capstone.emaildemoapp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import kamel.capstone.emaildemoapp.model.User;
import org.springframework.stereotype.Component;

import javax.naming.CannotProceedException;

@Component
public class JWT {
    private final String SECRET_KEY = "atypon_training";
    private final JwtParser JWT_PARSER;
    private final String TOKEN_HEADER = "Authorization";
    private final String TOKEN_PREFIX = "Bearer ";

    public JWT(){
        this.JWT_PARSER = Jwts.parser().setSigningKey(SECRET_KEY);
    }

    public String createToken(User user) {
        Claims claims = Jwts.claims().setSubject(user.getUsername());
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    private Claims parseJwtClaims(String token) {
        return JWT_PARSER.parseClaimsJws(token).getBody();
    }

    public Claims resolveClaims(String token) throws NullPointerException {
        if (token == null)
            throw new NullPointerException();
        return parseJwtClaims(token);
    }

    public String resolveToken(HttpServletRequest request) throws NullPointerException, CannotProceedException {
        String bearerToken = request.getHeader(TOKEN_HEADER);
        if (bearerToken == null)
            throw new NullPointerException();
        if (bearerToken.startsWith(TOKEN_PREFIX))
            return bearerToken.substring(TOKEN_PREFIX.length());
        throw new CannotProceedException();
    }

    public String getUsername(Claims claims) {
        return claims.getSubject();
    }
}
