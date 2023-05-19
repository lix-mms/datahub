import React, { useEffect, useState } from 'react';
import { Button, Modal, Steps } from 'antd';
import styled from 'styled-components';
import { Rule } from 'antd/lib/form';
import { Store, StoreValue } from 'antd/lib/form/interface';
import { CheckboxOptionType } from 'antd/es/checkbox';

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

export type InputField<T> = {
    defaultValue: CustomType<T, T extends any[] ? true : false>;
    multiValue: T extends any[] ? true : false;
    inputType: 'textfield' | 'textarea' | 'radio' | 'selectable-cell' | 'principalId';
    label?: string;
    required?: boolean;
    help?: string;
    rules?: Rule[];
    normalize?: (value: StoreValue, prevValue: StoreValue, allValues: Store) => StoreValue;
    options?: Array<CheckboxOptionType | string | number>;
    tooltipInfo?: string;
};

export type FieldGroup = {
    label: string;
    type: string;
    inputFields: Record<string, InputField<InputValueType>>;
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

export type Movement = 'forward' | 'backward' | 'stay';

export type StepComponentProps = {
    step: Step;
    stepData: StepData;
    updateStepData?: UpdateStepData;
    moveStep: (movement: Movement) => void;
    navigateTo: Movement;
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
    margin: 2em -2em -2em;
    padding: 2em;
    border-top: 1px solid ${ANTD_GRAY[4]};
    max-height: 400px;
    overflow-y: auto;
`;

const StepSummary = styled.div`
    display: flex;
    flex-direction: column;
    gap: 1em;
`;

const StepsStyled = styled(Steps)`
    width: 90%;
    margin: 0 auto;
`;

export const StepperModal = ({ onCloseModal, onOk, title, steps }: Props) => {
    const [current, setCurrent] = useState(0);
    const [stepsData, setStepsData] = useState<StepsData>({ 0: {} });
    const [navigateTo, setNavigateTo] = useState<Movement>('stay');

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

    const moveStep = (direction: Movement) => {
        if (direction === 'forward') {
            next();
        } else if (direction === 'backward') {
            prev();
        }
        setNavigateTo('stay');
    };

    return (
        <Modal
            title={title}
            width={650}
            key={title}
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
                        <Button
                            type="primary"
                            onClick={() => {
                                setNavigateTo('forward');
                            }}
                        >
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
            <StepsStyled current={current} items={items} />
            {current + 1 < items.length ? (
                <StepContent key={`step-${current}`}>
                    {React.createElement(steps[current].content, {
                        step: steps[current],
                        stepData,
                        updateStepData,
                        moveStep,
                        navigateTo,
                    })}
                </StepContent>
            ) : (
                <StepContent key={`step-${current}`}>
                    <h3>Summary</h3>
                    <StepSummary>
                        {steps.map((step, stepIndex) => (
                            <div key={`confirm-section-${steps[stepIndex].key}`}>
                                {step.confirmation({
                                    step: steps[stepIndex],
                                    stepData: stepsData[stepIndex],
                                })}
                            </div>
                        ))}
                    </StepSummary>
                </StepContent>
            )}
        </Modal>
    );
};
