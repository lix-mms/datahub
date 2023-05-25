import React, { useEffect, useMemo } from 'react';
import styled from 'styled-components';
import { Card, Typography } from 'antd';

import { useGetEntityWithSchema } from '../../../shared/tabs/Dataset/Schema/useGetEntitySchema';
import {
    Step,
    InputField,
    StepComponentProps,
    StepperModal,
    StepsData,
    ConfirmationStepComponentProps,
} from '../stepper/StepperModal';
import { SchemaTable } from '../SchemaTable';
import { SchemaSummaryTable } from '../SchemaSummaryTable';
import { groupByFieldPath } from '../../../dataset/profile/schema/utils/utils';
import { useEntityRegistry } from '../../../../useEntityRegistry';
import { filterSchemaRows } from '../../../shared/tabs/Dataset/Schema/utils/filterSchemaRows';
import { useGetDataAccessConfigurationQuery, useUpdateDatasetMutation } from '../../../../../graphql/dataset.generated';
import { useEntityData } from '../../../shared/EntityContext';
import { ExtendedSchemaFields } from '../../../dataset/profile/schema/utils/types';
import { DataAccessConfiguration, SchemaFieldAccessConfig } from '../../../../../types.generated';

type Props = {
    onCloseModal: () => void;
    onOk: () => void;
    title?: string;
};

const TableWrapper = styled.div`
    margin: 1em 0;
`;

/**
 * Map step values to GraphQL structure
 */
const prepareToSend = (values: StepsData, schemaFields: ExtendedSchemaFields[]): DataAccessConfiguration => {
    return {
        purposeRequired: true, // HARDCODED FOR MVP
        fieldAccessConfig: Object.keys(values[0].columns).map((key) => {
            const [schemaData] = schemaFields.filter((field) => field.fieldPath === key);
            return {
                fieldPath: key,
                ndaRequired: false, // HARDCODED FOR MVP
                visible: values[0].columns[key] as boolean,
                type: schemaData.type,
            };
        }),
    };
};

const SchemaStep = ({ step, stepData, updateStepData, moveStep, navigateTo }: StepComponentProps) => {
    const isStepSet = Object.keys(step).length > 0 && Object.keys(stepData).length;

    const selectableGroups = isStepSet
        ? Object.keys(step.fieldGroups)
              .filter((group) => step.fieldGroups[group].type === 'selectable-table')
              .map((groupName) => {
                  return {
                      name: groupName,
                      fields: Object.keys(stepData[groupName]).map((fieldName) => {
                          return { key: fieldName, field: fieldName };
                      }),
                      values: Object.keys(stepData[groupName])
                          .filter((fieldName) => stepData[groupName][fieldName])
                          .map((fieldName) => fieldName),
                      label: step.fieldGroups[groupName].label,
                  };
              })
        : [];

    const filledGroups = Object.values(stepData)
        .map((fieldGroupValue) => Object.values(fieldGroupValue).filter((fieldValue) => !!fieldValue))
        .filter((filledFields) => filledFields.length > 0);

    const isValid = filledGroups.length === selectableGroups.length;

    useEffect(() => {
        if (!isValid) {
            moveStep('stay');
        } else {
            moveStep(navigateTo);
        }
    }, [isValid, moveStep, navigateTo]);

    return (
        <>
            {selectableGroups.map((group) => (
                <Card size="small" title={group.label}>
                    <SchemaTable
                        key={group.name}
                        fields={group.fields}
                        values={group.values}
                        onChangeSelection={({ rows }: { rows: React.Key[] }) => {
                            const newStepData = {};
                            group.fields.forEach((field) => {
                                newStepData[field.field] = rows.includes(field.field);
                            });
                            if (newStepData && updateStepData) {
                                updateStepData({ stepData: { ...stepData, [group.name]: newStepData } });
                            }
                        }}
                    />
                </Card>
            ))}
        </>
    );
};

