import React, { useState } from 'react';
import { Input, Select } from 'antd';
import PropTypes from 'prop-types';

const { Option } = Select;

interface PriceInputProps {
    value?: string;
    onChange?: (value: string) => void;
}

type PrincipalType = 'serviceAccount' | 'group';

export const InputPrincipalId: React.FC<PriceInputProps> = ({ value, onChange }) => {
    const defaultValues = value?.includes(':') ? value?.split(':') : ['serviceAccount', ''];
    const [principalId, setPrincipalId] = useState(defaultValues[1]);
    const [principalType, setPrincipalType] = useState<PrincipalType>(defaultValues[0] as PrincipalType);

    const triggerChange = ({ type, id }: { type?: PrincipalType; id?: string }) => {
        const newValue = `${type}:${id}`;
        onChange?.(newValue);
    };

    const principalIdChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const newPrincipalId = e.target.value || '';
        setPrincipalId(newPrincipalId);
        triggerChange({ type: principalType, id: newPrincipalId });
    };

    const onTypeChange = (newType: PrincipalType) => {
        setPrincipalType(newType);
        triggerChange({ type: newType, id: principalId });
    };

    return (
        <span>
            <Input
                type="text"
                value={principalId}
                onChange={principalIdChange}
                addonBefore={
                    <Select value={principalType} onChange={onTypeChange}>
                        <Option value="serviceAccount">Service Account</Option>
                        <Option value="group">Group</Option>
                    </Select>
                }
            />
        </span>
    );
};

InputPrincipalId.propTypes = {
    value: PropTypes.string,
    onChange: PropTypes.func,
};
