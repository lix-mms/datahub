import React, { useEffect, useState } from 'react';
import { ColumnsType } from 'antd/lib/table';

import { StyledTable } from '../../shared/components/styled/StyledTable';

interface DataType {
    key: React.Key;
    field: string;
}
interface SchemaTableProps {
    fields: DataType[];
    values: React.Key[];
    onChangeSelection: ({ rows }: { rows: React.Key[] }) => void;
}

const columns: ColumnsType<DataType> = [
    {
        title: 'Field',
        dataIndex: 'field',
        key: 'field',
    },
];

export const SchemaTable = ({ fields, values, onChangeSelection }: SchemaTableProps) => {
    const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);

    useEffect(() => {
        setSelectedRowKeys(values);
    }, [setSelectedRowKeys, values]);

    const onSelectChange = (newSelectedRowKeys: React.Key[]) => {
        setSelectedRowKeys(newSelectedRowKeys);
        onChangeSelection({ rows: newSelectedRowKeys });
    };

    const rowSelection = {
        selectedRowKeys,
        onChange: onSelectChange,
    };

    return (
        <StyledTable
            pagination={false}
            rowSelection={rowSelection}
            columns={columns}
            dataSource={fields}
            scroll={{ y: 240 }}
            size="small"
        />
    );
};