const SchemaConfirmationStep = ({ step, stepData }: ConfirmationStepComponentProps) => {
    const selectableTables = Object.keys(step.fieldGroups)
        .filter((group) => step.fieldGroups[group].type === 'selectable-table')
        .map((groupName) => {
            return {
                structure: step.fieldGroups[groupName],
                groupName,
                values: Object.keys(stepData[groupName]).map((fieldName) => {
                    return {
                        key: fieldName,
                        field: fieldName,
                        visibility: stepData[groupName][fieldName] as boolean,
                    };
                }),
            };
        });

    return (
        <>
            {selectableTables.map((table) => (
                <>
                    <Typography.Text strong>{table.structure.label}</Typography.Text>
                    <TableWrapper>
                        <SchemaSummaryTable fields={table.values} />
                    </TableWrapper>
                </>
            ))}
        </>
    );
};

export const DataAccessConfigurationModal = ({ onCloseModal, onOk, title }: Props) => {
    const entityRegistry = useEntityRegistry();
    const { urn } = useEntityData();
    const { data: configurationEntity, loading: loadingConfigurationEntity } = useGetDataAccessConfigurationQuery({
        variables: { urn },
    });

    const { entityWithSchema, loading: loadingSchema } = useGetEntityWithSchema();

    const maybeUpdateEntity = useUpdateDatasetMutation?.({
        onCompleted: onOk,
    });
    let updateEntity;
    if (maybeUpdateEntity) {
        [updateEntity] = maybeUpdateEntity;
    }

    const modalTitle = title || 'Set up Access configuration';

    const schemaMetadata: any = entityWithSchema?.schemaMetadata || undefined;
    const editableSchemaMetadata: any = entityWithSchema?.editableSchemaMetadata || undefined;

    const { filteredRows } = filterSchemaRows(schemaMetadata?.fields, editableSchemaMetadata, '', entityRegistry);

    const fieldPaths = useMemo(() => {
        return groupByFieldPath(filteredRows, { showKeySchema: false });
    }, [filteredRows]);

    const storedFieldConfig = useMemo(() => {
        const configurationSaved: Record<string, SchemaFieldAccessConfig> = {};
        configurationEntity?.dataset?.dataAccessConfiguration?.fieldAccessConfig?.forEach((field) => {
            if (field && field.fieldPath) {
                const config = field as SchemaFieldAccessConfig;
                configurationSaved[field.fieldPath] = config;
            }
        });
        return configurationSaved;
    }, [configurationEntity]);

    const fieldConfig = useMemo(() => {
        const schemaFields: Record<string, InputField<boolean>> = {};
        fieldPaths.forEach((el) => {
            let defaultValue = true;
            if (el.fieldPath in storedFieldConfig) {
                defaultValue = storedFieldConfig[el.fieldPath].visible || false;
            }
            schemaFields[el.fieldPath] = { defaultValue, multiValue: false, inputType: 'selectable-cell' };
        });
        return schemaFields;
    }, [fieldPaths, storedFieldConfig]);

    const steps: Step[] = [
        {
            title: 'Columns visibility',
            content: SchemaStep,
            fieldGroups: {
                columns: { inputFields: fieldConfig, label: 'Fields accessibility', type: 'selectable-table' },
            },
            confirmation: SchemaConfirmationStep,
            key: 0,
        },
    ];

    const onComplete = async (values: StepsData) => {
        const dataAccessConfiguration = prepareToSend(values, fieldPaths);

        try {
            await updateEntity?.({ variables: { urn, input: { dataAccessConfiguration } } });
        } catch (errorUpdating) {
            console.error(errorUpdating);
        }
    };

    return (
        <>
            {(loadingSchema || loadingConfigurationEntity) && <>Loading</>}
            <StepperModal
                title={modalTitle}
                onCloseModal={onCloseModal}
                onOk={onComplete}
                steps={steps}
                key={modalTitle}
            />
        </>
    );
};
