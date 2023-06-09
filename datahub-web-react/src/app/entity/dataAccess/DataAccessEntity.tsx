import React from 'react';
import { DataAccess, EntityType, SearchResult } from '../../../types.generated';
import { Entity, IconStyleType, PreviewType } from '../Entity';
import { DataAccessProfile } from './DataAccessProfile';
import { useGetDataAccessQuery } from '../../../graphql/dataAccess.generated';
import { GenericEntityProperties } from '../shared/types';

export class DataAccessEntity implements Entity<DataAccess> {
    type = EntityType.DataAccess;

    icon = (_: number, __: IconStyleType, ___?: string) => {
        // TODO replace with the final svg icon for DataAccess
        return (
            <path d="M832 64H192c-17.7 0-32 14.3-32 32v832c0 17.7 14.3 32 32 32h640c17.7 0 32-14.3 32-32V96c0-17.7-14.3-32-32-32zm-600 72h560v208H232V136zm560 480H232V408h560v208zm0 272H232V680h560v208zM304 240a40 40 0 1080 0 40 40 0 10-80 0zm0 272a40 40 0 1080 0 40 40 0 10-80 0zm0 272a40 40 0 1080 0 40 40 0 10-80 0z" />
        );
    };

    isSearchEnabled = () => false;

    isBrowseEnabled = () => false;

    isLineageEnabled = () => false;

    getAutoCompleteFieldName = () => 'name';

    getPathName = () => 'dataAccess';

    getEntityName = () => 'Data Access';

    getCollectionName = () => 'Data Access Management';

    renderProfile = (urn: string) => (
        <DataAccessProfile
            urn={urn}
            useEntityQuery={useGetDataAccessQuery}
            entityType={this.type}
            getOverrideProperties={this.getOverridePropertiesFromEntity}
        />
    );

    renderPreview = (_: PreviewType, data: any) => <h1>{`Preview of ${data}`}</h1>;

    renderSearch = (result: SearchResult) => {
        const data = result.entity as DataAccess;
        return <h1>{`${data.dataset}`}</h1>;
    };

    displayName = (accessRequest: DataAccess) => {
        return accessRequest.urn;
    };

    getGenericEntityProperties = (_: DataAccess) => null;

    supportedCapabilities = () => {
        return new Set([]);
    };

    getOverridePropertiesFromEntity = (_?: DataAccess | null): GenericEntityProperties => {
        return {};
    };
}
