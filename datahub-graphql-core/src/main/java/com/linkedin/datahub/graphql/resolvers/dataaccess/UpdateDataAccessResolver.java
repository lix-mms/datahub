package com.linkedin.datahub.graphql.resolvers.dataaccess;

import com.datahub.authentication.Authentication;
import com.linkedin.common.AuditStamp;
import com.linkedin.common.urn.DataAccessUrn;
import com.linkedin.common.urn.DatasetUrn;
import com.linkedin.common.urn.UrnUtils;
import com.linkedin.dataaccess.*;
import com.linkedin.datahub.graphql.QueryContext;
import com.linkedin.datahub.graphql.generated.UpdateDataAccessInput;
import com.linkedin.metadata.service.DataAccessService;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.linkedin.datahub.graphql.resolvers.ResolverUtils.bindArgument;


public class UpdateDataAccessResolver implements DataFetcher<CompletableFuture<String>> {

  private final DataAccessService _dataAccessService;

  public UpdateDataAccessResolver(final DataAccessService dataAccessService) {
    _dataAccessService = dataAccessService;
  }

  @Override
  public CompletableFuture<String> get(final DataFetchingEnvironment environment) throws Exception {
    final QueryContext context = environment.getContext();
    Authentication authentication = context.getAuthentication();

    final UpdateDataAccessInput input = bindArgument(environment.getArgument("input"), UpdateDataAccessInput.class);

    return CompletableFuture.supplyAsync(() -> {
      try {
        final var urn = DataAccessUrn.deserialize(input.getUrn());
        DataAccessProperties properties = null;
        if (input.getProperties() != null) {
          properties = new DataAccessProperties();
          final var propertiesInput = input.getProperties();
          if (propertiesInput.getPurpose() != null) {
            properties.setPurpose(propertiesInput.getPurpose());
          }
          if (propertiesInput.getDetails() != null) {
            properties.setDetails(propertiesInput.getDetails());
          }
          if (input.getProperties().getTarget() != null) {
            properties.setTarget(DatasetUrn.createFromString(input.getProperties().getTarget()));
          }
          if (propertiesInput.getFieldAccesses() != null) {
            properties.setFieldAccesses(
                new SchemaFieldAccessArray(
                    propertiesInput.getFieldAccesses().stream().map(a -> new SchemaFieldAccess()
                        .setFieldPath(a.getFieldPath())
                        .setType(SchemaFieldInputMapper.mapGqlTypeToPdlType(a.getType()))
                    ).collect(Collectors.toList())
                )
            );
          }
        }

        if (input.getStatusInfo() == null) {
          throw new IllegalArgumentException("statusInfo must be non-null.");
        }
        final var statusInfo = new DataAccessStatusInfo();
        statusInfo.setStatus(DataAccessStatus.valueOf(input.getStatusInfo().getStatus().toString()));
        final var message = input.getStatusInfo().getMessage();
        statusInfo.setMessage(message == null ? "<NO MESSAGE>" : message);
        statusInfo.setAuditStamp(new AuditStamp()
            .setActor(UrnUtils.getUrn(context.getActorUrn()))
            .setTime(System.currentTimeMillis()));

        return _dataAccessService.updateDataAccess(urn, properties, statusInfo, authentication);
      } catch (URISyntaxException e) {
        throw new IllegalArgumentException(e);
      }
    });
  }
}
