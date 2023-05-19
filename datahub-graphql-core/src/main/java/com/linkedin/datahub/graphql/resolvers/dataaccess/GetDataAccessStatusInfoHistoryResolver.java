package com.linkedin.datahub.graphql.resolvers.dataaccess;

import com.linkedin.common.urn.DataAccessUrn;
import com.linkedin.common.urn.Urn;
import com.linkedin.datahub.graphql.QueryContext;
import com.linkedin.datahub.graphql.generated.*;
import com.linkedin.datahub.graphql.types.common.mappers.AuditStampMapper;
import com.linkedin.datahub.graphql.types.corpuser.mappers.CorpUserMapper;
import com.linkedin.entity.client.EntityClient;
import com.linkedin.metadata.Constants;
import com.linkedin.metadata.service.DataAccessService;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.linkedin.datahub.graphql.resolvers.ResolverUtils.bindArgument;


/**
 * Resolver used for listing all status info records (i.e. history of status changes) of data access request
 */
public class GetDataAccessStatusInfoHistoryResolver implements DataFetcher<CompletableFuture<GetDataAccessStatusInfoHistoryResult>> {

  private static final Integer DEFAULT_START = 0;
  private static final Integer DEFAULT_COUNT = 20;

  private final EntityClient _entityClient;

  private final DataAccessService _dataAccessService;

  public GetDataAccessStatusInfoHistoryResolver(final EntityClient entityClient, final DataAccessService dataAccessService) {
    _entityClient = entityClient;
    _dataAccessService = dataAccessService;
  }

  @Override
  public CompletableFuture<GetDataAccessStatusInfoHistoryResult> get(final DataFetchingEnvironment environment) throws Exception {

    final QueryContext context = environment.getContext();
    final var input = bindArgument(environment.getArgument("input"), GetDataAccessStatusInfoHistoryInput.class);

    return CompletableFuture.supplyAsync(() -> {
      try {
        final var statusInfoInstancesFromService = _dataAccessService.getDataAccessStatusInfoHistory(
          DataAccessUrn.createFromString(input.getUrn()),
          input.getStart(),
          input.getCount()
        );

        // prepare actors of status info all at once to avoid too many queries
        final var corpuserUrns = statusInfoInstancesFromService.stream().map(
            statusInfo -> statusInfo.getAuditStamp().getActor()).collect(Collectors.toSet());
        final var corpusersResponse = _entityClient.batchGetV2(
            Constants.CORP_USER_ENTITY_NAME,
            corpuserUrns,
            null,
            context.getAuthentication());
        final var corpuserMap = new HashMap<Urn, CorpUser>();
        corpuserUrns.forEach(urn -> corpuserMap.put(urn, CorpUserMapper.map(corpusersResponse.get(urn))));

        final var statusInfoHistoryResult = statusInfoInstancesFromService.stream().map(statusInfo ->
            DataAccessStatusInfo.builder()
                .setStatus(DataAccessStatus.valueOf(statusInfo.getStatus().toString()))
                .setMessage(statusInfo.getMessage())
                .setLastUpdatedBy(corpuserMap.get(statusInfo.getAuditStamp().getActor()))
                .setAuditStamp(AuditStampMapper.map(statusInfo.getAuditStamp()))
                .build()).collect(Collectors.toList());

        final var result = new GetDataAccessStatusInfoHistoryResult();
        result.setUrn(input.getUrn());
        result.setStart(input.getStart() == null ? DEFAULT_START : input.getStart());
        result.setCount(input.getCount() == null ? DEFAULT_COUNT : input.getCount());
        result.setStatusInfoHistory(statusInfoHistoryResult);
        return result;
      } catch (Exception e) {
        throw new RuntimeException("Failed to get status info history", e);
      }
    });
  }
}
