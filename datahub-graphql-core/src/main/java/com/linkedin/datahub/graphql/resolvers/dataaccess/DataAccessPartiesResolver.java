package com.linkedin.datahub.graphql.resolvers.dataaccess;

import com.linkedin.common.urn.DataAccessUrn;
import com.linkedin.datahub.graphql.QueryContext;
import com.linkedin.datahub.graphql.generated.DataAccessParties;
import com.linkedin.datahub.graphql.generated.Entity;
import com.linkedin.datahub.graphql.types.corpuser.mappers.CorpUserMapper;
import com.linkedin.entity.EntityResponse;
import com.linkedin.entity.client.EntityClient;
import com.linkedin.metadata.Constants;
import com.linkedin.r2.RemoteInvocationException;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import java.net.URISyntaxException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


public class DataAccessPartiesResolver implements DataFetcher<CompletableFuture<DataAccessParties>> {

  private final EntityClient _entityClient;

  public DataAccessPartiesResolver(final EntityClient entityClient) {
    _entityClient = entityClient;
  }

  @Override
  public CompletableFuture<DataAccessParties> get(DataFetchingEnvironment environment) {
    final QueryContext context = environment.getContext();
    final String urn = ((Entity) environment.getSource()).getUrn();
    final var accessParties = new DataAccessParties();

    return CompletableFuture.supplyAsync(() -> {
      try {
        final EntityResponse entity = _entityClient.getV2(
            Constants.DATA_ACCESS_ENTITY_NAME,
            DataAccessUrn.deserialize(urn),
            null, // null means all aspects
            context.getAuthentication());
        if (entity == null) {
          return accessParties;
        }

        final var accessPartiesAspect = entity.getAspects().get(Constants.DATA_ACCESS_PARTIES_ASPECT_NAME);
        final var accessPartiesData = new com.linkedin.dataaccess.DataAccessParties(accessPartiesAspect.getValue().data());
        final var requesterUrn = accessPartiesData.getRequester();
        if (requesterUrn != null) {
          final EntityResponse requesterEntity = _entityClient.getV2(
              Constants.CORP_USER_ENTITY_NAME,
              requesterUrn,
              null, // null means all aspects
              context.getAuthentication());
          if (requesterEntity != null) {
            accessParties.setRequester(CorpUserMapper.map(requesterEntity));
          }
        }
        final var grantorUrn = accessPartiesData.getGrantor();
        final EntityResponse grantorEntity;
        if (grantorUrn != null) {
          grantorEntity = _entityClient.getV2(
              Constants.CORP_USER_ENTITY_NAME,
              grantorUrn,
              null, // null means all aspects
              context.getAuthentication());
          if (grantorEntity != null) {
            accessParties.setGrantor(CorpUserMapper.map(grantorEntity));
          }
        }
        final var authorizedApprovers = accessPartiesData.getAuthorizedApprovers();
        if (authorizedApprovers != null) {
          final var approversMap = _entityClient.batchGetV2(
              Constants.CORP_USER_ENTITY_NAME,
              Set.copyOf(authorizedApprovers),
              null, // null means all aspects
              context.getAuthentication());
          accessParties.setAuthorizedApprovers(
              approversMap.values()
                  .stream()
                  .map(CorpUserMapper::map)
                  .collect(Collectors.toList()));
        }

        return accessParties;
      } catch (URISyntaxException e) {
        throw new IllegalArgumentException(e);
      } catch (RemoteInvocationException e) {
        throw new RuntimeException(e);
      }
    });
  }
}
