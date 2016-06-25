package sample.chirper.favorite.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import sample.chirper.favorite.api.FavoriteService;

public class FavoriteModule extends AbstractModule implements ServiceGuiceSupport {

    @Override
    protected void configure() {
        bindServices(serviceBinding(FavoriteService.class, FavoriteServiceImpl.class));
    }
}
