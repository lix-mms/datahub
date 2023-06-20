package com.linkedin.datahub.graphql.resolvers.dataaccess;

import com.linkedin.common.urn.DataAccessUrn;
import com.linkedin.datahub.graphql.QueryContext;
import com.linkedin.datahub.graphql.generated.DataAccessLifeCycle;
import com.linkedin.datahub.graphql.generated.Entity;
import com.linkedin.entity.EntityResponse;
import com.linkedin.entity.client.EntityClient;
import com.linkedin.metadata.Constants;
import com.linkedin.r2.RemoteInvocationException;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;


public class DataAccessLifeCycleResolver implements DataFetcher<CompletableFuture<DataAccessLifeCycle>> {

  private final EntityClient _entityClient;

  public DataAccessLifeCycleResolver(final EntityClient entityClient) {
    _entityClient = entityClient;
  }

  @Override
  public CompletableFuture<DataAccessLifeCycle> get(DataFetchingEnvironment environment) {
    final QueryContext context = environment.getContext();
    final String urn = ((Entity) environment.getSource()).getUrn();
    final var lifeCycle = new DataAccessLifeCycle();
    return CompletableFuture.supplyAsync(() -> {
      try {
        final EntityResponse entity = _entityClient.getV2(
            Constants.DATA_ACCESS_ENTITY_NAME,
            DataAccessUrn.deserialize(urn),
            null, // null means all aspects
            context.getAuthentication());
        if (entity == null) {
          return lifeCycle;
        }

        final var lifeCycleAspect = entity.getAspects().get(Constants.DATA_ACCESS_LIFE_CYCLE_ASPECT_NAME);
        if (lifeCycleAspect == null) {
          return lifeCycle;
        }

        final var lifeCycleData = new com.linkedin.dataaccess.DataAccessLifeCycle(lifeCycleAspect.getValue().data());
        lifeCycle.setRequestTime(lifeCycleData.getRequestTime());
        return lifeCycle;
      } catch (URISyntaxException e) {
        throw new IllegalArgumentException(e);
      } catch (RemoteInvocationException e) {
        throw new RuntimeException(e);
      }
    });
  }
}
