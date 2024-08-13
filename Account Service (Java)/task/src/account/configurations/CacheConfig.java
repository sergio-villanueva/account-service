package account.configurations;

import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@EnableCaching
@Configuration
public class CacheConfig {

    @Bean
    public CacheManagerCustomizer<ConcurrentMapCacheManager> cacheManagerCustomizer() {
        return new SimpleCacheManagerCustomizer();
    }

    private class SimpleCacheManagerCustomizer implements CacheManagerCustomizer<ConcurrentMapCacheManager> {

        @Override
        public void customize(ConcurrentMapCacheManager cacheManager) {
            cacheManager.setCacheNames(List.of("attemptsCache"));
        }
    }
}
