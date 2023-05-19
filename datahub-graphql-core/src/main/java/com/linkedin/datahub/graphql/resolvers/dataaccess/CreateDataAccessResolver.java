package com.linkedin.datahub.graphql.resolvers.dataaccess;

import com.datahub.authentication.Authentication;
import com.linkedin.common.AuditStamp;
import com.linkedin.common.urn.DataPlatformPrincipalUrn;
import com.linkedin.common.urn.DatasetUrn;
import com.linkedin.common.urn.UrnUtils;
import com.linkedin.dataaccess.*;
import com.linkedin.datahub.graphql.QueryContext;
import com.linkedin.datahub.graphql.generated.CreateDataAccessInput;
import com.linkedin.metadata.key.DataAccessKey;
import com.linkedin.metadata.service.DataAccessService;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.linkedin.datahub.graphql.resolvers.ResolverUtils.bindArgument;


public class CreateDataAccessResolver implements DataFetcher<CompletableFuture<String>> {

  private final DataAccessService _dataAccessService;

  public CreateDataAccessResolver(final DataAccessService dataAccessService) {
    _dataAccessService = dataAccessService;
  }

  @Override
  public CompletableFuture<String> get(final DataFetchingEnvironment environment) throws Exception {
    final QueryContext context = environment.getContext();
    Authentication authentication = context.getAuthentication();

    final CreateDataAccessInput input = bindArgument(environment.getArgument("input"), CreateDataAccessInput.class);

    return CompletableFuture.supplyAsync(() -> {
      try {
        final var datasetUrn = DatasetUrn.deserialize(input.getDatasetUrn());
        final DataAccessKey key = new DataAccessKey();
        key.setDataset(datasetUrn);
        key.setPrincipal(DataPlatformPrincipalUrn.deserialize(input.getDataPlatformPrincipalUrn()));

        final var properties = new DataAccessProperties();
        properties.setPurpose(input.getProperties().getPurpose());
        properties.setDetails(input.getProperties().getDetails());
        if (input.getProperties().getTarget() != null) {
          properties.setTarget(DatasetUrn.createFromString(input.getProperties().getTarget()));
        }
        if (input.getProperties().getFieldAccesses() != null) {
          properties.setFieldAccesses(
              new SchemaFieldAccessArray(
                  input.getProperties().getFieldAccesses().stream().map(a -> new SchemaFieldAccess()
                      .setFieldPath(a.getFieldPath())
                      .setType(SchemaFieldInputMapper.mapGqlTypeToPdlType(a.getType()))
                  ).collect(Collectors.toList())
              )
          );
        }

        final var statusInfo = new DataAccessStatusInfo();
        var initialMessage = "";
        if (input.getProperties().getPurpose() != null) {
          initialMessage = input.getProperties().getPurpose();
        }
        statusInfo.setStatus(DataAccessStatus.PENDING);
        statusInfo.setMessage(initialMessage);
        statusInfo.setAuditStamp(new AuditStamp()
            .setActor(UrnUtils.getUrn(context.getActorUrn()))
            .setTime(System.currentTimeMillis())
            .setMessage("<SYSTEM MESSAGE>\nNew request for data access created."));

        return _dataAccessService.createDataAccess(key, properties, statusInfo, authentication);
      } catch (URISyntaxException e) {
        throw new IllegalArgumentException(e);
      }
    });
  }
}
