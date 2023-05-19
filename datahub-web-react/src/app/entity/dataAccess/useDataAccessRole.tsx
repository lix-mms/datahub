import { useUserContext } from '../../context/useUserContext';
import { useEntityData } from '../shared/EntityContext';
import { EntityType } from '../../../types.generated';

type DataAccessRoleHook = {
    isOwner: boolean;
    ownerUrn: string | undefined;
};

export function useDataAccessRole(): DataAccessRoleHook {
    const { urn: userUrn } = useUserContext();
    const { entityType, entityData } = useEntityData();
    const isDataset = entityType === EntityType.Dataset;

    if (isDataset) {
        const owners = entityData?.ownership?.owners?.map((owner) => owner.owner.urn);
        const ownerUrn = owners?.filter((el) => el === userUrn);
        const isOwner = Boolean(ownerUrn && ownerUrn.length > 0);

        return {
            isOwner,
            ownerUrn: ownerUrn && ownerUrn[0],
        };
    }

    return {
        isOwner: false,
        ownerUrn: undefined,
    };
}
