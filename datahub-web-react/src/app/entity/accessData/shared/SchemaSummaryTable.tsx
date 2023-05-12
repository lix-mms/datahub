import React from 'react';
import { ColumnsType } from 'antd/lib/table';
import { EyeInvisibleOutlined, EyeOutlined } from '@ant-design/icons';
import { StyledTable } from '../../shared/components/styled/StyledTable';

export interface SchemaSummaryDataType {
    key: React.Key;
    field: string;
    visibility: boolean;
}

const columns: ColumnsType<SchemaSummaryDataType> = [
    {
        title: 'Field',
        dataIndex: 'field',
    },
    {
        title: 'Accessibility',
        dataIndex: 'visibility',
        render: (_, { visibility }) => (
            <>
                {visibility ? (
                    <>
                        <EyeOutlined /> Shown
                    </>
                ) : (
                    <>
                        <EyeInvisibleOutlined />
                        Hidden
                    </>
                )}
            </>
        ),
    },
];

interface SchemaTableProps {
    fields: SchemaSummaryDataType[];
}

export const SchemaSummaryTable = ({ fields }: SchemaTableProps) => {
    return <StyledTable size="small" pagination={false} columns={columns} dataSource={fields} scroll={{ y: 163 }} />;
};
