package com.linkedin.datahub.graphql.resolvers.dataaccess;

import com.linkedin.common.urn.DataAccessUrn;
import com.linkedin.datahub.graphql.QueryContext;
import com.linkedin.datahub.graphql.generated.DataAccessProperties;
import com.linkedin.datahub.graphql.generated.Entity;
import com.linkedin.datahub.graphql.generated.SchemaFieldAccess;
import com.linkedin.entity.EntityResponse;
import com.linkedin.entity.client.EntityClient;
import com.linkedin.metadata.Constants;
import com.linkedin.r2.RemoteInvocationException;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


public class DataAccessPropertiesResolver implements DataFetcher<CompletableFuture<DataAccessProperties>> {

  private final EntityClient _entityClient;

  public DataAccessPropertiesResolver(final EntityClient entityClient) {
    _entityClient = entityClient;
  }

  @Override
  public CompletableFuture<DataAccessProperties> get(DataFetchingEnvironment environment) {
    final QueryContext context = environment.getContext();
    final String urn = ((Entity) environment.getSource()).getUrn();
    final var properties = new DataAccessProperties();

    return CompletableFuture.supplyAsync(() -> {
      try {
        final EntityResponse entity = _entityClient.getV2(
            Constants.DATA_ACCESS_ENTITY_NAME,
            DataAccessUrn.deserialize(urn),
            null, // null means all aspects
            context.getAuthentication());
        if (entity == null) {
          return properties;
        }

        final var propertiesAspect = entity.getAspects().get(Constants.DATA_ACCESS_PROPERTIES_ASPECT_NAME);
        if (propertiesAspect == null) {
          return properties;
        }

        final var propertiesData = new com.linkedin.dataaccess.DataAccessProperties(propertiesAspect.getValue().data());
        properties.setPurpose(propertiesData.getPurpose());
        properties.setDetails(propertiesData.getDetails());
        if (propertiesData.getTarget() != null) {
          properties.setTarget(propertiesData.getTarget().toString());
        }
        if (propertiesData.getFieldAccesses() != null) {
          properties.setFieldAccesses(propertiesData.getFieldAccesses().stream().map(
              schemaFieldAccessData -> new SchemaFieldAccess.Builder()
                  .setFieldPath(schemaFieldAccessData.getFieldPath())
                  .setType(SchemaFieldInputMapper.mapPdlTypeToGqlType(schemaFieldAccessData.getType()))
                  .build()
          ).collect(Collectors.toList()));
        }
        return properties;
      } catch (URISyntaxException e) {
        throw new IllegalArgumentException(e);
      } catch (RemoteInvocationException e) {
        throw new RuntimeException(e);
      }
    });
  }
}
