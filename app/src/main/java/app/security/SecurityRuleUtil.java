package app.security;

import io.micronaut.security.rules.SecurityRuleResult;
import reactor.core.publisher.Mono;

public class SecurityRuleUtil {
  public static final Mono<SecurityRuleResult> REJECTED = Mono.just(SecurityRuleResult.REJECTED);
  public static final Mono<SecurityRuleResult> UNKNOWN = Mono.just(SecurityRuleResult.UNKNOWN);
  public static final Mono<SecurityRuleResult> ALLOWED = Mono.just(SecurityRuleResult.ALLOWED);
}
