import React, { useEffect, useState } from 'react';
import { Button, Modal, Steps } from 'antd';
import styled from 'styled-components';
import { ANTD_GRAY } from '../../../shared/constants';

export type CustomType<T, A extends boolean> = T extends string
    ? A extends true
        ? string[]
        : string
    : T extends number
    ? A extends true
        ? number[]
        : number
    : T extends boolean
    ? A extends true
        ? boolean[]
        : boolean
    : never;

type InputValueType = boolean | string | number;

export type InputField<T, M extends boolean> = {
    defaultValue: CustomType<T, M>;
    multiValue: M;
};

export type FieldGroup = {
    label: string;
    type: string;
    inputFields: Record<string, InputField<InputValueType, boolean>>;
};

type UpdateStepData = ({ stepData }: { stepData: StepData }) => void;

export type StepsData = {
    [key: number]: StepData;
};

export type StepData = {
    [key: string]: Record<string, CustomType<InputValueType, boolean>>;
};

export type ConfirmationStepComponentProps = {
    step: Step;
    stepData: StepData;
};

export type StepComponentProps = {
    step: Step;
    stepData: StepData;
    updateStepData?: UpdateStepData;
};

export type Step = {
    title: string;
    content: ({ step, stepData }: StepComponentProps) => JSX.Element;
    fieldGroups: Record<string, FieldGroup>;
    confirmation: ({ step, stepData }: ConfirmationStepComponentProps) => JSX.Element;
    key: number;
};

type Props = {
    onCloseModal: () => void;
    onOk: (values: StepsData) => void;
    title?: string;
    steps: Step[];
};

const StepContent = styled.div`
    margin: 2.5em -2em 0;
    padding: 2em;
    border-top: 1px solid ${ANTD_GRAY[4]};
    max-height: 400px;
    overflow-y: auto;
`;

const StepSummary = styled.div`
    border: 1px solid ${ANTD_GRAY[4]};
    margin-bottom: 1em;
    padding: 1em;
`;

export const StepperModal = ({ onCloseModal, onOk, title, steps }: Props) => {
    const [current, setCurrent] = useState(0);
    const [stepsData, setStepsData] = useState<StepsData>({});

    useEffect(() => {
        const initialValues: StepsData = {};
        steps.forEach((stepData, stepNumber) => {
            const groups = {};
            Object.keys(stepData.fieldGroups).forEach((group) => {
                const stepFields = {};
                const fieldGroup = stepData.fieldGroups[group];
                Object.keys(fieldGroup.inputFields).forEach((fieldName) => {
                    stepFields[fieldName] = fieldGroup.inputFields[fieldName].defaultValue;
                });
                groups[group] = stepFields;
            });
            initialValues[stepNumber] = groups;
        });
        setStepsData(initialValues);
    }, [steps, setStepsData]);

    const updateStepData: UpdateStepData = ({ stepData }) => {
        const newStepsData: StepsData = { ...stepsData };
        newStepsData[current] = stepData;
        setStepsData(newStepsData);
    };

    const items = steps.map((item) => ({ key: item.title, title: item.title }));
    items.push({ key: 'confirmation', title: 'Confirmation' });

    const stepData = stepsData[current];

    const next = () => {
        setCurrent(current + 1);
    };

    const prev = () => {
        setCurrent(current - 1);
    };

    return (
        <Modal
            width={650}
            title={title}
            open
            onCancel={onCloseModal}
            keyboard
            footer={
                <>
                    {current === 0 && (
                        <Button style={{ margin: '0 8px' }} onClick={onCloseModal}>
                            Cancel
                        </Button>
                    )}
                    {current > 0 && (
                        <Button style={{ margin: '0 8px' }} onClick={() => prev()}>
                            Previous
                        </Button>
                    )}
                    {current < items.length - 1 && (
                        <Button type="primary" onClick={() => next()}>
                            Next
                        </Button>
                    )}
                    {current === items.length - 1 && (
                        <Button
                            type="primary"
                            onClick={() => {
                                onOk(stepsData);
                            }}
                        >
                            Done
                        </Button>
                    )}
                </>
            }
        >
            <Steps current={current} items={items} />
            {current + 1 < items.length ? (
                <StepContent>{steps[current].content({ step: steps[current], stepData, updateStepData })}</StepContent>
            ) : (
                <StepContent>
                    <h3>Summary</h3>
                    <StepSummary>
                        {steps.map((step, stepIndex) => {
                            if (current < items.length) {
                                return step.confirmation({ step: steps[stepIndex], stepData: stepsData[stepIndex] });
                            }
                            return <></>;
                        })}
                    </StepSummary>
                </StepContent>
            )}
        </Modal>
    );
};
