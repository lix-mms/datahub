import React, { useState } from 'react';
import { Button, Dropdown, MenuProps, message } from 'antd';
import styled from 'styled-components';
import { SettingOutlined } from '@ant-design/icons';

import { ReactComponent as DataAccessSvg } from '../../../../../images/data-access.svg';

import { useDataAccessRole } from '../../useDataAccessRole';
import { DataAccessConfigurationModal } from './DataAccessConfigurationModal';
import { RequestDataAccessModal } from './RequestDataAccessModal';
import { useEntityData } from '../../../shared/EntityContext';
import { useGetDataAccessConfigurationQuery } from '../../../../../graphql/dataset.generated';

const DropdownMenuWrapper = styled.div`
    margin-right: 0.5em;
`;

const DataAccessIcon = styled(DataAccessSvg)`
    margin-right: 0.8em;
`;

const ButtonStyled = styled(Button)`
    display: flex;
    font-weight: 500;
    color: black !important;
`;

export const DropdownDataAccessMenu = () => {
    const { isOwner } = useDataAccessRole();
    const [showAccessConfigModal, setShowAccessConfigModal] = useState(false);
    const [showRequestDataAccessModal, setShowRequestDataAccessModal] = useState(false);

    /** TODO Perhaps this query can be moved to RequestDataAccessModal component
     * and handle an alternative flow when the an access configuration doesn't exist */
    const { urn } = useEntityData();
    const { data: configurationEntity } = useGetDataAccessConfigurationQuery({
        variables: { urn },
    });

    const items: MenuProps['items'] = [];

    if (isOwner) {
        items.push({
            key: '1',
            label: 'Set up Access',
            onClick: () => {
                setShowAccessConfigModal(true);
            },
            icon: <SettingOutlined />,
        });
    } else if (configurationEntity && configurationEntity.dataset?.dataAccessConfiguration) {
        items.push({
            key: '2',
            label: 'Request Data Access',
            onClick: () => {
                setShowRequestDataAccessModal(true);
            },
        });
    }

    return (
        <>
            {items.length > 0 && (
                <DropdownMenuWrapper>
                    <Dropdown menu={{ items }} placement="bottomLeft" trigger={['click']}>
                        <ButtonStyled icon={<DataAccessIcon />}>Data Access</ButtonStyled>
                    </Dropdown>
                    {showAccessConfigModal && (
                        <DataAccessConfigurationModal
                            onOk={() => {
                                message.success('Access Configuration has been stored');
                                setShowAccessConfigModal(false);
                            }}
                            onCloseModal={() => {
                                setShowAccessConfigModal(false);
                            }}
                            key="data-access-modal"
                        />
                    )}
                    {showRequestDataAccessModal && (
                        <RequestDataAccessModal
                            title=""
                            onCloseModal={() => {
                                setShowRequestDataAccessModal(false);
                            }}
                            onOk={() => {
                                message.success('Access Data Request has been sent');
                                setShowRequestDataAccessModal(false);
                            }}
                            key="request-data-access-modal"
                        />
                    )}
                </DropdownMenuWrapper>
            )}
        </>
    );
};
