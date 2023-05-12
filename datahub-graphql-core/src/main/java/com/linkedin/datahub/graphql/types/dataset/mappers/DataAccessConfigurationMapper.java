package com.linkedin.datahub.graphql.types.dataset.mappers;

import com.linkedin.common.urn.Urn;
import com.linkedin.datahub.graphql.generated.DataAccessConfiguration;
import com.linkedin.datahub.graphql.generated.SchemaFieldAccessConfig;
import com.linkedin.mxe.SystemMetadata;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.stream.Collectors;

public class DataAccessConfigurationMapper {

    public static final DataAccessConfigurationMapper INSTANCE = new DataAccessConfigurationMapper();

    public static DataAccessConfiguration map(@Nonnull final com.linkedin.dataset.DataAccessConfiguration dataAccessConfiguration, @Nonnull final Urn entityUrn) {
        return INSTANCE.apply(dataAccessConfiguration, null, entityUrn);
    }

    public DataAccessConfiguration apply(@Nonnull final com.linkedin.dataset.DataAccessConfiguration input, @Nullable final SystemMetadata systemMetadata, @Nonnull final Urn entityUrn) {
        final var result = new DataAccessConfiguration();
        result.setPurposeRequired(input.hasPurposeRequired());
        result.setFieldAccessConfig(input.getFieldAccessConfig().stream()
            .map(schemaFieldAccessConfig -> new SchemaFieldAccessConfig(
                schemaFieldAccessConfig.getFieldPath(),
                schemaFieldAccessConfig.isVisible(),
                schemaFieldAccessConfig.isNdaRequired()
            ))
            .collect(Collectors.toList()));
        return result;
    }
}
