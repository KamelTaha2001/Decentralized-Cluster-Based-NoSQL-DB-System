package kamel.capstone.bootstrapnode.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import kamel.capstone.bootstrapnode.data.model.User;
import kamel.capstone.bootstrapnode.util.Constants;

import javax.naming.CannotProceedException;
import java.util.List;

public class JWT {

    private final JwtParser jwtParser;

    private final String TOKEN_HEADER = "Authorization";
    private final String TOKEN_PREFIX = "Bearer ";

    public JWT(){
        this.jwtParser = Jwts.parser().setSigningKey(Constants.PRIVATE_KEY);
    }

    public String createToken(User user) {
        Claims claims = Jwts.claims().setSubject(user.getUsername());
        claims.put("roles",user.getRoles());
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS256, Constants.PRIVATE_KEY)
                .compact();
    }

    private Claims parseJwtClaims(String token) {
        return jwtParser.parseClaimsJws(token).getBody();
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

    private List<String> getRoles(Claims claims) {
        return (List<String>) claims.get("roles");
    }
}
