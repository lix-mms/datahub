package com.linkedin.datahub.graphql.types.dataaccess;

import com.linkedin.common.urn.Urn;
import com.linkedin.datahub.graphql.generated.DataAccess;
import com.linkedin.datahub.graphql.generated.Dataset;
import com.linkedin.datahub.graphql.generated.EntityType;
import com.linkedin.entity.EntityResponse;
import com.linkedin.entity.EnvelopedAspect;
import com.linkedin.entity.EnvelopedAspectMap;
import com.linkedin.metadata.Constants;
import com.linkedin.metadata.key.DataAccessKey;


public class DataAccessMapper {

    private DataAccessMapper() {
    }

    public static DataAccess map(final EntityResponse entityResponse) {
        final DataAccess result = new DataAccess();
        final Urn entityUrn = entityResponse.getUrn();
        final EnvelopedAspectMap aspects = entityResponse.getAspects();

        result.setUrn(entityUrn.toString());
        result.setType(EntityType.DATA_ACCESS);

        final EnvelopedAspect envelopedDataAccessKey = aspects.get(Constants.DATA_ACCESS_KEY_ASPECT_NAME);
        if (envelopedDataAccessKey != null) {
            var key = new DataAccessKey(envelopedDataAccessKey.getValue().data());
            var ds = Dataset.builder()
                    .setType(EntityType.DATASET)
                    .setUrn(key.getDataset().toString()).build();
            result.setDataset(ds);
        } else {
            return null;
        }
        // Skip resolving individual deep fields while they will be resolved by dedicated field resolvers
        return result;
    }
}
