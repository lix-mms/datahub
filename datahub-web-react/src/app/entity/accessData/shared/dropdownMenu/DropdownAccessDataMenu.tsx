import React, { useState } from 'react';
import { Button, Dropdown, MenuProps, message } from 'antd';
import styled from 'styled-components';
import { SettingOutlined } from '@ant-design/icons';

import { ReactComponent as DataAccessSvg } from '../../../../../images/data-access.svg';

import { useDataAccessRole } from '../../useDataAccessRole';
import { DataAccessModal } from './DataAccessModal';

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
export const DropdownAccessDataMenu = () => {
    const { isOwner } = useDataAccessRole();
    const [showAccessConfigModal, setShowAccessConfigModal] = useState(false);

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
    }

    return (
        <>
            {items.length > 0 && (
                <DropdownMenuWrapper>
                    <Dropdown menu={{ items }} placement="bottomLeft" trigger={['click']}>
                        <ButtonStyled icon={<DataAccessIcon />}>Data Access</ButtonStyled>
                    </Dropdown>
                    {showAccessConfigModal && (
                        <DataAccessModal
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
                </DropdownMenuWrapper>
            )}
        </>
    );
};
