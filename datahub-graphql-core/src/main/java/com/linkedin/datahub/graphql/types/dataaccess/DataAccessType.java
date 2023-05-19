package com.linkedin.datahub.graphql.types.dataaccess;

import com.google.common.collect.ImmutableSet;
import com.linkedin.common.urn.Urn;
import com.linkedin.datahub.graphql.QueryContext;
import com.linkedin.datahub.graphql.generated.DataAccess;
import com.linkedin.datahub.graphql.generated.Entity;
import com.linkedin.datahub.graphql.generated.EntityType;
import com.linkedin.entity.EntityResponse;
import com.linkedin.entity.client.EntityClient;
import com.linkedin.metadata.Constants;
import graphql.execution.DataFetcherResult;

import javax.annotation.Nonnull;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


public class DataAccessType implements com.linkedin.datahub.graphql.types.EntityType<DataAccess, String> {

    static final Set<String> ASPECTS_TO_FETCH = ImmutableSet.of(
            Constants.DATA_ACCESS_KEY_ASPECT_NAME,
            Constants.DATA_ACCESS_STATUS_INFO_ASPECT_NAME,
            Constants.DATA_ACCESS_PROPERTIES_ASPECT_NAME
    );
    private final EntityClient _entityClient;

    public DataAccessType(final EntityClient entityClient) {
        _entityClient = entityClient;
    }

    @Override
    public EntityType type() {
        return EntityType.DATA_ACCESS;
    }

    @Override
    public Function<Entity, String> getKeyProvider() {
        return Entity::getUrn;
    }

    @Override
    public Class<DataAccess> objectClass() {
        return DataAccess.class;
    }

    @Override
    public List<DataFetcherResult<DataAccess>> batchLoad(@Nonnull List<String> urns, @Nonnull QueryContext context) throws Exception {
        final List<Urn> darUrns = urns.stream()
                .map(this::getUrn)
                .collect(Collectors.toList());

        try {
            final Map<Urn, EntityResponse> entities = _entityClient.batchGetV2(
                    Constants.DATA_ACCESS_ENTITY_NAME,
                    new HashSet<>(darUrns),
                    ASPECTS_TO_FETCH,
                    context.getAuthentication());

            final List<EntityResponse> gmsResults = new ArrayList<>();
            for (Urn urn : darUrns) {
                gmsResults.add(entities.getOrDefault(urn, null));
            }
            return gmsResults.stream()
                    .map(gmsResult ->
                            gmsResult == null ? null : DataFetcherResult.<DataAccess>newResult()
                                    .data(DataAccessMapper.map(gmsResult))
                                    .build()
                    )
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load data access", e);
        }
    }

    private Urn getUrn(final String urnStr) {
        try {
            return Urn.createFromString(urnStr);
        } catch (URISyntaxException e) {
            throw new RuntimeException(String.format("Failed to convert urn string %s into Urn", urnStr));
        }
    }
}
