package com.linkedin.datahub.graphql.resolvers.dataaccess;

import com.linkedin.common.urn.DataAccessUrn;
import com.linkedin.datahub.graphql.QueryContext;
import com.linkedin.datahub.graphql.generated.DataAccessStatus;
import com.linkedin.datahub.graphql.generated.DataAccessStatusInfo;
import com.linkedin.datahub.graphql.generated.Entity;
import com.linkedin.datahub.graphql.types.common.mappers.AuditStampMapper;
import com.linkedin.datahub.graphql.types.corpuser.mappers.CorpUserMapper;
import com.linkedin.entity.EntityResponse;
import com.linkedin.entity.client.EntityClient;
import com.linkedin.metadata.Constants;
import com.linkedin.r2.RemoteInvocationException;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;


public class DataAccessStatusInfoResolver implements DataFetcher<CompletableFuture<DataAccessStatusInfo>> {

  private final EntityClient _entityClient;

  public DataAccessStatusInfoResolver(final EntityClient entityClient) {
    _entityClient = entityClient;
  }

  @Override
  public CompletableFuture<DataAccessStatusInfo> get(DataFetchingEnvironment environment) {
    final QueryContext context = environment.getContext();
    final String urn = ((Entity) environment.getSource()).getUrn();
    final DataAccessStatusInfo statusInfo = new DataAccessStatusInfo();
    return CompletableFuture.supplyAsync(() -> {
      try {
        final EntityResponse entity = _entityClient.getV2(
            Constants.DATA_ACCESS_ENTITY_NAME,
            DataAccessUrn.deserialize(urn),
            null, // null means all aspects
            context.getAuthentication());
        if (entity == null) {
          return statusInfo;
        }

        final var statusInfoAspect = entity.getAspects().get(Constants.DATA_ACCESS_STATUS_INFO_ASPECT_NAME);
        if (statusInfoAspect == null) {
          return statusInfo;
        }

        final var statusInfoData = new com.linkedin.dataaccess.DataAccessStatusInfo(statusInfoAspect.getValue().data());

        statusInfo.setStatus(DataAccessStatus.valueOf(statusInfoData.getStatus().toString()));

        final var auditStamp = statusInfoData.getAuditStamp();
        final var actorResponse = _entityClient.getV2(
            Constants.CORP_USER_ENTITY_NAME,
            auditStamp.getActor(),
            null,
            context.getAuthentication()
        );
        if (actorResponse != null) {
          statusInfo.setLastUpdatedBy(CorpUserMapper.map(actorResponse));
        }
        statusInfo.setAuditStamp(AuditStampMapper.map(auditStamp));
        statusInfo.setMessage(statusInfoData.getMessage());
        return statusInfo;
      } catch (URISyntaxException e) {
        throw new IllegalArgumentException(e);
      } catch (RemoteInvocationException e) {
        throw new RuntimeException(e);
      }
    });
  }
}
