package com.linkedin.datahub.graphql.types.dataplatformprincipal;

import com.linkedin.common.urn.DataPlatformPrincipalUrn;
import com.linkedin.common.urn.Urn;
import com.linkedin.datahub.graphql.QueryContext;
import com.linkedin.datahub.graphql.generated.DataPlatformPrincipal;
import com.linkedin.datahub.graphql.generated.Entity;
import com.linkedin.datahub.graphql.types.EntityType;
import com.linkedin.datahub.graphql.types.dataplatform.mappers.DataPlatformMapper;
import com.linkedin.entity.client.EntityClient;
import com.linkedin.r2.RemoteInvocationException;
import graphql.execution.DataFetcherResult;
import lombok.SneakyThrows;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.linkedin.metadata.Constants.DATA_PLATFORM_ENTITY_NAME;


public class DataPlatformPrincipalType implements EntityType<DataPlatformPrincipal, String> {

    private final EntityClient _entityClient;

    public DataPlatformPrincipalType(final EntityClient entityClient) {
        _entityClient = entityClient;
    }

    @Override
    public Class<DataPlatformPrincipal> objectClass() {
        return DataPlatformPrincipal.class;
    }

    @Override
    @SneakyThrows({URISyntaxException.class, RemoteInvocationException.class})
    public List<DataFetcherResult<DataPlatformPrincipal>> batchLoad(final List<String> urns, final QueryContext context) {
        final var principals = new ArrayList<DataPlatformPrincipalUrn>();
        for (final var urn : urns) {
            principals.add(DataPlatformPrincipalUrn.createFromString(urn));
        }

        final var dataPlatformUrns = new HashSet<Urn>();
        for (DataPlatformPrincipalUrn principal : principals) {
            dataPlatformUrns.add(principal.getPlatformEntity());
        }

        final var dataPlatformResponses = _entityClient.batchGetV2(
            DATA_PLATFORM_ENTITY_NAME,
            dataPlatformUrns,
            null,
            context.getAuthentication()
        );

        final var results = principals.stream().map(p -> {
            final var result = new DataPlatformPrincipal();
            result.setName(p.getName());
            result.setPlatform(DataPlatformMapper.map(dataPlatformResponses.get(p.getPlatformEntity())));
            return result;
        });

        return results
            .map(result -> DataFetcherResult.<DataPlatformPrincipal>newResult()
                .data(result)
                .build())
            .collect(Collectors.toList());
    }

    @Override
    public com.linkedin.datahub.graphql.generated.EntityType type() {
        return com.linkedin.datahub.graphql.generated.EntityType.DATA_PLATFORM_PRINCIPAL;
    }

    @Override
    public Function<Entity, String> getKeyProvider() {
        return Entity::getUrn;
    }
}
