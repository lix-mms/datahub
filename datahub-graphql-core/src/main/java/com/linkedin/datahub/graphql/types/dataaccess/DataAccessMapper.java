package com.linkedin.datahub.graphql.types.dataaccess;

import com.linkedin.common.urn.DataAccessUrn;
import com.linkedin.common.urn.Urn;
import com.linkedin.datahub.graphql.generated.DataAccess;
import com.linkedin.datahub.graphql.generated.DataPlatformPrincipal;
import com.linkedin.datahub.graphql.generated.Dataset;
import com.linkedin.datahub.graphql.generated.EntityType;
import com.linkedin.entity.EntityResponse;
import com.linkedin.entity.EnvelopedAspect;
import com.linkedin.entity.EnvelopedAspectMap;
import com.linkedin.metadata.Constants;
import com.linkedin.metadata.key.DataAccessKey;
import lombok.SneakyThrows;

import java.net.URISyntaxException;


public class DataAccessMapper {

    private DataAccessMapper() {
    }

    @SneakyThrows(URISyntaxException.class)
    public static DataAccess map(final EntityResponse entityResponse) {
        final DataAccess result = new DataAccess();
        final Urn urn = entityResponse.getUrn();
        final DataAccessUrn dataAccessUrn = DataAccessUrn.createFromUrn(urn);
        final EnvelopedAspectMap aspects = entityResponse.getAspects();

        result.setUrn(urn.toString());
        result.setType(EntityType.DATA_ACCESS);

        final EnvelopedAspect envelopedDataAccessKey = aspects.get(Constants.DATA_ACCESS_KEY_ASPECT_NAME);
        if (envelopedDataAccessKey != null) {
            final var key = new DataAccessKey(envelopedDataAccessKey.getValue().data());

            // related objects here are not populated completely
            // further attributes will be populated in dedicated subsequent resolvers

            final var ds = Dataset.builder()
                .setType(EntityType.DATASET)
                .setUrn(key.getDataset().toString())
                .build();
            result.setDataset(ds);

            final var p = DataPlatformPrincipal.builder()
                .setType(EntityType.DATA_PLATFORM_PRINCIPAL)
                .setUrn(dataAccessUrn.getDataPlatformPrincipalEntity().toString())
                .build();
            result.setPrincipal(p);
        } else {
            return null;
        }

        // Skip resolving individual deep fields while they will be resolved by dedicated field resolvers
        return result;
    }
}
