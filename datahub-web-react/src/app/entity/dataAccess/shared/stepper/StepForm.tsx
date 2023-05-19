import React, { useEffect } from 'react';
import { Card, Col, Form, FormInstance, Input, Radio, Row, Tooltip } from 'antd';
import TextArea from 'antd/lib/input/TextArea';
import styled from 'styled-components';
import { Store } from 'antd/lib/form/interface';
import { InfoCircleOutlined } from '@ant-design/icons';

import {
    ConfirmationStepComponentProps,
    FieldGroup as FieldGroupType,
    InputField,
    StepComponentProps,
    StepData,
} from './StepperModal';
import { InputPrincipalId } from './PrincipalIdInput';

type Props = {
    fieldGroups: Record<string, FieldGroupType>;
    form: FormInstance<any>;
    initialValues?: Store;
};
interface AdditionalAttributes {
    addonAfter?: JSX.Element;
}

const layout = {
    labelCol: { span: 10 },
    wrapperCol: { span: 18, offset: 0 },
};

const FieldInput = styled.div`
    width: 100%;
`;

const FieldGroup = styled(Card)`
    margin-bottom: 1em;
`;

const VerticalContainer = styled.div`
    display: flex;
    flex-direction: column;
    gap: 1em;
`;

/* eslint-disable no-template-curly-in-string */
const validateMessages = {
    required: '${label} is required!',
    types: {
        email: '${label} is not a valid email!',
        number: '${label} is not a valid number!',
    },
    number: {
        range: '${label} must be between ${min} and ${max}',
    },
};
/* eslint-enable no-template-curly-in-string */

const buildRules = (required: boolean, label: string) => {
    return { required, message: `${label} is required` };
};

const renderField = (field: InputField<boolean | string | number>) => {
    const { tooltipInfo } = field;
    const additionalAttributes: AdditionalAttributes = {};
    if (tooltipInfo) {
        additionalAttributes.addonAfter = (
            <Tooltip title={tooltipInfo}>
                <InfoCircleOutlined />
            </Tooltip>
        );
    }
    switch (field.inputType) {
        case 'textfield':
            return <Input {...additionalAttributes} />;
        case 'textarea':
            return <TextArea rows={5} />;
        case 'radio':
            return <Radio.Group options={field.options} optionType="button" />;
        case 'principalId':
            return <InputPrincipalId />;
        default:
            break;
    }
    return <></>;
};

export const FormFields = ({ fieldGroups, form, initialValues }: Props) => {
    return (
        <Form
            form={form}
            {...layout}
            initialValues={initialValues}
            validateMessages={validateMessages}
            requiredMark="optional"
        >
            {Object.keys(fieldGroups)
                .filter((group) => fieldGroups[group].type === 'fields-group')
                .map((groupName) => {
                    const fieldGroup = fieldGroups[groupName];
                    return (
                        <FieldGroup size="small" title={fieldGroup.label} key={`group:${groupName}}`}>
                            <Row>
                                <FieldInput>
                                    {Object.keys(fieldGroup.inputFields).map((fieldName) => {
                                        const field = fieldGroup.inputFields[fieldName];
                                        const { required = false, label = fieldName, rules = [], normalize } = field;
                                        return (
                                            <>
                                                <Form.Item
                                                    rules={[...rules, buildRules(required, label)]}
                                                    name={[groupName, fieldName]}
                                                    label={
                                                        <span style={{ whiteSpace: 'normal', wordBreak: 'break-all' }}>
                                                            {field.label}
                                                        </span>
                                                    }
                                                    labelAlign="left"
                                                    normalize={normalize}
                                                    key={`group:${groupName}-field:${fieldName}`}
                                                >
                                                    {renderField(field)}
                                                </Form.Item>
                                            </>
                                        );
                                    })}
                                </FieldInput>
                            </Row>
                        </FieldGroup>
                    );
                })}
        </Form>
    );
};

export const GenericFormStep = ({ step, stepData, updateStepData, moveStep, navigateTo }: StepComponentProps) => {
    const [form] = Form.useForm();

    useEffect(() => {
        if (navigateTo === 'forward') {
            form.validateFields()
                .then((res) => {
                    const data = res as StepData;
                    if (updateStepData) {
                        updateStepData({ stepData: data });
                    }
                    moveStep('forward');
                })
                .catch((errors) => {
                    console.error('errors', errors);
                });
        }
        moveStep('stay');
    }, [moveStep, navigateTo, form, updateStepData]);

    return (
        <>
            <FormFields fieldGroups={step.fieldGroups} form={form} initialValues={stepData} />
        </>
    );
};

export const GenericFormConfirmationStep = ({ step, stepData }: ConfirmationStepComponentProps) => {
    const fieldGroups = Object.keys(step.fieldGroups).map((groupName) => {
        const group = step.fieldGroups[groupName];
        return {
            title: group.label,
            fields: Object.keys(group.inputFields).map((fieldName) => {
                return {
                    title: group.inputFields[fieldName].label,
                    value: stepData[groupName][fieldName],
                };
            }),
        };
    });

    return (
        <VerticalContainer>
            {fieldGroups.map((group) => (
                <Card title={group.title} size="small">
                    <VerticalContainer>
                        {group.fields.map((field) => (
                            <Row>
                                <Col span={10}>{field.title}</Col>
                                <Col span={14}>{field.value}</Col>
                            </Row>
                        ))}
                    </VerticalContainer>
                </Card>
            ))}
        </VerticalContainer>
    );
};
